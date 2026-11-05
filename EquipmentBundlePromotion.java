/**
 * Promotion C - Equipment Bundle Discount.
 * Fixed RM20 off when the transaction has at least ONE facility/court booking
 * AND 3 or more EQUIPMENT units (same or different items).
 * Accessories, facility packages and add-on services are not counted.
 */
public class EquipmentBundlePromotion extends Promotion {

    private static final int MIN_EQUIPMENT_UNITS = 3;
    private static final double FIXED_DISCOUNT = 20.0;

    public EquipmentBundlePromotion() {
        super("C", "Equipment Bundle Discount");
    }

    @Override
    public boolean isEligible(RentalTransaction tx) {
        return tx.hasFacility() && tx.getEquipmentQuantity() >= MIN_EQUIPMENT_UNITS;
    }

    @Override
    public double calculateSaving(RentalTransaction tx) {
        if (!isEligible(tx)) {
            return 0;
        }
        // the discount can never be more than the bill itself
        return Money.round(Math.min(FIXED_DISCOUNT, tx.getSubtotal()));
    }

    @Override
    public String getDescription() {
        return "RM20 off with 1+ facility booking and 3+ equipment units";
    }
}
