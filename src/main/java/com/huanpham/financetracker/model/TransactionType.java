package com.huanpham.financetracker.model;

/**
 * Loại giao dịch / danh mục: Thu nhập hoặc Chi tiêu.
 */
public enum TransactionType {

    INCOME("Thu nhập"),
    EXPENSE("Chi tiêu");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
