/**
 * FacilityType - types of courts / activity spaces at ActiveHub.
 * Each type has a maximum number of participants allowed per booking.
 */
public enum FacilityType {
    BADMINTON("Badminton Court", 12),
    FUTSAL("Futsal Court", 20);

    private final String displayName;
    private final int maxParticipants;

    FacilityType(String displayName, int maxParticipants) {
        this.displayName = displayName;
        this.maxParticipants = maxParticipants;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }
}
