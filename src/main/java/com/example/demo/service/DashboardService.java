package com.example.demo.service;

import com.example.demo.dto.DashboardDto;
import com.example.demo.dto.OrderDto;
import com.example.demo.entity.ActiveStatus;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.PersonType;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PersonRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PersonRepository personRepository;

    public DashboardService(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            PersonRepository personRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.personRepository = personRepository;
    }

    public DashboardDto getDashboard() {
        DashboardDto dto = new DashboardDto();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<Order> todayOrders = orderRepository.findByCreatedAtBetween(startOfToday, LocalDateTime.now());
        BigDecimal todayRevenue = todayOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTodayRevenue(todayRevenue);
        dto.setTodayOrderCount(todayOrders.size());

        dto.setActiveProductCount(
                productRepository.findByStatus(ActiveStatus.ACTIVE).size());
        dto.setNewCustomerCount(personRepository.findByType(PersonType.CUSTOMER).size());

        // Revenue last 7 days
        List<DashboardDto.DailyRevenue> revenue7 = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            List<Order> dayOrders = orderRepository.findByCreatedAtBetween(
                    day.atStartOfDay(), day.plusDays(1).atStartOfDay());
            BigDecimal revenue = dayOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            revenue7.add(new DashboardDto.DailyRevenue(day.format(formatter), revenue));
        }
        dto.setRevenueLast7Days(revenue7);

        // Recent orders
        List<OrderDto> recent = orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(5)
                .map(OrderDto::from)
                .toList();
        dto.setRecentOrders(recent);

        return dto;
    }
}