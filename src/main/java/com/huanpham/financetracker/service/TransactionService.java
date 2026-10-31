package com.huanpham.financetracker.service;

import com.huanpham.financetracker.model.Category;
import com.huanpham.financetracker.model.Transaction;
import com.huanpham.financetracker.model.TransactionType;
import com.huanpham.financetracker.model.dto.FinancialSummary;
import com.huanpham.financetracker.model.dto.TransactionFilter;
import com.huanpham.financetracker.model.dto.TransactionForm;
import com.huanpham.financetracker.repository.TransactionRepository;
import com.huanpham.financetracker.repository.TransactionSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * Nghiệp vụ quản lý giao dịch và tính toán tổng thu/chi/số dư.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("date"), Sort.Order.desc("id"));

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public List<Transaction> search(TransactionFilter filter) {
        return transactionRepository.findAll(TransactionSpecifications.matches(filter), DEFAULT_SORT);
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch #" + id));
    }

    public List<Transaction> recent() {
        return transactionRepository.findTop5ByOrderByDateDescIdDesc();
    }

    @Transactional
    public Transaction save(TransactionForm form) {
        Category category = categoryService.findById(form.getCategoryId());
        if (category.getType() != form.getType()) {
            throw new BusinessException("Danh mục \"" + category.getName() + "\" không thuộc loại "
                    + form.getType().getDisplayName());
        }
        Transaction transaction = form.getId() == null ? new Transaction() : findById(form.getId());
        transaction.setDescription(form.getDescription().trim());
        transaction.setAmount(form.getAmount());
        transaction.setType(category.getType());
        transaction.setDate(form.getDate());
        transaction.setCategory(category);
        transaction.setNote(form.getNote() == null || form.getNote().isBlank() ? null : form.getNote().trim());
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void delete(Long id) {
        transactionRepository.delete(findById(id));
    }

    /** Tổng thu/chi/số dư trên toàn bộ dữ liệu. */
    public FinancialSummary overallSummary() {
        return FinancialSummary.of(
                transactionRepository.sumByType(TransactionType.INCOME),
                transactionRepository.sumByType(TransactionType.EXPENSE),
                transactionRepository.count());
    }

    /** Tổng thu/chi/số dư trên một tập giao dịch (ví dụ kết quả đã lọc). */
    public static FinancialSummary summarize(Collection<Transaction> transactions) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                income = income.add(t.getAmount());
            } else {
                expense = expense.add(t.getAmount());
            }
        }
        return FinancialSummary.of(income, expense, transactions.size());
    }
}
