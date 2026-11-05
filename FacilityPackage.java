/**
 * FacilityPackage - an hourly court / facility package (e.g. Badminton Court per hour).
 * Linked to a FacilityType so the system knows which package to charge
 * when a booking for that type of court is turned into a transaction.
 */
public class FacilityPackage extends RentalItem {

    private final FacilityType facilityType;

    public FacilityPackage(String itemCode, String itemName, double rentalPrice, FacilityType facilityType) {
        super(itemCode, itemName, rentalPrice);
        if (facilityType == null) {
            throw new IllegalArgumentException("Facility type is required for a facility package.");
        }
        this.facilityType = facilityType;
    }

    public FacilityType getFacilityType() {
        return facilityType;
    }

    @Override
    public ItemCategory getCategory() {
        return ItemCategory.FACILITY;
    }

    @Override
    public String getPriceUnit() {
        return "hour";
    }

    @Override
    public String toFileLine() {
        return baseFileLine() + "|" + facilityType.name();
    }
}
