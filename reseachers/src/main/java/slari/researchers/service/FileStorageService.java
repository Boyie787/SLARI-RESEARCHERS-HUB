package slari.researchers.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final long MAX_DOCUMENT_SIZE = 15 * 1024 * 1024; // 15 MB
    private static final int MAX_IMAGE_DIMENSION = 800;

    @PostConstruct
    public void createUploadDirectories() {
        try {
            Files.createDirectories(Paths.get(uploadDir, "images"));
            Files.createDirectories(Paths.get(uploadDir, "documents"));
            log.info("FileStorageService: Upload directories ready at '{}'", uploadDir);
        } catch (IOException e) {
            log.error("FileStorageService: Could not create upload directories", e);
            throw new RuntimeException("Could not create upload directories", e);
        }
    }

    /**
     * Store a crop image uploaded by a researcher.
     *
     * Validates file type (JPG/PNG/WEBP) and size (max 5 MB),
     * generates a unique filename, saves to uploads/images/,
     * and returns the URL path for the frontend.
     *
     * @param file uploaded MultipartFile from HTTP request
     * @return URL path e.g. "/uploads/images/cassava-abc123.jpg"
     */
    public String storeImage(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE,
                "Image must be JPG, PNG, or WEBP format and under 5MB");

        String filename = generateUniqueFilename(file.getOriginalFilename());
        Path targetPath = Paths.get(uploadDir, "images", filename);

        try {
            byte[] compressed = compressImage(file.getBytes());
            Files.write(targetPath, compressed);

            log.info("Image stored: {} (original: {} bytes, compressed: {} bytes)",
                    filename, file.getSize(), compressed.length);

            return "/uploads/images/" + filename;
        } catch (IOException e) {
            log.error("Failed to store image: {}", filename, e);
            throw new RuntimeException("Failed to store image. Please try again.");
        }
    }

    /**
     * Store a research document (PDF or DOCX).
     *
     * Validates file type and size (max 15 MB),
     * saves to uploads/documents/,
     * returns the URL path for the frontend.
     *
     * @param file uploaded MultipartFile from HTTP request
     * @return URL path e.g. "/uploads/documents/report-abc123.pdf"
     */
    public String storeDocument(MultipartFile file) {
        validateFile(file, ALLOWED_DOCUMENT_TYPES, MAX_DOCUMENT_SIZE,
                "Document must be PDF or DOCX format and under 15MB");

        String filename = generateUniqueFilename(file.getOriginalFilename());
        Path targetPath = Paths.get(uploadDir, "documents", filename);

        saveFile(file, targetPath);
        log.info("Document stored: {}", filename);
        return "/uploads/documents/" + filename;
    }

    /**
     * Compress an image by resizing it to max 800x800 pixels.
     * Converts to JPEG format for better compression.
     *
     * @param imageData raw image bytes
     * @return compressed image bytes
     */
    private byte[] compressImage(byte[] imageData) {
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageData));
            if (original == null) {
                log.warn("Could not read image for compression, returning original");
                return imageData;
            }

            int width = original.getWidth();
            int height = original.getHeight();

            // Only resize if image is larger than max dimension
            if (width <= MAX_IMAGE_DIMENSION && height <= MAX_IMAGE_DIMENSION) {
                return imageData;
            }

            // Calculate new dimensions maintaining aspect ratio
            double ratio = Math.min((double) MAX_IMAGE_DIMENSION / width,
                    (double) MAX_IMAGE_DIMENSION / height);
            int newWidth = (int) (width * ratio);
            int newHeight = (int) (height * ratio);

            // Create resized image
            BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(original, 0, 0, newWidth, newHeight, null);
            g.dispose();

            // Convert to JPEG for better compression
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "jpg", baos);

            log.info("Image compressed from {}x{} to {}x{}", width, height, newWidth, newHeight);
            return baos.toByteArray();

        } catch (Exception e) {
            log.warn("Image compression failed, using original: {}", e.getMessage());
            return imageData;
        }
    }

    /**
     * Delete a file from disk.
     * Called when admin deletes a variety to clean up associated files.
     *
     * @param filePath the URL path e.g. "/uploads/images/cassava-abc123.jpg"
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) return;
        try {
            String relativePath = filePath.startsWith("/uploads/")
                    ? filePath.substring("/uploads/".length())
                    : filePath;

            Path path = Paths.get(uploadDir).resolve(relativePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) log.info("File deleted: {}", filePath);
            else         log.warn("File not found for deletion: {}", filePath);
        } catch (IOException e) {
            log.error("Could not delete file: {}", filePath, e);
        }
    }

    /* ══════════════════════════════════════
       PRIVATE HELPERS
       ══════════════════════════════════════ */

    /**
     * Validate file — checks emptiness, MIME type, and file size.
     * Throws RuntimeException caught by GlobalExceptionHandler.
     */
    private void validateFile(MultipartFile file,
                              List<String> allowedTypes,
                              long maxSize,
                              String errorMessage) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty or not provided.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new RuntimeException(errorMessage);
        }
        if (file.getSize() > maxSize) {
            throw new RuntimeException(errorMessage);
        }
    }

    /**
     * Generate a unique, filesystem-safe filename.
     *
     * Example:
     *   Input:  "Cassava Trial Photo 2024.jpg"
     *   Output: "cassava-trial-photo-2024-3f8a9b2c-1d4e-4f5a-8b6c.jpg"
     *
     * The UUID suffix prevents any two files from ever having the
     * same name, even if the same file is uploaded multiple times.
     */
    private String generateUniqueFilename(String originalFilename) {
        String cleanName = StringUtils.cleanPath(
                originalFilename != null ? originalFilename : "file"
        );

        String extension = "";
        int dotIndex = cleanName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = cleanName.substring(dotIndex);
            cleanName = cleanName.substring(0, dotIndex);
        }

        String safeName = cleanName
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        return safeName + "-" + UUID.randomUUID() + extension;
    }

    /**
     * Write file bytes to disk.
     * REPLACE_EXISTING overwrites if a file with the same name exists.
     */
    private void saveFile(MultipartFile file, Path targetPath) {
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store file at {}", targetPath, e);
            throw new RuntimeException("Failed to store file. Please try again.");
        }
    }
}