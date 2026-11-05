/**
 * Customer - a person who books a facility or rents items.
 * Encapsulation: private fields, read-only getters.
 */
public class Customer {

    private final String name;
    private final String contactNumber;

    public Customer(String name, String contactNumber) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }
        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact number cannot be empty.");
        }
        this.name = name.trim();
        this.contactNumber = contactNumber.trim();
    }

    public String getName() {
        return name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    @Override
    public String toString() {
        return name + " (" + contactNumber + ")";
    }
}
