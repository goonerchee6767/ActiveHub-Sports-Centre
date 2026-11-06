import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class PromotionEngine {

    private final List<Promotion> promotions = new ArrayList<>();

    public void addPromotion(Promotion promotion) {
        promotions.add(promotion);
        promotions.sort(Comparator.comparing(Promotion::getCode));
    }

    public List<Promotion> getPromotions() {
        return Collections.unmodifiableList(promotions);
    }

  
    public PromotionResult evaluate(RentalTransaction tx) {
        PromotionResult result = new PromotionResult();
        Promotion best = null;
        long bestSen = 0;

        for (Promotion promotion : promotions) {
            if (!promotion.isEligible(tx)) {
                continue;
            }
            double saving = Money.round(Math.min(promotion.calculateSaving(tx), tx.getSubtotal()));
            result.addEligible(promotion, saving);

            long savingSen = Money.toSen(saving); 
            if (savingSen > bestSen) {
                best = promotion;
                bestSen = savingSen;
            }
        }
        if (best != null) {
            result.select(best, bestSen / 100.0);
        }
        return result;
    }

    
    public PromotionResult applyBestPromotion(RentalTransaction tx) {
        PromotionResult result = evaluate(tx);
        tx.applyPromotion(result);
        return result;
    }
}
