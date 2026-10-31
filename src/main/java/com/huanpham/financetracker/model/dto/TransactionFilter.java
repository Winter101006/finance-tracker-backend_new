package com.huanpham.financetracker.model.dto;

import com.huanpham.financetracker.model.TransactionType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Tiêu chí lọc danh sách giao dịch.
 *
 * @param month      tháng cần lọc (định dạng yyyy-MM), null = tất cả
 * @param type       loại giao dịch, null = tất cả
 * @param categoryId danh mục, null = tất cả
 * @param keyword    từ khóa tìm theo mô tả/ghi chú, null = không lọc
 */
public record TransactionFilter(
        @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
        TransactionType type,
        Long categoryId,
        String keyword) {

    public static TransactionFilter empty() {
        return new TransactionFilter(null, null, null, null);
    }

    public LocalDate startDate() {
        return month == null ? null : month.atDay(1);
    }

    public LocalDate endDate() {
        return month == null ? null : month.atEndOfMonth();
    }

    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }
}
