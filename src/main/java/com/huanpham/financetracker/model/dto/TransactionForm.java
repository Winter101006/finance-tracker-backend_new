package com.huanpham.financetracker.model.dto;

import com.huanpham.financetracker.FinanceTrackerApplication;
import com.huanpham.financetracker.model.Transaction;
import com.huanpham.financetracker.model.TransactionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Form object dùng cho màn hình thêm/sửa giao dịch.
 */
@Getter
@Setter
@NoArgsConstructor
public class TransactionForm {

    private Long id;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 100, message = "Mô tả tối đa 100 ký tự")
    private String description;

    @NotNull(message = "Vui lòng nhập số tiền")
    @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0")
    @DecimalMax(value = "999999999999.99", message = "Số tiền quá lớn")
    @Digits(integer = 12, fraction = 2, message = "Số tiền không hợp lệ")
    private BigDecimal amount;

    @NotNull(message = "Vui lòng chọn loại giao dịch")
    private TransactionType type = TransactionType.EXPENSE;

    @NotNull(message = "Vui lòng chọn ngày giao dịch")
    @PastOrPresent(message = "Ngày giao dịch không được ở tương lai")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date = LocalDate.now(FinanceTrackerApplication.VIETNAM_ZONE);

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String note;

    public static TransactionForm from(Transaction t) {
        TransactionForm form = new TransactionForm();
        form.setId(t.getId());
        form.setDescription(t.getDescription());
        form.setAmount(t.getAmount());
        form.setType(t.getType());
        form.setDate(t.getDate());
        form.setCategoryId(t.getCategory().getId());
        form.setNote(t.getNote());
        return form;
    }
}
