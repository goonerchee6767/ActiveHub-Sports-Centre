import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class PromotionResult {

    private final Map<Promotion, Double> eligibleSavings = new LinkedHashMap<>();
    private Promotion selectedPromotion;
    private double selectedSaving;

    void addEligible(Promotion promotion, double saving) {
        eligibleSavings.put(promotion, saving);
    }

    void select(Promotion promotion, double saving) {
        this.selectedPromotion = promotion;
        this.selectedSaving = saving;
    }

    public Map<Promotion, Double> getEligibleSavings() {
        return Collections.unmodifiableMap(eligibleSavings);
    }

    
    public Double getSavingFor(Promotion promotion) {
        return eligibleSavings.get(promotion);
    }

    public Promotion getSelectedPromotion() {
        return selectedPromotion;
    }

    public double getSelectedSaving() {
        return selectedSaving;
    }

    public boolean hasPromotion() {
        return selectedPromotion != null;
    }
}
