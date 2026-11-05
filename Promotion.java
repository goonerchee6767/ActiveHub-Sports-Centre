/**
 * Promotion - ABSTRACT base class for all promotion rules.
 *
 * POLYMORPHISM: PromotionEngine stores a List<Promotion> and calls
 * isEligible() / calculateSaving() without knowing which concrete
 * promotion (A, B or C) it is dealing with. Each subclass overrides the
 * methods with its own rule.
 */
public abstract class Promotion {

    private final String code;
    private final String name;

    protected Promotion(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /** Does the transaction meet this promotion's conditions? */
    public abstract boolean isEligible(RentalTransaction tx);

    /** Monetary saving (RM) this promotion would give. Returns 0 if not eligible. */
    public abstract double calculateSaving(RentalTransaction tx);

    /** Short rule description shown to staff. */
    public abstract String getDescription();

    @Override
    public String toString() {
        return "[" + code + "] " + name;
    }
}
