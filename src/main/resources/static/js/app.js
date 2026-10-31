/* Tiện ích dùng chung cho toàn bộ giao diện Finance Tracker */
(function () {
    'use strict';

    /** Định dạng số tiền kiểu Việt Nam: 1.234.567 ₫ */
    window.formatVnd = function (value) {
        const number = Number(value) || 0;
        return number.toLocaleString('vi-VN', {maximumFractionDigits: 0}) + ' ₫';
    };

    /** Rút gọn số lớn cho trục biểu đồ: 1,5tr / 200k */
    window.formatShortVnd = function (value) {
        const n = Number(value) || 0;
        const abs = Math.abs(n);
        if (abs >= 1e9) return (n / 1e9).toLocaleString('vi-VN', {maximumFractionDigits: 1}) + ' tỷ';
        if (abs >= 1e6) return (n / 1e6).toLocaleString('vi-VN', {maximumFractionDigits: 1}) + ' tr';
        if (abs >= 1e3) return (n / 1e3).toLocaleString('vi-VN', {maximumFractionDigits: 0}) + 'k';
        return n.toLocaleString('vi-VN');
    };

    document.addEventListener('DOMContentLoaded', function () {
        // Xác nhận trước khi xóa: <form data-confirm="...">
        document.querySelectorAll('form[data-confirm]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (!window.confirm(form.getAttribute('data-confirm'))) {
                    e.preventDefault();
                }
            });
        });

        // Tự động submit khi thay đổi bộ lọc: <select data-auto-submit>
        document.querySelectorAll('[data-auto-submit]').forEach(function (el) {
            el.addEventListener('change', function () {
                el.form && el.form.submit();
            });
        });

        // Tự ẩn thông báo thành công sau 4 giây
        document.querySelectorAll('.alert-success').forEach(function (alert) {
            setTimeout(function () {
                if (window.bootstrap) {
                    bootstrap.Alert.getOrCreateInstance(alert).close();
                }
            }, 4000);
        });
    });
})();
