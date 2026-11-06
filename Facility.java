
public class Facility implements Storable {

    private final String facilityId;
    private final String facilityName;
    private final FacilityType type;

    public Facility(String facilityId, String facilityName, FacilityType type) {
        if (facilityId == null || facilityId.trim().isEmpty() || type == null) {
            throw new IllegalArgumentException("Facility ID and type are required.");
        }
        this.facilityId = facilityId.trim().toUpperCase();
        this.facilityName = facilityName.trim();
        this.type = type;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public FacilityType getType() {
        return type;
    }

    @Override
    public String toFileLine() {
        return facilityId + "|" + facilityName + "|" + type.name();
    }

    @Override
    public String toString() {
        return facilityName + " (" + facilityId + ")";
    }
}
