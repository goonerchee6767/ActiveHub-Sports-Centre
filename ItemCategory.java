/**
 * ItemCategory - the four categories of items in the rental catalogue.
 */
public enum ItemCategory {
    EQUIPMENT("Equipment", "Equipment"),
    FACILITY("Facility / Court Package", "Facility"),
    ACCESSORY("Accessory", "Accessory"),
    SERVICE("Add-on Service", "Service");

    private final String label;
    private final String shortLabel;

    ItemCategory(String label, String shortLabel) {
        this.label = label;
        this.shortLabel = shortLabel;
    }

    public String getLabel() {
        return label;
    }

    public String getShortLabel() {
        return shortLabel;
    }
}
