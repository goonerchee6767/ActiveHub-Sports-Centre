/**
 * AddOnItem - accessories (e.g. towel) and add-on services (e.g. locker).
 * These are NOT counted as equipment units for Promotion C.
 */
public class AddOnItem extends RentalItem {

    private final ItemCategory category;

    public AddOnItem(String itemCode, String itemName, double rentalPrice, ItemCategory category) {
        super(itemCode, itemName, rentalPrice);
        if (category != ItemCategory.ACCESSORY && category != ItemCategory.SERVICE) {
            throw new IllegalArgumentException("Add-on item must be ACCESSORY or SERVICE.");
        }
        this.category = category;
    }

    @Override
    public ItemCategory getCategory() {
        return category;
    }

    @Override
    public String getPriceUnit() {
        return category == ItemCategory.SERVICE ? "session" : "piece";
    }

    @Override
    public String toFileLine() {
        return baseFileLine();
    }
}
