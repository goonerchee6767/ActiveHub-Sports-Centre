/**
 * Promotion - ABSTRACT base class for all promotion rules.
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

   
    public abstract boolean isEligible(RentalTransaction tx);

    
    public abstract double calculateSaving(RentalTransaction tx);

  
    public abstract String getDescription();

    @Override
    public String toString() {
        return "[" + code + "] " + name;
    }
}
