/**
 * EquipmentItem - sports equipment such as racquets and balls.
 * Inherits from RentalItem and overrides the abstract methods.
 * Equipment units are counted for Promotion C (Equipment Bundle Discount).
 */
public class EquipmentItem extends RentalItem {

    public EquipmentItem(String itemCode, String itemName, double rentalPrice) {
        super(itemCode, itemName, rentalPrice);
    }

    @Override
    public ItemCategory getCategory() {
        return ItemCategory.EQUIPMENT;
    }

    @Override
    public String getPriceUnit() {
        return "unit";
    }

    @Override
    public String toFileLine() {
        return baseFileLine();
    }
}
