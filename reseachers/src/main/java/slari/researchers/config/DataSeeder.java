package slari.researchers.config;

import slari.researchers.entity.*;
import slari.researchers.enums.Role;
import slari.researchers.enums.VarietyStatus;
import slari.researchers.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VarietyRepository varietyRepository;
    private final RatingRepository ratingRepository;
    private final BookmarkRepository bookmarkRepository;
    private final RecommendationRepository recommendationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // Guard: skip if already seeded
        if (userRepository.count() > 0 || varietyRepository.count() > 0) {
            log.info("DataSeeder: Database already has data — skipping seed.");
            return;
        }

        log.info("DataSeeder: Seeding database with demo data...");

        // Use password that meets requirements
        String hashedPassword = passwordEncoder.encode("Demo@1234");

        /* ============================================
           SEED USERS
           ============================================ */

        // Admin
        User admin = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@slari.gov.sl")
                .password(hashedPassword)
                .role(Role.ADMIN)
                .active(true)
                .build());

        // Researchers
        User amara = userRepository.save(User.builder()
                .name("Dr. Amara Koroma")
                .email("amara@slari.gov.sl")
                .password(hashedPassword)
                .role(Role.RESEARCHER)
                .institute("SLARI Njala")
                .active(true)
                .build());

        User fatmata = userRepository.save(User.builder()
                .name("Dr. Fatmata Bangura")
                .email("fatmata@slari.gov.sl")
                .password(hashedPassword)
                .role(Role.RESEARCHER)
                .institute("SLARI Rokupr")
                .active(true)
                .build());

        User ibrahim = userRepository.save(User.builder()
                .name("Dr. Ibrahim Sesay")
                .email("ibrahim@slari.gov.sl")
                .password(hashedPassword)
                .role(Role.RESEARCHER)
                .institute("SLARI Teko")
                .active(true)
                .build());

        User mariama = userRepository.save(User.builder()
                .name("Dr. Mariama Conteh")
                .email("mariama@slari.gov.sl")
                .password(hashedPassword)
                .role(Role.RESEARCHER)
                .institute("SLARI Kabala")
                .active(true)
                .build());

        // Extension Officers
        User joseph = userRepository.save(User.builder()
                .name("Officer Joseph Kamara")
                .email("joseph@extension.sl")
                .password(hashedPassword)
                .role(Role.EXTENSION)
                .location("Northern Province")
                .active(true)
                .build());

        User hawa = userRepository.save(User.builder()
                .name("Officer Hawa Turay")
                .email("hawa@extension.sl")
                .password(hashedPassword)
                .role(Role.EXTENSION)
                .location("Southern Province")
                .active(true)
                .build());

        User mohamed = userRepository.save(User.builder()
                .name("Officer Mohamed Sesay")
                .email("msesay@extension.sl")
                .password(hashedPassword)
                .role(Role.EXTENSION)
                .location("Eastern Province")
                .active(true)
                .build());

        // Farmers
        User sorie = userRepository.save(User.builder()
                .name("Sorie Kamara")
                .email("sorie@farmer.sl")
                .password(hashedPassword)
                .role(Role.FARMER)
                .location("Tonkolili District")
                .active(true)
                .build());

        User aminata = userRepository.save(User.builder()
                .name("Aminata Koroma")
                .email("aminata@farmer.sl")
                .password(hashedPassword)
                .role(Role.FARMER)
                .location("Kenema District")
                .active(true)
                .build());

        User mohamedTuray = userRepository.save(User.builder()
                .name("Mohamed Turay")
                .email("mturay@farmer.sl")
                .password(hashedPassword)
                .role(Role.FARMER)
                .location("Bombali District")
                .active(true)
                .build());

        User isata = userRepository.save(User.builder()
                .name("Isata Bangura")
                .email("isata@farmer.sl")
                .password(hashedPassword)
                .role(Role.FARMER)
                .location("Bo District")
                .active(true)
                .build());

        User foday = userRepository.save(User.builder()
                .name("Foday Conteh")
                .email("foday@farmer.sl")
                .password(hashedPassword)
                .role(Role.FARMER)
                .location("Pujehun District")
                .active(true)
                .build());

        log.info("DataSeeder: ✅ {} users created.", userRepository.count());

        /* ============================================
           SEED VARIETIES
           ============================================ */

        // Variety 1: Cassava
        Variety cassava = varietyRepository.save(Variety.builder()
                .name("SLARI-CASS-001")
                .crop("Cassava")
                .description("High-yielding cassava variety with strong resistance to Cassava Mosaic Disease. Best suited for humid lowland zones in the Southern Province.")
                .yieldAmount("24").yieldUnit("t/ha")
                .duration("12 months")
                .resistance("CMD, CBSD")
                .region("Southern").season("Bimodal")
                .soilType("Loamy, Sandy loam")
                .rainfall("1200–2000mm").altitude("0–800m")
                .status(VarietyStatus.PUBLISHED)
                .year(2023)
                .researcher(amara)
                .institute("SLARI Njala")
                .insights(List.of(
                        "Best for high rainfall areas",
                        "Matures in 12 months",
                        "Resistant to Cassava Mosaic Disease",
                        "Suited for processing into gari and fufu"))
                .tags(List.of("High Yield", "Disease Resistant", "Processing"))
                .build());

        // Variety 2: Rice
        Variety rice = varietyRepository.save(Variety.builder()
                .name("NERICA-L-14")
                .crop("Rice")
                .description("Upland NERICA rice variety adapted to dryland farming in the northern savannah belt.")
                .yieldAmount("5.2").yieldUnit("t/ha")
                .duration("105 days")
                .resistance("Blast, RYMV")
                .region("Northern").season("Dry & Wet")
                .soilType("Sandy loam, Laterite")
                .rainfall("700–1400mm").altitude("0–600m")
                .status(VarietyStatus.PUBLISHED)
                .year(2022)
                .researcher(fatmata)
                .institute("SLARI Rokupr")
                .insights(List.of(
                        "Performs well in low rainfall zones",
                        "Short maturity — only 105 days",
                        "Ideal for upland dryland farming",
                        "Two crops possible per season"))
                .tags(List.of("Short Season", "Drought Tolerant", "Upland"))
                .build());

        // Variety 3: Maize
        Variety maize = varietyRepository.save(Variety.builder()
                .name("SAMMAZ-15")
                .crop("Maize")
                .description("Open pollinated maize variety developed for savannah agroecology zones.")
                .yieldAmount("4.8").yieldUnit("t/ha")
                .duration("90 days")
                .resistance("Streak virus, Downy mildew")
                .region("Northern").season("Wet")
                .soilType("Well-drained loam")
                .rainfall("900–1600mm").altitude("100–1000m")
                .status(VarietyStatus.PUBLISHED)
                .year(2023)
                .researcher(ibrahim)
                .institute("SLARI Teko")
                .insights(List.of(
                        "Drought tolerant after establishment",
                        "Matures in just 90 days",
                        "High protein content",
                        "Good for intercropping with legumes"))
                .tags(List.of("Drought Tolerant", "Open Pollinated", "High Protein"))
                .build());

        // Variety 4: Sweet Potato
        Variety sweetPotato = varietyRepository.save(Variety.builder()
                .name("SARI-SPT-3")
                .crop("Sweet Potato")
                .description("Orange-fleshed sweet potato rich in Vitamin A.")
                .yieldAmount("18").yieldUnit("t/ha")
                .duration("4 months")
                .resistance("SPCSV, weevil-tolerant")
                .region("All Zones").season("Bimodal")
                .soilType("Sandy loam to loam")
                .rainfall("800–2000mm").altitude("0–1200m")
                .status(VarietyStatus.PUBLISHED)
                .year(2024)
                .researcher(mariama)
                .institute("SLARI Kabala")
                .insights(List.of(
                        "Rich in Vitamin A (orange-fleshed)",
                        "Excellent for child nutrition programs",
                        "Suitable across all regions",
                        "High market demand and good income"))
                .tags(List.of("Vitamin A Rich", "Nutrition", "All Regions"))
                .build());

        // Variety 5: Groundnut
        Variety groundnut = varietyRepository.save(Variety.builder()
                .name("SLARI-GNT-2")
                .crop("Groundnut")
                .description("Spanish-type groundnut variety with good shelling percentage and high oil content.")
                .yieldAmount("1.8").yieldUnit("t/ha")
                .duration("110 days")
                .resistance("Leaf spot, Rosette")
                .region("Northern").season("Wet")
                .soilType("Sandy to sandy loam")
                .rainfall("700–1200mm").altitude("0–500m")
                .status(VarietyStatus.PUBLISHED)
                .year(2022)
                .researcher(ibrahim)
                .institute("SLARI Teko")
                .insights(List.of(
                        "High oil content (48%)",
                        "Good for confectionery and export",
                        "Fixes nitrogen — improves soil fertility",
                        "Suited to Northern Province soils"))
                .tags(List.of("High Oil", "Nitrogen Fixing", "Northern Zones"))
                .build());

        // Variety 6: Sorghum (PENDING)
        Variety sorghum = varietyRepository.save(Variety.builder()
                .name("SORGHUM-SL-01")
                .crop("Sorghum")
                .description("Dual-purpose sorghum variety for food and fodder in semi-arid northern regions.")
                .yieldAmount("3.2").yieldUnit("t/ha")
                .duration("120 days")
                .resistance("Anthracnose, Head mold")
                .region("Northern").season("Wet")
                .soilType("Sandy to clay loam")
                .rainfall("600–1200mm").altitude("0–600m")
                .status(VarietyStatus.PENDING)
                .year(2024)
                .researcher(fatmata)
                .institute("SLARI Rokupr")
                .insights(List.of(
                        "Dual purpose: food and animal fodder",
                        "Withstands dry season moisture stress",
                        "Ideal for northern dry zones",
                        "Good for local brewing industries"))
                .tags(List.of("Dual Purpose", "Semi-Arid", "Fodder"))
                .build());

        // Variety 7: Cowpea
        Variety cowpea = varietyRepository.save(Variety.builder()
                .name("COWPEA-SL-04")
                .crop("Cowpea")
                .description("Early maturing cowpea variety ideal for intercropping with maize.")
                .yieldAmount("1.2").yieldUnit("t/ha")
                .duration("75 days")
                .resistance("Striga, Aphid-tolerant")
                .region("All Zones").season("Bimodal")
                .soilType("Any well-drained soil")
                .rainfall("600–2000mm").altitude("0–1500m")
                .status(VarietyStatus.PUBLISHED)
                .year(2023)
                .researcher(amara)
                .institute("SLARI Njala")
                .insights(List.of(
                        "Matures in just 75 days",
                        "Excellent for intercropping with maize",
                        "Young leaves used as nutritious vegetable",
                        "High protein for household nutrition"))
                .tags(List.of("Early Maturing", "Intercrop", "High Protein"))
                .build());

        // Variety 8: Yam (DRAFT)
        Variety yam = varietyRepository.save(Variety.builder()
                .name("SLARI-YAM-02")
                .crop("Yam")
                .description("White Guinea yam with large tubers and good storability.")
                .yieldAmount("20").yieldUnit("t/ha")
                .duration("8 months")
                .resistance("Yam mosaic virus")
                .region("Southern").season("Wet")
                .soilType("Deep loamy soil")
                .rainfall("1500–2500mm").altitude("0–600m")
                .status(VarietyStatus.DRAFT)
                .year(2024)
                .researcher(mariama)
                .institute("SLARI Kabala")
                .insights(List.of(
                        "Large tuber size — premium quality",
                        "Good storability up to 6 months",
                        "Suited to humid forest farming zones",
                        "Commands premium price at market"))
                .tags(List.of("Large Tuber", "Good Storage", "Southern Zone"))
                .build());

        log.info("DataSeeder: ✅ {} varieties created.", varietyRepository.count());

        /* ============================================
           SEED RATINGS
           ============================================ */

        // Farmers rate published varieties
        ratingRepository.save(Rating.builder()
                .user(sorie)
                .variety(cassava)
                .score(5)
                .build());

        ratingRepository.save(Rating.builder()
                .user(aminata)
                .variety(cassava)
                .score(4)
                .build());

        ratingRepository.save(Rating.builder()
                .user(mohamedTuray)
                .variety(rice)
                .score(5)
                .build());

        ratingRepository.save(Rating.builder()
                .user(isata)
                .variety(maize)
                .score(4)
                .build());

        ratingRepository.save(Rating.builder()
                .user(foday)
                .variety(sweetPotato)
                .score(5)
                .build());

        log.info("DataSeeder: ✅ {} ratings created.", ratingRepository.count());

        /* ============================================
           SEED BOOKMARKS
           ============================================ */

        bookmarkRepository.save(Bookmark.builder()
                .user(sorie)
                .variety(cassava)
                .build());

        bookmarkRepository.save(Bookmark.builder()
                .user(aminata)
                .variety(rice)
                .build());

        bookmarkRepository.save(Bookmark.builder()
                .user(mohamedTuray)
                .variety(cassava)
                .build());

        log.info("DataSeeder: ✅ {} bookmarks created.", bookmarkRepository.count());

        /* ============================================
           SEED RECOMMENDATIONS
           ============================================ */

        recommendationRepository.save(Recommendation.builder()
                .officer(joseph)
                .farmer(sorie)
                .variety(rice)
                .note("This NERICA variety is perfect for your Tonkolili farmland. It matures quickly and handles the rainfall patterns in your area well.")
                .build());

        recommendationRepository.save(Recommendation.builder()
                .officer(hawa)
                .farmer(aminata)
                .variety(cassava)
                .note("SLARI-CASS-001 is ideal for Kenema. The CMD resistance will protect your crop and the yield is excellent.")
                .build());

        recommendationRepository.save(Recommendation.builder()
                .officer(mohamed)
                .farmer(foday)
                .variety(sweetPotato)
                .note("Sweet potato is gaining popularity in your region. This variety has excellent market value and nutritional benefits.")
                .build());

        log.info("DataSeeder: ✅ {} recommendations created.", recommendationRepository.count());

        /* ============================================
           SEED COMPLETE
           ============================================ */

        log.info("DataSeeder: 🌱 SLARI Research Hub is ready!");
        log.info("DataSeeder: 📌 Demo login credentials:");
        log.info("   Admin:      admin@slari.gov.sl / Demo@1234");
        log.info("   Researcher: amara@slari.gov.sl / Demo@1234");
        log.info("   Extension:  joseph@extension.sl / Demo@1234");
        log.info("   Farmer:     sorie@farmer.sl / Demo@1234");
        log.info("DataSeeder: 📌 API running at http://localhost:8080/api/");
        log.info("DataSeeder: 📌 Swagger UI at http://localhost:8080/swagger-ui.html");
    }
}