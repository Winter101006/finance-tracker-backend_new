package com.huanpham.financetracker.service;

import com.huanpham.financetracker.model.Category;
import com.huanpham.financetracker.model.TransactionType;
import com.huanpham.financetracker.repository.CategoryRepository;
import com.huanpham.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Nghiệp vụ quản lý danh mục thu/chi.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByTypeAscNameAsc();
    }

    public List<Category> findByType(TransactionType type) {
        return categoryRepository.findByTypeOrderByNameAsc(type);
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục #" + id));
    }

    /** Số giao dịch đang dùng mỗi danh mục (key = id danh mục). */
    public Map<Long, Long> usageCounts() {
        return findAll().stream().collect(Collectors.toMap(
                Category::getId,
                c -> transactionRepository.countByCategoryId(c.getId()),
                (a, b) -> a));
    }

    public Map<Long, Category> asMap() {
        return findAll().stream().collect(Collectors.toMap(Category::getId, Function.identity()));
    }

    @Transactional
    public Category save(Category category) {
        String name = category.getName() == null ? "" : category.getName().trim();
        category.setName(name);
        boolean duplicated = category.getId() == null
                ? categoryRepository.existsByNameIgnoreCaseAndType(name, category.getType())
                : categoryRepository.existsByNameIgnoreCaseAndTypeAndIdNot(name, category.getType(), category.getId());
        if (duplicated) {
            throw new BusinessException("Danh mục \"" + name + "\" (" + category.getType().getDisplayName() + ") đã tồn tại");
        }
        if (category.getIcon() == null || category.getIcon().isBlank()) {
            category.setIcon(Category.DEFAULT_ICON);
        }
        if (category.getColor() == null || category.getColor().isBlank()) {
            category.setColor(Category.DEFAULT_COLOR);
        }
        if (category.getId() != null) {
            Category existing = findById(category.getId());
            long used = transactionRepository.countByCategoryId(existing.getId());
            if (used > 0 && existing.getType() != category.getType()) {
                throw new BusinessException("Không thể đổi loại của danh mục đang có " + used + " giao dịch");
            }
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        long used = transactionRepository.countByCategoryId(id);
        if (used > 0) {
            throw new BusinessException("Không thể xóa danh mục \"" + category.getName()
                    + "\" vì đang có " + used + " giao dịch sử dụng");
        }
        categoryRepository.delete(category);
    }
}
