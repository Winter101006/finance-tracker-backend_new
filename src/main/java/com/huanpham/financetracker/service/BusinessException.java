package com.huanpham.financetracker.service;

/**
 * Ném ra khi vi phạm quy tắc nghiệp vụ (ví dụ: xóa danh mục đang được sử dụng).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
