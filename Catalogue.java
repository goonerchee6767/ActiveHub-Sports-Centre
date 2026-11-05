import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Catalogue - the rental catalogue of ActiveHub Sports Centre.
 *
 * AGGREGATION: a Catalogue holds many RentalItem objects (1 to many),
 * but the items are created outside (by DataStore) and can exist on their own.
 */
public class Catalogue {

    private final List<RentalItem> items = new ArrayList<>();

    public void addItem(RentalItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        if (findItem(item.getItemCode()) != null) {
            throw new IllegalArgumentException("Duplicate item code: " + item.getItemCode());
        }
        items.add(item);
    }

    /** Finds an item by its code (case-insensitive). Returns null if not found. */
    public RentalItem findItem(String itemCode) {
        if (itemCode == null) {
            return null;
        }
        for (RentalItem item : items) {
            if (item.getItemCode().equalsIgnoreCase(itemCode.trim())) {
                return item;
            }
        }
        return null;
    }

    /** Finds the hourly facility package that matches a facility type. */
    public FacilityPackage findPackageFor(FacilityType type) {
        for (RentalItem item : items) {
            if (item instanceof FacilityPackage && ((FacilityPackage) item).getFacilityType() == type) {
                return (FacilityPackage) item;
            }
        }
        return null;
    }

    public List<RentalItem> getItemsByCategory(ItemCategory category) {
        List<RentalItem> result = new ArrayList<>();
        for (RentalItem item : items) {
            if (item.getCategory() == category) {
                result.add(item);
            }
        }
        return result;
    }

    public List<RentalItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int size() {
        return items.size();
    }

    /** Prints the whole catalogue grouped by category so staff can read it easily. */
    public void display() {
        String line = InputHelper.repeat('=', 72);
        System.out.println(line);
        System.out.println("                    ACTIVEHUB RENTAL CATALOGUE");
        System.out.println(line);
        System.out.printf("%-6s %-30s %-10s %s%n", "Code", "Item Name", "Category", "Rental Price");
        System.out.println(InputHelper.repeat('-', 72));
        for (ItemCategory category : ItemCategory.values()) {
            List<RentalItem> list = getItemsByCategory(category);
            if (list.isEmpty()) {
                continue;
            }
            System.out.println("[" + category.getLabel() + "]");
            for (RentalItem item : list) {
                System.out.println(item);
            }
        }
        System.out.println(line);
        System.out.println("Total items: " + items.size());
    }

    /** Prints only one category. */
    public void displayCategory(ItemCategory category) {
        List<RentalItem> list = getItemsByCategory(category);
        System.out.println("[" + category.getLabel() + "]");
        if (list.isEmpty()) {
            System.out.println("  (no items in this category)");
        }
        for (RentalItem item : list) {
            System.out.println(item);
        }
    }
}
