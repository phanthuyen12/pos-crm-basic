(function () {
  const user = guardPage("pos");
  if (!user) return;

  let checkout;
  try {
    checkout = JSON.parse(sessionStorage.getItem("posapp_checkout") || "null");
  } catch (e) {
    checkout = null;
  }

  if (!checkout || !Array.isArray(checkout.items) || checkout.items.length === 0) {
    window.location.replace("/client/pos.html");
    return;
  }

  // Nhân viên liên kết với đơn phải là tài khoản hiện đang đăng nhập.
  checkout.employeeId = user.id;
  const itemsEl = document.getElementById("paymentItems");
  const errorEl = document.getElementById("paymentError");
  const button = document.getElementById("confirmPaymentBtn");
  document.getElementById("backToCartBtn").addEventListener("click", () => window.history.back());
  let voucherId = checkout.voucherId || null;
  const subtotal = checkout.items.reduce((sum, item) => sum + Number(item.unitPrice || 0) * Number(item.quantity || 0), 0);
  let discount = 0;

  function renderSummary() {
    const taxable = Math.max(0, subtotal - discount);
    const tax = Math.round(taxable * 0.08);
    document.getElementById("subtotal").textContent = formatVnd(subtotal);
    document.getElementById("discount").textContent = "-" + formatVnd(discount);
    document.getElementById("tax").textContent = formatVnd(tax);
    document.getElementById("total").textContent = formatVnd(taxable + tax);
  }

  function renderItems() {
    itemsEl.innerHTML = checkout.items.map((item) => `
      <div class="payment-item d-flex justify-content-between gap-3 py-2">
        <div><strong>${item.productName}</strong><div class="small text-muted">${item.quantity} × ${formatVnd(item.unitPrice)}</div></div>
        <strong>${formatVnd(Number(item.unitPrice) * Number(item.quantity))}</strong>
      </div>`).join("");
    document.getElementById("orderPreview").textContent = checkout.orderCodePreview || "Đơn mới";
    document.getElementById("customerName").textContent = checkout.customerName || "Khách lẻ";
    document.getElementById("voucherCode").textContent = checkout.voucherCode ? `(${checkout.voucherCode})` : "";
  }

  async function validateVoucher() {
    if (!checkout.voucherCode) {
      renderSummary();
      return;
    }
    try {
      const response = await apiPost("/api/vouchers/apply", { code: checkout.voucherCode, subtotal });
      if (response.success) {
        voucherId = response.data.voucherId;
        discount = Number(response.data.discountAmount || 0);
      } else {
        voucherId = null;
        checkout.voucherCode = null;
        document.getElementById("voucherCode").textContent = "(không còn hiệu lực)";
        errorEl.textContent = `Voucher không áp dụng được: ${response.message}. Bạn vẫn có thể thanh toán không dùng voucher.`;
        errorEl.classList.remove("d-none");
      }
    } catch (e) {
      voucherId = null;
      checkout.voucherCode = null;
      errorEl.textContent = "Không kiểm tra được voucher. Bạn vẫn có thể tiếp tục thanh toán không dùng voucher.";
      errorEl.classList.remove("d-none");
    }
    renderSummary();
  }

  async function confirmPayment() {
    errorEl.textContent = "";
    errorEl.classList.add("d-none");
    button.disabled = true;
    button.querySelector(".pay-label").classList.add("d-none");
    button.querySelector(".pay-loading").classList.remove("d-none");
    const method = document.querySelector('input[name="paymentMethod"]:checked').value;
    const payload = {
      customerId: checkout.customerId || null,
      employeeId: user.id,
      voucherId: voucherId,
      orderType: checkout.orderType || "TAKEAWAY",
      paymentMethod: method,
      note: checkout.note || null,
      items: checkout.items.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
        note: item.note || null,
      })),
    };
    try {
      const response = await apiPost("/api/orders", payload);
      if (!response.success) {
        errorEl.textContent = response.message || "Không thể tạo đơn thanh toán.";
        errorEl.classList.remove("d-none");
        return;
      }
      sessionStorage.removeItem("posapp_checkout");
      window.location.href = `/client/payment-success.html?orderId=${encodeURIComponent(response.data.id)}`;
    } catch (e) {
      errorEl.textContent = "Lỗi kết nối máy chủ: " + e.message;
      errorEl.classList.remove("d-none");
    } finally {
      button.disabled = false;
      button.querySelector(".pay-label").classList.remove("d-none");
      button.querySelector(".pay-loading").classList.add("d-none");
    }
  }

  button.addEventListener("click", confirmPayment);
  renderItems();
  validateVoucher();
})();
