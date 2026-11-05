/**
 * RentalItem - ABSTRACT parent class of every item in the rental catalogue.
 *
 * OOP concepts:
 *  - Abstraction  : cannot be instantiated; declares abstract methods
 *                   getCategory() and getPriceUnit().
 *  - Encapsulation: fields are private, accessed via getters, and the
 *                   setter validates the price.
 *  - Inheritance  : EquipmentItem, FacilityPackage and AddOnItem extend it.
 *  - Polymorphism : each subclass returns its own category / unit / file line.
 */
public abstract class RentalItem implements Storable {

    private final String itemCode;
    private final String itemName;
    private double rentalPrice;

    protected RentalItem(String itemCode, String itemName, double rentalPrice) {
        if (itemCode == null || itemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Item code cannot be empty.");
        }
        if (itemName == null || itemName.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty.");
        }
        this.itemCode = itemCode.trim().toUpperCase();
        this.itemName = itemName.trim();
        if (rentalPrice < 0) {
            throw new IllegalArgumentException("Rental price cannot be negative.");
        }
        this.rentalPrice = Money.round(rentalPrice);
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public double getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(double rentalPrice) {
        if (rentalPrice < 0) {
            throw new IllegalArgumentException("Rental price cannot be negative.");
        }
        this.rentalPrice = Money.round(rentalPrice);
    }

    /** Each subclass decides which catalogue category it belongs to. */
    public abstract ItemCategory getCategory();

    /** Unit the price is charged per, e.g. "hour", "unit", "session". */
    public abstract String getPriceUnit();

    /** Only real equipment counts towards the Equipment Bundle promotion. */
    public boolean isEquipment() {
        return getCategory() == ItemCategory.EQUIPMENT;
    }

    public boolean isFacility() {
        return getCategory() == ItemCategory.FACILITY;
    }

    /** Common start of the file line, reused by every subclass. */
    protected String baseFileLine() {
        return getCategory().name() + "|" + itemCode + "|" + itemName + "|"
                + String.format("%.2f", rentalPrice);
    }

    @Override
    public String toString() {
        return String.format("%-6s %-30s %-10s RM%7.2f / %s",
                itemCode, itemName, getCategory().getShortLabel(), rentalPrice, getPriceUnit());
    }
}
