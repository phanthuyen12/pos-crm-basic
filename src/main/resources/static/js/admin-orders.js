window.initAdminOrders = function () {
  const tableBody = document.getElementById("orderTableBody");
  const modalEl = document.getElementById("orderModal");
  const modal = modalEl ? bootstrap.Modal.getOrCreateInstance(modalEl) : null;
  const editModalEl = document.getElementById("orderEditModal");
  const editModal = editModalEl ? bootstrap.Modal.getOrCreateInstance(editModalEl) : null;
  const editBody = document.getElementById("editOrderBody");
  let products = [];
  let editingOrder = null;

  function statusBadge(status) {
    if (status === "COMPLETED") return '<span class="badge-status bg-success-subtle text-success">Hoàn thành</span>';
    if (status === "PENDING") return '<span class="badge-status bg-warning-subtle text-warning">Đang xử lý</span>';
    if (status === "CANCELLED") return '<span class="badge-status bg-danger-subtle text-danger">Đã hủy</span>';
    return '<span class="badge-status bg-secondary-subtle text-secondary">-</span>';
  }

  function paymentLabel(method) {
    if (method === "CASH") return '<i class="bi bi-cash-coin text-muted me-1"></i>Tiền mặt';
    if (method === "CARD") return '<i class="bi bi-credit-card text-muted me-1"></i>Thẻ';
    if (method === "QR") return '<i class="bi bi-qr-code text-muted me-1"></i>QR Code';
    return '<span class="text-muted">-</span>';
  }

  function formatDateTime(dt) {
    if (!dt) return "-";
    const d = new Date(dt);
    return d.toLocaleDateString("vi-VN") + " " + d.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
  }

  async function load() {
    try {
      const resp = await apiGet("/api/orders");
      const orders = resp.success ? (resp.data || []) : [];
      render(orders);
    } catch (e) {
      render([]);
    }
  }

  function render(orders) {
    if (!tableBody) return;
    if (orders.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-4">Chưa có đơn hàng nào</td></tr>`;
      return;
    }
    tableBody.innerHTML = orders.map((o) => `
      <tr>
        <td class="fw-semibold">#${o.orderCode}</td>
        <td>${o.customerName || "Khách lẻ"}</td>
        <td class="text-muted">${formatDateTime(o.createdAt)}</td>
        <td>${(o.items || []).reduce((s, i) => s + i.quantity, 0)}</td>
        <td class="fw-semibold">${formatVnd(o.totalAmount)}</td>
        <td>${paymentLabel(o.paymentMethod)}</td>
        <td>${statusBadge(o.status)}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-light" data-action="view" data-id="${o.id}"><i class="bi bi-eye"></i></button>
          ${o.status === "PENDING" ? `<button class="btn btn-sm btn-light text-primary" data-action="edit" data-id="${o.id}" title="Cập nhật đơn"><i class="bi bi-pencil"></i></button>` : ""}
          ${o.status !== "COMPLETED" ? `<button class="btn btn-sm btn-light text-success" data-action="pay" data-id="${o.id}" title="Thanh toán"><i class="bi bi-check-lg"></i></button>` : ""}
          ${o.status !== "CANCELLED" && o.status !== "COMPLETED" ? `<button class="btn btn-sm btn-light text-danger" data-action="cancel" data-id="${o.id}" title="Hủy"><i class="bi bi-x-lg"></i></button>` : ""}
        </td>
      </tr>`).join("");
  }

  async function view(id) {
    try {
      const resp = await apiGet(`/api/orders/${id}`);
      if (!resp.success) { alert(resp.message); return; }
      const o = resp.data;
      const body = modalEl.querySelector(".modal-body");
      const title = modalEl.querySelector(".modal-title");
      if (title) title.textContent = `Chi tiết đơn #${o.orderCode}`;
      if (body) {
        body.innerHTML = `
          <div class="d-flex justify-content-between mb-3">
            <div>
              <p class="mb-0 fw-semibold">${o.customerName || "Khách lẻ"}</p>
              <small class="text-muted">${formatDateTime(o.createdAt)}</small>
            </div>
            ${statusBadge(o.status)}
          </div>
          <table class="table table-sm mb-3">
            <tbody>
              ${(o.items || []).map((i) => `<tr><td>${i.productName} x${i.quantity}</td><td class="text-end">${formatVnd(i.lineTotal)}</td></tr>`).join("")}
            </tbody>
          </table>
          <div class="d-flex justify-content-between small text-muted mb-1"><span>Tạm tính</span><span>${formatVnd(o.subtotal)}</span></div>
          <div class="d-flex justify-content-between small text-muted mb-1"><span>Giảm giá</span><span>-${formatVnd(o.discountAmount)}</span></div>
          <div class="d-flex justify-content-between small text-muted mb-1"><span>Thuế VAT (8%)</span><span>${formatVnd(o.taxAmount)}</span></div>
          <div class="d-flex justify-content-between fw-bold fs-5"><span>Tổng cộng</span><span>${formatVnd(o.totalAmount)}</span></div>`;
      }
      modal.show();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  async function pay(id) {
    if (!confirm("Xác nhận thanh toán đơn hàng này?")) return;
    try {
      const resp = await apiPost(`/api/orders/${id}/pay`, { paymentMethod: "CASH" });
      if (!resp.success) { alert(resp.message); return; }
      load();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  async function cancel(id) {
    if (!confirm("Hủy đơn hàng này?")) return;
    try {
      const resp = await apiPost(`/api/orders/${id}/cancel`, null);
      if (!resp.success) { alert(resp.message); return; }
      load();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  async function loadProducts() {
    const response = await apiGet("/api/products?activeOnly=true");
    if (!response.success) throw new Error(response.message || "Không tải được danh sách sản phẩm");
    products = response.data || [];
  }

  function renderEditItem(item, index) {
    const options = products.map((product) =>
      `<option value="${product.id}" ${Number(product.id) === Number(item.productId) ? "selected" : ""}>${product.name} — ${formatVnd(product.price)}</option>`
    ).join("");
    return `<div class="row g-2 align-items-end mb-2 edit-order-item">
      <div class="col-md-6"><label class="form-label small">Sản phẩm</label><select class="form-select form-select-sm" data-field="productId">${options}</select></div>
      <div class="col-4 col-md-2"><label class="form-label small">Số lượng</label><input class="form-control form-control-sm" type="number" min="1" value="${item.quantity || 1}" data-field="quantity"></div>
      <div class="col-6 col-md-3"><label class="form-label small">Ghi chú</label><input class="form-control form-control-sm" value="${item.note || ""}" data-field="note"></div>
      <div class="col-2 col-md-1"><button class="btn btn-sm btn-outline-danger w-100" type="button" data-action="remove-item" title="Bỏ món"><i class="bi bi-trash"></i></button></div>
    </div>`;
  }

  async function edit(id) {
    try {
      const [orderResponse] = await Promise.all([apiGet(`/api/orders/${id}`), loadProducts()]);
      if (!orderResponse.success) { alert(orderResponse.message); return; }
      editingOrder = orderResponse.data;
      const order = editingOrder;
      editBody.innerHTML = `
        <div class="alert alert-info py-2">Chỉ đơn đang chờ thanh toán mới được cập nhật. Tồn kho sẽ được trừ khi thanh toán.</div>
        <div class="mb-3"><label class="form-label small fw-semibold">Loại đơn</label>
          <select class="form-select" id="editOrderType">
            <option value="TAKEAWAY" ${order.orderType === "TAKEAWAY" ? "selected" : ""}>Mang đi</option>
            <option value="DINE_IN" ${order.orderType === "DINE_IN" ? "selected" : ""}>Tại quán</option>
            <option value="DELIVERY" ${order.orderType === "DELIVERY" ? "selected" : ""}>Giao hàng</option>
          </select>
        </div>
        <div id="editOrderItems">${(order.items || []).map(renderEditItem).join("")}</div>
        <div class="row g-2 align-items-end mt-2">
          <div class="col"><label class="form-label small">Thêm sản phẩm</label>
            <select class="form-select form-select-sm" id="newOrderProduct">${products.map((p) => `<option value="${p.id}">${p.name} — ${formatVnd(p.price)}</option>`).join("")}</select>
          </div>
          <div class="col-auto"><button class="btn btn-sm btn-outline-brand" type="button" id="addOrderItemBtn"><i class="bi bi-plus-lg me-1"></i>Thêm món</button></div>
        </div>
        <div class="mt-3"><label class="form-label small fw-semibold">Ghi chú đơn</label><textarea class="form-control" id="editOrderNote" rows="2">${order.note || ""}</textarea></div>`;
      editModal.show();
    } catch (e) { alert("Không mở được đơn để cập nhật: " + e.message); }
  }

  async function saveEdit() {
    if (!editingOrder) return;
    const itemRows = [...editBody.querySelectorAll(".edit-order-item")];
    const items = itemRows.map((row) => ({
      productId: Number(row.querySelector('[data-field="productId"]').value),
      quantity: Number(row.querySelector('[data-field="quantity"]').value),
      note: row.querySelector('[data-field="note"]').value || null,
    }));
    const payload = {
      customerId: editingOrder.customerId || null,
      employeeId: editingOrder.employeeId || null,
      voucherId: editingOrder.voucherId || null,
      orderType: document.getElementById("editOrderType").value,
      note: document.getElementById("editOrderNote").value || null,
      items,
    };
    try {
      const response = await apiPut(`/api/orders/${editingOrder.id}`, payload);
      if (!response.success) { alert(response.message || "Không cập nhật được đơn hàng"); return; }
      editModal.hide();
      editingOrder = null;
      load();
    } catch (e) { alert("Lỗi cập nhật đơn hàng: " + e.message); }
  }

  if (tableBody) {
    tableBody.addEventListener("click", (e) => {
      const btn = e.target.closest("button[data-action]");
      if (!btn) return;
      if (btn.dataset.action === "view") view(btn.dataset.id);
      else if (btn.dataset.action === "edit") edit(btn.dataset.id);
      else if (btn.dataset.action === "pay") pay(btn.dataset.id);
      else if (btn.dataset.action === "cancel") cancel(btn.dataset.id);
    });
  }

  editBody.addEventListener("click", (e) => {
    if (e.target.closest("[data-action='remove-item']")) {
      e.target.closest(".edit-order-item").remove();
    }
    if (e.target.closest("#addOrderItemBtn")) {
      const productId = Number(document.getElementById("newOrderProduct").value);
      const product = products.find((p) => Number(p.id) === productId);
      if (!product) return;
      document.getElementById("editOrderItems").insertAdjacentHTML("beforeend", renderEditItem({
        productId: product.id,
        quantity: 1,
        note: "",
      }, 0));
    }
  });

  document.getElementById("saveOrderChangesBtn").addEventListener("click", saveEdit);

  load();
};
