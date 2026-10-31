package com.huanpham.financetracker.config;

import com.huanpham.financetracker.FinanceTrackerApplication;
import com.huanpham.financetracker.model.Category;
import com.huanpham.financetracker.model.Transaction;
import com.huanpham.financetracker.model.TransactionType;
import com.huanpham.financetracker.repository.CategoryRepository;
import com.huanpham.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Khởi tạo dữ liệu mẫu khi ứng dụng chạy lần đầu (CSDL trống).
 * <ul>
 *     <li>Danh mục thu/chi mặc định: luôn tạo nếu bảng danh mục trống.</li>
 *     <li>Giao dịch mẫu (6 tháng gần nhất): chỉ tạo khi bật {@code app.seed.sample-transactions=true}.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Value("${app.seed.default-categories:true}")
    private boolean seedCategories;

    @Value("${app.seed.sample-transactions:false}")
    private boolean seedTransactions;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (seedCategories && categoryRepository.count() == 0) {
            categoryRepository.saveAll(defaultCategories());
            log.info("Đã khởi tạo {} danh mục mặc định", categoryRepository.count());
        }
        if (seedTransactions && transactionRepository.count() == 0 && categoryRepository.count() > 0) {
            List<Transaction> samples = sampleTransactions();
            transactionRepository.saveAll(samples);
            log.info("Đã khởi tạo {} giao dịch mẫu", samples.size());
        }
    }

    static List<Category> defaultCategories() {
        return List.of(
                // Chi tiêu
                new Category("Ăn uống", TransactionType.EXPENSE, "bi-cup-hot", "#ff6384"),
                new Category("Di chuyển", TransactionType.EXPENSE, "bi-car-front", "#36a2eb"),
                new Category("Nhà ở", TransactionType.EXPENSE, "bi-house-door", "#ff9f40"),
                new Category("Hóa đơn & Tiện ích", TransactionType.EXPENSE, "bi-lightning-charge", "#ffcd56"),
                new Category("Mua sắm", TransactionType.EXPENSE, "bi-bag", "#9966ff"),
                new Category("Giải trí", TransactionType.EXPENSE, "bi-controller", "#4bc0c0"),
                new Category("Sức khỏe", TransactionType.EXPENSE, "bi-heart-pulse", "#e74c3c"),
                new Category("Giáo dục", TransactionType.EXPENSE, "bi-book", "#2ecc71"),
                new Category("Khác (Chi)", TransactionType.EXPENSE, "bi-three-dots", "#95a5a6"),
                // Thu nhập
                new Category("Lương", TransactionType.INCOME, "bi-cash-stack", "#198754"),
                new Category("Thưởng", TransactionType.INCOME, "bi-gift", "#20c997"),
                new Category("Đầu tư", TransactionType.INCOME, "bi-graph-up-arrow", "#0d6efd"),
                new Category("Kinh doanh / Làm thêm", TransactionType.INCOME, "bi-briefcase", "#6610f2"),
                new Category("Khác (Thu)", TransactionType.INCOME, "bi-plus-circle", "#6c757d"));
    }

    private List<Transaction> sampleTransactions() {
        Map<String, Category> byName = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getName, Function.identity(), (a, b) -> a));
        YearMonth current = YearMonth.now(FinanceTrackerApplication.VIETNAM_ZONE);
        LocalDate today = LocalDate.now(FinanceTrackerApplication.VIETNAM_ZONE);
        List<Transaction> list = new java.util.ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth m = current.minusMonths(i);
            add(list, byName, "Lương", "Lương tháng " + m.getMonthValue(), 15_000_000, m.atDay(5), today);
            add(list, byName, "Nhà ở", "Tiền thuê nhà", 4_000_000, m.atDay(6), today);
            add(list, byName, "Hóa đơn & Tiện ích", "Điện, nước, Internet", 900_000 + i * 35_000L, m.atDay(10), today);
            add(list, byName, "Ăn uống", "Đi chợ, ăn ngoài", 2_800_000 + (i % 3) * 250_000L, m.atDay(15), today);
            add(list, byName, "Di chuyển", "Xăng xe, Grab", 600_000 + (i % 2) * 150_000L, m.atDay(18), today);
            add(list, byName, "Mua sắm", "Mua sắm cá nhân", 1_200_000 + (i % 4) * 300_000L, m.atDay(20), today);
            add(list, byName, "Giải trí", "Xem phim, cafe", 500_000 + (i % 3) * 100_000L, m.atDay(22), today);
            if (i % 2 == 0) {
                add(list, byName, "Kinh doanh / Làm thêm", "Freelance", 3_000_000, m.atDay(25), today);
            }
            if (i % 3 == 0) {
                add(list, byName, "Sức khỏe", "Khám sức khỏe, thuốc", 450_000, m.atDay(12), today);
            }
        }
        return list;
    }

    private static void add(List<Transaction> list, Map<String, Category> categories, String categoryName,
                            String description, long amount, LocalDate date, LocalDate today) {
        Category category = categories.get(categoryName);
        if (category == null || date.isAfter(today)) {
            return;
        }
        Transaction t = new Transaction();
        t.setDescription(description);
        t.setAmount(BigDecimal.valueOf(amount));
        t.setType(category.getType());
        t.setCategory(category);
        t.setDate(date);
        t.setNote("Dữ liệu mẫu");
        list.add(t);
    }
}
