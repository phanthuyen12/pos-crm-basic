package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDto {

    private BigDecimal todayRevenue;
    private long todayOrderCount;
    private long activeProductCount;
    private long newCustomerCount;
    private List<DailyRevenue> revenueLast7Days;
    private List<OrderDto> recentOrders;

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public long getTodayOrderCount() {
        return todayOrderCount;
    }

    public void setTodayOrderCount(long todayOrderCount) {
        this.todayOrderCount = todayOrderCount;
    }

    public long getActiveProductCount() {
        return activeProductCount;
    }

    public void setActiveProductCount(long activeProductCount) {
        this.activeProductCount = activeProductCount;
    }

    public long getNewCustomerCount() {
        return newCustomerCount;
    }

    public void setNewCustomerCount(long newCustomerCount) {
        this.newCustomerCount = newCustomerCount;
    }

    public List<DailyRevenue> getRevenueLast7Days() {
        return revenueLast7Days;
    }

    public void setRevenueLast7Days(List<DailyRevenue> revenueLast7Days) {
        this.revenueLast7Days = revenueLast7Days;
    }

    public List<OrderDto> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<OrderDto> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public static class DailyRevenue {
        private String label;
        private BigDecimal revenue;

        public DailyRevenue(String label, BigDecimal revenue) {
            this.label = label;
            this.revenue = revenue;
        }

        public String getLabel() {
            return label;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }
    }
}