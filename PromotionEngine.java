import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * PromotionEngine - rule-based engine that evaluates ALL promotions for a
 * transaction and applies only the ONE with the greatest saving.
 *
 * Tie rule: if savings are equal, the lower alphabetical code wins (A, B, C).
 * This works because promotions are kept sorted by code and a later
 * promotion only replaces the current best when its saving is STRICTLY greater.
 *
 * AGGREGATION: holds a list of Promotion objects.
 * POLYMORPHISM: p.isEligible(tx) / p.calculateSaving(tx) run the correct
 * subclass version at runtime.
 */
public class PromotionEngine {

    private final List<Promotion> promotions = new ArrayList<>();

    public void addPromotion(Promotion promotion) {
        promotions.add(promotion);
        promotions.sort(Comparator.comparing(Promotion::getCode));
    }

    public List<Promotion> getPromotions() {
        return Collections.unmodifiableList(promotions);
    }

    /** Evaluates every promotion and picks the best one (does not change the transaction). */
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

            long savingSen = Money.toSen(saving); // compare in sen to avoid floating point issues
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

    /** Evaluates and applies the best promotion to the transaction. */
    public PromotionResult applyBestPromotion(RentalTransaction tx) {
        PromotionResult result = evaluate(tx);
        tx.applyPromotion(result);
        return result;
    }
}
