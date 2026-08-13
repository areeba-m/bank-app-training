package com.redmath.categorization;

import com.redmath.categorization.entity.Category;
import com.redmath.categorization.rule.RuleBasedCategorizer;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleBasedCategorizerTest {

    private final RuleBasedCategorizer categorizer = new RuleBasedCategorizer();

    @Test
    void shouldMatchKnownUtilityMerchant() {
        Optional<Category> result = categorizer.match("K-ELECTRIC BILL PAYMENT");
        assertEquals(Optional.of(Category.UTILITIES), result);
    }

    @Test
    void shouldMatchKnownEntertainmentMerchant() {
        assertEquals(Optional.of(Category.ENTERTAINMENT), categorizer.match("NETFLIX.COM"));
    }

    @Test
    void shouldMatchKnownTransportMerchant() {
        assertEquals(Optional.of(Category.TRANSPORT), categorizer.match("CAREEM RIDE"));
    }

    @Test
    void shouldMatchCashWithdrawal() {
        assertEquals(Optional.of(Category.CASH_WITHDRAWAL), categorizer.match("ATM CASH WITHDRAWAL"));
    }

    @Test
    void shouldBeCaseInsensitive() {
        assertEquals(Optional.of(Category.FOOD), categorizer.match("mcdonald's lahore"));
    }

    @Test
    void shouldReturnEmptyForUnknownDescription() {
        assertTrue(categorizer.match("TRX-839201").isEmpty());
    }

    @Test
    void shouldReturnEmptyForBlankDescription() {
        assertTrue(categorizer.match("   ").isEmpty());
        assertTrue(categorizer.match(null).isEmpty());
    }
}
