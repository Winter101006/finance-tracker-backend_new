package com.huanpham.financetracker.controller;

import com.huanpham.financetracker.model.Category;
import com.huanpham.financetracker.model.TransactionType;
import com.huanpham.financetracker.service.BusinessException;
import com.huanpham.financetracker.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

/**
 * Quản lý danh mục thu/chi.
 */
@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("incomeCategories", categoryService.findByType(TransactionType.INCOME));
        model.addAttribute("expenseCategories", categoryService.findByType(TransactionType.EXPENSE));
        model.addAttribute("usage", categoryService.usageCounts());
        model.addAttribute("activePage", "categories");
        return "categories/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) TransactionType type, Model model) {
        Category category = new Category();
        category.setType(type == null ? TransactionType.EXPENSE : type);
        return showForm(category, model);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        return showForm(categoryService.findById(id), model);
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("category") Category category,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return showForm(category, model);
        }
        try {
            boolean isNew = category.getId() == null;
            categoryService.save(category);
            redirect.addFlashAttribute("successMessage",
                    (isNew ? "Đã thêm" : "Đã cập nhật") + " danh mục \"" + category.getName() + "\"");
        } catch (BusinessException ex) {
            bindingResult.reject("category.invalid", ex.getMessage());
            return showForm(category, model);
        }
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            categoryService.delete(id);
            redirect.addFlashAttribute("successMessage", "Đã xóa danh mục");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/categories";
    }

    private String showForm(Category category, Model model) {
        model.addAttribute("category", category);
        model.addAttribute("types", TransactionType.values());
        model.addAttribute("activePage", "categories");
        return "categories/form";
    }
}
