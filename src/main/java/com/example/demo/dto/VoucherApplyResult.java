package com.example.demo.dto;

import com.example.demo.entity.Voucher;
import com.example.demo.entity.VoucherDiscountType;

import java.math.BigDecimal;

public class VoucherApplyResult {

    private Long voucherId;
    private String code;
    private VoucherDiscountType discountType;
    private BigDecimal discountAmount;

    public VoucherApplyResult(Long voucherId, String code, VoucherDiscountType discountType, BigDecimal discountAmount) {
        this.voucherId = voucherId;
        this.code = code;
        this.discountType = discountType;
        this.discountAmount = discountAmount;
    }

    public static VoucherApplyResult of(Voucher v, BigDecimal discountAmount) {
        return new VoucherApplyResult(v.getId(), v.getCode(), v.getDiscountType(), discountAmount);
    }

    public Long getVoucherId() {
        return voucherId;
    }

    public String getCode() {
        return code;
    }

    public VoucherDiscountType getDiscountType() {
        return discountType;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
}