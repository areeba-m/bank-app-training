package com.redmath.categorization.rule;

import com.redmath.categorization.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Matches transaction descriptions against a predefined keyword list before the
 * more expensive LLM-based categorization is attempted. Keeping this list small
 * and merchant-focused keeps cost and latency down for the common cases.
 */
@Component
public class RuleBasedCategorizer {

    private static final List<CategoryRule> RULES = List.of(
            new CategoryRule("k-electric", Category.UTILITIES),
            new CategoryRule("kelectric", Category.UTILITIES),
            new CategoryRule("lesco", Category.UTILITIES),
            new CategoryRule("wapda", Category.UTILITIES),
            new CategoryRule("sui gas", Category.UTILITIES),
            new CategoryRule("ptcl", Category.UTILITIES),
            new CategoryRule("internet", Category.UTILITIES),

            new CategoryRule("netflix", Category.ENTERTAINMENT),
            new CategoryRule("spotify", Category.ENTERTAINMENT),
            new CategoryRule("youtube premium", Category.ENTERTAINMENT),
            new CategoryRule("cinema", Category.ENTERTAINMENT),
            new CategoryRule("cinepax", Category.ENTERTAINMENT),

            new CategoryRule("careem", Category.TRANSPORT),
            new CategoryRule("uber", Category.TRANSPORT),
            new CategoryRule("indrive", Category.TRANSPORT),
            new CategoryRule("petrol", Category.TRANSPORT),
            new CategoryRule("fuel", Category.TRANSPORT),
            new CategoryRule("shell", Category.TRANSPORT),
            new CategoryRule("total parco", Category.TRANSPORT),

            new CategoryRule("atm cash", Category.CASH_WITHDRAWAL),
            new CategoryRule("cash withdrawal", Category.CASH_WITHDRAWAL),
            new CategoryRule("atm withdrawal", Category.CASH_WITHDRAWAL),

            new CategoryRule("mcdonald", Category.FOOD),
            new CategoryRule("kfc", Category.FOOD),
            new CategoryRule("foodpanda", Category.FOOD),
            new CategoryRule("restaurant", Category.FOOD),
            new CategoryRule("cafe", Category.FOOD),
            new CategoryRule("grocery", Category.FOOD),
            new CategoryRule("imtiaz", Category.FOOD),
            new CategoryRule("carrefour", Category.FOOD),

            new CategoryRule("daraz", Category.SHOPPING),
            new CategoryRule("amazon", Category.SHOPPING),
            new CategoryRule("mall", Category.SHOPPING),
            new CategoryRule("outfitters", Category.SHOPPING),

            new CategoryRule("hospital", Category.HEALTH),
            new CategoryRule("pharmacy", Category.HEALTH),
            new CategoryRule("clinic", Category.HEALTH),
            new CategoryRule("d.watson", Category.HEALTH),
            new CategoryRule("chase up", Category.HEALTH),

            new CategoryRule("school", Category.EDUCATION),
            new CategoryRule("university", Category.EDUCATION),
            new CategoryRule("tuition", Category.EDUCATION),
            new CategoryRule("course fee", Category.EDUCATION),

            new CategoryRule("bill payment", Category.BILLS),
            new CategoryRule("utility bill", Category.BILLS)
    );

    /**
     * @param description raw transaction description as entered by the user or merchant feed
     * @return the matched category, or empty if no rule applies
     */
    public Optional<Category> match(String description) {
        if (description == null || description.isBlank()) {
            return Optional.empty();
        }

        String normalized = description.toLowerCase(Locale.ROOT);

        return RULES.stream()
                .filter(rule -> normalized.contains(rule.keyword()))
                .map(CategoryRule::category)
                .findFirst();
    }
}
