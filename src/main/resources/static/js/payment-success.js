(function () {
  const user = guardPage("pos");
  if (!user) return;
  if (isAdmin(user)) document.getElementById("adminOrdersLink").classList.remove("d-none");

  const params = new URLSearchParams(window.location.search);
  const orderId = params.get("orderId");
  const content = document.getElementById("receiptContent");
  const error = document.getElementById("receiptError");
  const paymentLabels = { CASH: "Tiền mặt", CARD: "Thẻ ngân hàng", QR: "QR Code" };
  const dateTime = (value) => value ? new Date(value).toLocaleString("vi-VN") : "-";

  async function loadReceipt() {
    if (!orderId) {
      content.innerHTML = "";
      error.textContent = "Thiếu mã đơn hàng để tải hóa đơn.";
      error.classList.remove("d-none");
      return;
    }
    try {
      const response = await apiGet(`/api/orders/${encodeURIComponent(orderId)}`);
      if (!response.success) throw new Error(response.message || "Không tìm thấy đơn hàng");
      const order = response.data;
      content.innerHTML = `
        <header class="text-center pb-3 mb-3 border-bottom">
          <div class="fw-bold fs-4">POS APP</div>
          <div class="small text-muted">HÓA ĐƠN THANH TOÁN</div>
          <div class="small text-muted mt-2">Mã đơn: <strong>${order.orderCode}</strong></div>
          <div class="small text-muted">Thời gian: ${dateTime(order.paidAt || order.createdAt)}</div>
        </header>
        <div class="d-flex justify-content-between small mb-3"><span>Khách hàng</span><strong>${order.customerName || "Khách lẻ"}</strong></div>
        <div class="d-flex justify-content-between small mb-3"><span>Nhân viên phụ trách</span><strong>${order.employeeName || "-"}</strong></div>
        <div class="d-flex justify-content-between small mb-3"><span>Thanh toán</span><strong>${paymentLabels[order.paymentMethod] || "-"}</strong></div>
        <div class="d-flex justify-content-between small fw-semibold border-bottom pb-2 mb-1"><span>Món hàng</span><span>Thành tiền</span></div>
        ${(order.items || []).map((item) => `
          <div class="receipt-line d-flex justify-content-between gap-3 py-2 small">
            <span>${item.productName} × ${item.quantity}${item.note ? `<br><span class="text-muted">${item.note}</span>` : ""}</span>
            <span class="text-nowrap">${formatVnd(item.lineTotal)}</span>
          </div>`).join("")}
        <div class="border-top mt-2 pt-3">
          <div class="d-flex justify-content-between small mb-2"><span>Tạm tính</span><span>${formatVnd(order.subtotal)}</span></div>
          <div class="d-flex justify-content-between small mb-2"><span>Giảm giá</span><span>-${formatVnd(order.discountAmount)}</span></div>
          <div class="d-flex justify-content-between small mb-2"><span>VAT</span><span>${formatVnd(order.taxAmount)}</span></div>
          <div class="d-flex justify-content-between fw-bold fs-5 pt-2 border-top"><span>Tổng thanh toán</span><span>${formatVnd(order.totalAmount)}</span></div>
        </div>
        <p class="text-center text-muted small mt-4 mb-0">Cảm ơn quý khách và hẹn gặp lại!</p>`;
    } catch (e) {
      content.innerHTML = "";
      error.textContent = e.message;
      error.classList.remove("d-none");
    }
  }

  document.getElementById("printReceiptBtn").addEventListener("click", () => window.print());
  loadReceipt();
})();
