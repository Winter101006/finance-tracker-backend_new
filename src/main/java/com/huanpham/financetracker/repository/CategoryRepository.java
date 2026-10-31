package com.huanpham.financetracker.repository;

import com.huanpham.financetracker.model.Category;
import com.huanpham.financetracker.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByTypeAscNameAsc();

    List<Category> findByTypeOrderByNameAsc(TransactionType type);

    boolean existsByNameIgnoreCaseAndType(String name, TransactionType type);

    boolean existsByNameIgnoreCaseAndTypeAndIdNot(String name, TransactionType type, Long id);
}
