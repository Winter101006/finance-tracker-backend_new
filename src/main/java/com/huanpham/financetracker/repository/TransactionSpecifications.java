package com.huanpham.financetracker.repository;

import com.huanpham.financetracker.model.Transaction;
import com.huanpham.financetracker.model.dto.TransactionFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Xây dựng điều kiện lọc động cho {@link Transaction}.
 */
public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> matches(TransactionFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.month() != null) {
                predicates.add(cb.between(root.get("date"), filter.startDate(), filter.endDate()));
            }
            if (filter.type() != null) {
                predicates.add(cb.equal(root.get("type"), filter.type()));
            }
            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }
            if (filter.hasKeyword()) {
                String pattern = "%" + filter.keyword().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("description")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.<String>get("note"), "")), pattern)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
