package slari.researchers.enums;

/**
 * VarietyStatus — Publication Workflow Enum
 *
 * Every crop variety moves through this workflow:
 *
 *   DRAFT ──► PENDING ──► PUBLISHED ──► ARCHIVED
 *               │
 *               └──► REJECTED ──► DRAFT (resubmit)
 *
 * DRAFT:
 *   Researcher saved the form but has not submitted it yet.
 *   Only visible to the researcher who created it.
 *
 * PENDING:
 *   Researcher submitted for review.
 *   Visible to admins in the approval queue.
 *   NOT visible to farmers on the browse page.
 *
 * PUBLISHED:
 *   Admin approved the submission.
 *   Visible to all users including guests on the browse page.
 *
 * ARCHIVED:
 *   Admin or researcher archived the variety.
 *   Not visible on public browse page but kept in database.
 *
 * REJECTED:
 *   Admin rejected the submission.
 *   Researcher can edit and resubmit.
 *
 * Stored in the `varieties` table as a VARCHAR column via
 * @Enumerated(EnumType.STRING) on the Variety entity.
 */
public enum VarietyStatus {
    DRAFT,
    PENDING,
    PUBLISHED,
    ARCHIVED,
    REJECTED;

    /**
     * Check if the variety is visible to the public (including guests).
     */
    public boolean isPubliclyVisible() {
        return this == PUBLISHED;
    }

    /**
     * Check if the variety needs admin review.
     */
    public boolean needsReview() {
        return this == PENDING;
    }

    /**
     * Check if the variety can be edited by its creator.
     */
    public boolean isEditable() {
        return this == DRAFT || this == REJECTED;
    }

    /**
     * Get the next status in the workflow.
     */
    public VarietyStatus next() {
        return switch (this) {
            case DRAFT -> PENDING;
            case PENDING -> PUBLISHED;
            case PUBLISHED -> ARCHIVED;
            case ARCHIVED -> this;
            case REJECTED -> DRAFT;
        };
    }

    /**
     * Get the previous status in the workflow.
     */
    public VarietyStatus previous() {
        return switch (this) {
            case DRAFT -> this;
            case PENDING -> DRAFT;
            case PUBLISHED -> PENDING;
            case ARCHIVED -> PUBLISHED;
            case REJECTED -> this;
        };
    }

    /**
     * Check if transition from current status to new status is valid.
     */
    public boolean canTransitionTo(VarietyStatus newStatus) {
        return switch (this) {
            case DRAFT -> newStatus == PENDING || newStatus == ARCHIVED;
            case PENDING -> newStatus == PUBLISHED || newStatus == REJECTED;
            case PUBLISHED -> newStatus == ARCHIVED;
            case ARCHIVED -> newStatus == PUBLISHED;
            case REJECTED -> newStatus == DRAFT || newStatus == ARCHIVED;
        };
    }

    /**
     * Get the display name for UI (capitalized with spaces).
     */
    public String getDisplayName() {
        return switch (this) {
            case DRAFT -> "Draft";
            case PENDING -> "Pending Review";
            case PUBLISHED -> "Published";
            case ARCHIVED -> "Archived";
            case REJECTED -> "Rejected";
        };
    }

    /**
     * Get the CSS class for status badges in UI.
     */
    public String getBadgeClass() {
        return switch (this) {
            case DRAFT -> "badge-secondary";
            case PENDING -> "badge-warning";
            case PUBLISHED -> "badge-success";
            case ARCHIVED -> "badge-info";
            case REJECTED -> "badge-danger";
        };
    }

    /**
     * Get the icon class for status in UI.
     */
    public String getIconClass() {
        return switch (this) {
            case DRAFT -> "fa-pencil-alt";
            case PENDING -> "fa-clock";
            case PUBLISHED -> "fa-check-circle";
            case ARCHIVED -> "fa-archive";
            case REJECTED -> "fa-times-circle";
        };
    }
}