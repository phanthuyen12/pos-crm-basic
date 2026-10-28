window.initAdminInventory = function () {
  const stockTableBody = document.getElementById("stockTableBody");
  const historyTableBody = document.getElementById("historyTableBody");
  const modalEl = document.getElementById("stockInModal");
  const modal = modalEl ? bootstrap.Modal.getOrCreateInstance(modalEl) : null;

  let products = [];
  let employees = [];
  let movements = [];

  function stockStatus(p) {
    if (p.stockQuantity <= 0) return '<span class="badge-status bg-danger-subtle text-danger">Hết hàng</span>';
    if (p.stockQuantity <= (p.lowStockThreshold || 10)) return '<span class="badge-status bg-warning-subtle text-warning">Sắp hết</span>';
    return '<span class="badge-status bg-success-subtle text-success">Còn hàng</span>';
  }

  function updateStats() {
    const tracking = products.length;
    const low = products.filter((p) => p.stockQuantity > 0 && p.stockQuantity <= (p.lowStockThreshold || 10)).length;
    const out = products.filter((p) => p.stockQuantity <= 0).length;
    const stockIn = movements.filter((m) => m.movementType === "IN").length;

    const set = (id, v) => { const el = document.getElementById(id); if (el) el.textContent = v; };
    set("statTracking", tracking);
    set("statLow", low);
    set("statOut", out);
    set("statStockIn", stockIn);
  }

  async function loadProducts() {
    try {
      const resp = await apiGet("/api/products");
      products = resp.success ? (resp.data || []) : [];
      renderStock();
      updateStats();
    } catch (e) { products = []; renderStock(); }
  }

  async function loadEmployees() {
    try {
      const resp = await apiGet("/api/people?type=EMPLOYEE");
      employees = resp.success ? (resp.data || []) : [];
    } catch (e) { employees = []; }
  }

  async function loadMovements() {
    try {
      const resp = await apiGet("/api/stock-movements");
      movements = resp.success ? (resp.data || []) : [];
      renderHistory();
      updateStats();
    } catch (e) { movements = []; renderHistory(); }
  }

  function renderStock() {
    if (!stockTableBody) return;
    if (products.length === 0) {
      stockTableBody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">Chưa có sản phẩm</td></tr>`;
      return;
    }
    stockTableBody.innerHTML = products.map((p) => `
      <tr>
        <td>
          <div class="d-flex align-items-center gap-2">
            <div class="product-thumb d-flex align-items-center justify-content-center text-brand"><i class="bi ${p.icon || "bi-box"}"></i></div>
            <span class="fw-semibold">${p.name}</span>
          </div>
        </td>
        <td>${p.categoryName || "-"}</td>
        <td class="fw-semibold">${p.stockQuantity}</td>
        <td class="text-muted">${p.lowStockThreshold}</td>
        <td>${stockStatus(p)}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-light" data-action="stock" data-id="${p.id}"><i class="bi bi-plus-lg"></i></button>
        </td>
      </tr>`).join("");
  }

  function renderHistory() {
    if (!historyTableBody) return;
    if (movements.length === 0) {
      historyTableBody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">Chưa có phiếu nhập/xuất</td></tr>`;
      return;
    }
    historyTableBody.innerHTML = movements.slice(0, 20).map((m) => `
      <tr>
        <td class="text-muted">${m.createdAt ? new Date(m.createdAt).toLocaleDateString("vi-VN") : "-"}</td>
        <td class="fw-semibold">${m.productName || "-"}</td>
        <td>${m.movementType === "IN" ? "+" : "-"}${m.quantity}</td>
        <td>${m.supplier || "-"}</td>
        <td>${m.createdByName || "-"}</td>
      </tr>`).join("");
  }

  function openStockModal(productId) {
    const productSel = document.getElementById("stockProduct");
    if (productSel) {
      productSel.innerHTML = products.map((p) =>
        `<option value="${p.id}" ${Number(p.id) === Number(productId) ? "selected" : ""}>${p.name}</option>`).join("");
    }
    const empSel = document.getElementById("stockEmp");
    if (empSel) {
      empSel.innerHTML = '<option value="">- Chọn nhân viên -</option>' +
        employees.map((e) => `<option value="${e.id}">${e.fullName || ""}</option>`).join("");
    }
    document.getElementById("stockType").value = "IN";
    document.getElementById("stockQty").value = "";
    document.getElementById("stockSupplier").value = "";
    document.getElementById("stockNote").value = "";
    modal.show();
  }

  async function saveStock() {
    const payload = {
      productId: document.getElementById("stockProduct").value,
      movementType: document.getElementById("stockType").value,
      quantity: document.getElementById("stockQty").value,
      supplier: document.getElementById("stockSupplier").value || null,
      note: document.getElementById("stockNote").value || null,
      createdById: document.getElementById("stockEmp").value || null,
    };
    try {
      const resp = await apiPost("/api/stock-movements", payload);
      if (!resp.success) { alert(resp.message || "Lỗi lưu phiếu"); return; }
      modal.hide();
      loadProducts();
      loadMovements();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  const addBtn = document.querySelector('[data-bs-target="#stockInModal"]');
  if (addBtn) addBtn.addEventListener("click", () => openStockModal(null));

  if (stockTableBody) {
    stockTableBody.addEventListener("click", (e) => {
      const btn = e.target.closest("button[data-action]");
      if (btn) openStockModal(btn.dataset.id);
    });
  }

  document.getElementById("saveStockBtn").addEventListener("click", saveStock);

  loadProducts();
  loadEmployees();
  loadMovements();
};