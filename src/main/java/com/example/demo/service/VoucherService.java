package com.example.demo.service;

import com.example.demo.dto.VoucherApplyResult;
import com.example.demo.dto.VoucherDto;
import com.example.demo.dto.VoucherRequest;
import com.example.demo.entity.ActiveStatus;
import com.example.demo.entity.Voucher;
import com.example.demo.entity.VoucherDiscountType;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Transactional(readOnly = true)
    public List<VoucherDto> findAll() {
        return voucherRepository.findAll().stream().map(VoucherDto::from).toList();
    }

    @Transactional(readOnly = true)
    public VoucherDto findById(Long id) {
        return VoucherDto.from(getEntity(id));
    }

    public VoucherDto create(VoucherRequest request) {
        validate(request);
        if (voucherRepository.findByCode(request.getCode()).isPresent()) {
            throw ApiException.badRequest("Mã giảm giá đã tồn tại");
        }
        Voucher voucher = new Voucher();
        applyFields(voucher, request);
        return VoucherDto.from(voucherRepository.save(voucher));
    }

    public VoucherDto update(Long id, VoucherRequest request) {
        Voucher voucher = getEntity(id);
        validate(request);
        applyFields(voucher, request);
        return VoucherDto.from(voucherRepository.save(voucher));
    }

    public void delete(Long id) {
        voucherRepository.delete(getEntity(id));
    }

    /**
     * Áp dụng voucher: trả về số tiền được giảm dựa trên subtotal.
     * Chưa ghi nhận usedCount ở đây; việc tăng used_count do OrderService thực hiện.
     */
    @Transactional(readOnly = true)
    public VoucherApplyResult apply(String code, BigDecimal subtotal) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> ApiException.notFound("Mã giảm giá không tồn tại"));

        if (voucher.getStatus() != ActiveStatus.ACTIVE) {
            throw ApiException.badRequest("Mã giảm giá không còn hiệu lực");
        }
        LocalDate today = LocalDate.now();
        if (voucher.getStartDate() != null && today.isBefore(voucher.getStartDate())) {
            throw ApiException.badRequest("Mã giảm giá chưa đến hạn sử dụng");
        }
        if (voucher.getEndDate() != null && today.isAfter(voucher.getEndDate())) {
            throw ApiException.badRequest("Mã giảm giá đã hết hạn");
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw ApiException.badRequest("Mã giảm giá đã hết lượt sử dụng");
        }

        BigDecimal subtotalSafe = subtotal != null ? subtotal : BigDecimal.ZERO;
        if (subtotalSafe.compareTo(voucher.getMinOrderAmount()) < 0) {
            throw ApiException.badRequest("Giá trị đơn tối thiểu để áp dụng là " + voucher.getMinOrderAmount());
        }

        BigDecimal discount;
        if (voucher.getDiscountType() == VoucherDiscountType.PERCENT) {
            discount = subtotalSafe.multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (voucher.getMaxDiscountAmount() != null && discount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discount = voucher.getMaxDiscountAmount();
            }
        } else {
            discount = voucher.getDiscountValue();
        }
        if (discount.compareTo(subtotalSafe) > 0) {
            discount = subtotalSafe;
        }
        return VoucherApplyResult.of(voucher, discount);
    }

    private void validate(VoucherRequest request) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw ApiException.badRequest("Mã giảm giá không được để trống");
        }
        if (request.getDiscountValue() == null || request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw ApiException.badRequest("Giá trị giảm giá phải lớn hơn 0");
        }
    }

    private void applyFields(Voucher voucher, VoucherRequest request) {
        voucher.setCode(request.getCode());
        voucher.setDescription(request.getDescription());
        voucher.setDiscountType(request.getDiscountType() != null ? request.getDiscountType() : VoucherDiscountType.PERCENT);
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setMinOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO);
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setStatus(request.getStatus() != null ? request.getStatus() : ActiveStatus.ACTIVE);
    }

    private Voucher getEntity(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy voucher id=" + id));
    }
}
