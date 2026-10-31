package com.huanpham.financetracker.controller;

import com.huanpham.financetracker.model.Transaction;
import com.huanpham.financetracker.model.TransactionType;
import com.huanpham.financetracker.model.dto.TransactionFilter;
import com.huanpham.financetracker.model.dto.TransactionForm;
import com.huanpham.financetracker.service.BusinessException;
import com.huanpham.financetracker.service.CategoryService;
import com.huanpham.financetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.util.List;

/**
 * Quản lý giao dịch: danh sách + lọc, thêm, sửa, xóa.
 */
@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                       @RequestParam(required = false) TransactionType type,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        TransactionFilter filter = new TransactionFilter(month, type, categoryId, keyword);
        List<Transaction> transactions = transactionService.search(filter);

        model.addAttribute("filter", filter);
        model.addAttribute("transactions", transactions);
        model.addAttribute("summary", TransactionService.summarize(transactions));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("types", TransactionType.values());
        model.addAttribute("activePage", "transactions");
        return "transactions/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) TransactionType type, Model model) {
        TransactionForm form = new TransactionForm();
        if (type != null) {
            form.setType(type);
        }
        return showForm(form, model);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        return showForm(TransactionForm.from(transactionService.findById(id)), model);
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") TransactionForm form,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return showForm(form, model);
        }
        try {
            boolean isNew = form.getId() == null;
            transactionService.save(form);
            redirect.addFlashAttribute("successMessage",
                    isNew ? "Đã thêm giao dịch thành công" : "Đã cập nhật giao dịch thành công");
        } catch (BusinessException ex) {
            bindingResult.rejectValue("categoryId", "category.invalid", ex.getMessage());
            return showForm(form, model);
        }
        return "redirect:/transactions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        transactionService.delete(id);
        redirect.addFlashAttribute("successMessage", "Đã xóa giao dịch");
        return "redirect:/transactions";
    }

    private String showForm(TransactionForm form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("incomeCategories", categoryService.findByType(TransactionType.INCOME));
        model.addAttribute("expenseCategories", categoryService.findByType(TransactionType.EXPENSE));
        model.addAttribute("activePage", "transactions");
        return "transactions/form";
    }
}
