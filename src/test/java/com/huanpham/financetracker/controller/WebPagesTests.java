package com.huanpham.financetracker.controller;

import com.huanpham.financetracker.model.Category;
import com.huanpham.financetracker.model.TransactionType;
import com.huanpham.financetracker.repository.TransactionRepository;
import com.huanpham.financetracker.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Kiểm tra các trang giao diện render thành công (bắt lỗi cú pháp Thymeleaf) và luồng CRUD qua HTTP.
 */
@SpringBootTest
@AutoConfigureMockMvc
class WebPagesTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void clean() {
        transactionRepository.deleteAll();
    }

    @Test
    void allPagesRender() throws Exception {
        Category food = categoryService.findByType(TransactionType.EXPENSE).getFirst();
        mockMvc.perform(post("/transactions/save")
                        .param("description", "Cà phê")
                        .param("amount", "35000")
                        .param("type", "EXPENSE")
                        .param("date", LocalDate.now().toString())
                        .param("categoryId", food.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"));
        Long txId = transactionRepository.findAll().getFirst().getId();

        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("Cà phê")));
        mockMvc.perform(get("/").param("month", "2020-01")).andExpect(status().isOk());
        mockMvc.perform(get("/transactions")).andExpect(status().isOk())
                .andExpect(content().string(containsString("35.000")));
        mockMvc.perform(get("/transactions").param("month", LocalDate.now().toString().substring(0, 7))
                        .param("type", "EXPENSE").param("keyword", "phê"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/transactions/new")).andExpect(status().isOk());
        mockMvc.perform(get("/transactions/" + txId + "/edit")).andExpect(status().isOk());
        mockMvc.perform(get("/categories")).andExpect(status().isOk());
        mockMvc.perform(get("/categories/new")).andExpect(status().isOk());
        mockMvc.perform(get("/categories/" + food.getId() + "/edit")).andExpect(status().isOk());
        mockMvc.perform(get("/transactions/999999/edit")).andExpect(status().isNotFound());

        mockMvc.perform(post("/transactions/" + txId + "/delete")).andExpect(status().is3xxRedirection());
        assertThat(transactionRepository.count()).isZero();
    }

    @Test
    void invalidTransactionShowsErrors() throws Exception {
        mockMvc.perform(post("/transactions/save")
                        .param("description", "")
                        .param("amount", "-5")
                        .param("type", "EXPENSE")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions/form"))
                .andExpect(model().attributeHasFieldErrors("form", "description", "amount", "categoryId"));
    }

    @Test
    void createAndDeleteCategory() throws Exception {
        mockMvc.perform(post("/categories/save")
                        .param("name", "Thú cưng")
                        .param("type", "EXPENSE")
                        .param("icon", "bi-heart")
                        .param("color", "#123456"))
                .andExpect(status().is3xxRedirection());
        Category pet = categoryService.findByType(TransactionType.EXPENSE).stream()
                .filter(c -> c.getName().equals("Thú cưng")).findFirst().orElseThrow();

        // Trùng tên -> hiển thị lại form với lỗi
        mockMvc.perform(post("/categories/save")
                        .param("name", "thú cưng")
                        .param("type", "EXPENSE")
                        .param("color", "#123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"));

        mockMvc.perform(post("/categories/" + pet.getId() + "/delete")).andExpect(status().is3xxRedirection());
        assertThat(categoryService.findByType(TransactionType.EXPENSE))
                .noneMatch(c -> c.getName().equals("Thú cưng"));
    }
}
