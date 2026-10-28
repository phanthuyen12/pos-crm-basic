package com.example.demo.dto;

import java.math.BigDecimal;

public class ApplyVoucherRequest {

    private String code;
    private BigDecimal subtotal;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}