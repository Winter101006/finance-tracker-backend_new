package com.huanpham.financetracker.repository;

import com.huanpham.financetracker.model.Transaction;
import com.huanpham.financetracker.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    /** Tổng tiền theo loại trên toàn bộ dữ liệu. */
    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);

    /** Tổng tiền theo loại tính đến (trước) một ngày - dùng để tính số dư đầu kỳ. */
    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.type = :type and t.date < :before")
    BigDecimal sumByTypeBefore(@Param("type") TransactionType type, @Param("before") LocalDate before);

    /** Các giao dịch trong khoảng ngày [from, to], sắp xếp tăng dần theo ngày. */
    List<Transaction> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);

    /** Giao dịch gần đây nhất. */
    List<Transaction> findTop5ByOrderByDateDescIdDesc();

    long countByCategoryId(Long categoryId);
}
