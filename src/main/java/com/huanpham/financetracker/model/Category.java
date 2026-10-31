package com.huanpham.financetracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Danh mục thu/chi (ví dụ: Ăn uống, Lương, Di chuyển...).
 */
@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_category_name_type", columnNames = {"name", "type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    public static final String DEFAULT_ICON = "bi-tag";
    public static final String DEFAULT_COLOR = "#6c757d";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 50, message = "Tên danh mục tối đa 50 ký tự")
    @Column(nullable = false, length = 50)
    private String name;

    @NotNull(message = "Vui lòng chọn loại danh mục")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    /** Tên class Bootstrap Icons, ví dụ: bi-cart. */
    @Size(max = 40, message = "Icon tối đa 40 ký tự")
    @Column(length = 40)
    private String icon = DEFAULT_ICON;

    /** Mã màu HEX dùng cho biểu đồ, ví dụ: #ff6384. */
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Màu phải có dạng #RRGGBB")
    @Column(length = 7)
    private String color = DEFAULT_COLOR;

    public Category(String name, TransactionType type, String icon, String color) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
    }
}
