package com.redmath.categorization.repository;

import com.redmath.categorization.entity.Category;
import com.redmath.categorization.entity.TransactionCategory;
import com.redmath.transactions.entity.Indicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {

    Optional<TransactionCategory> findByTransactionId(Long transactionId);


    @Query("select tc.category as category, sum(t.amount) as total "
            + "from TransactionCategory tc join tc.transaction t "
            + "where t.account.userId = :userId "
            + "and t.indicator = :indicator "
            + "and t.date >= :from and t.date < :to "
            + "group by tc.category")
    List<CategoryTotalProjection> sumAmountsByCategory(
            @Param("userId") Long userId,
            @Param("indicator") Indicator indicator,
            @Param("from") Instant from,
            @Param("to") Instant to);

    interface CategoryTotalProjection {
        Category getCategory();

        BigDecimal getTotal();
    }
}
