package com.huanpham.financetracker.service;

/**
 * Ném ra khi không tìm thấy bản ghi (giao dịch / danh mục).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
