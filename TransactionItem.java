
public class TransactionItem {

    private final RentalItem item;
    private final double unitPrice;
    private int quantity;

    TransactionItem(RentalItem item, int quantity, double unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        this.item = item;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public RentalItem getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        this.quantity = quantity;
    }

    void increaseQuantity(int extra) {
        setQuantity(this.quantity + extra);
    }

    /** Subtotal = rental price x quantity */
    public double getSubtotal() {
        return Money.round(unitPrice * quantity);
    }
}
