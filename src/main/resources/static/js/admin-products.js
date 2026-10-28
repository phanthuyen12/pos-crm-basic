window.initAdminProducts = function () {
  const tableBody = document.getElementById("productTableBody");
  const categorySelect = document.getElementById("productCategory");
  const modalEl = document.getElementById("productModal");
  const modal = modalEl ? bootstrap.Modal.getOrCreateInstance(modalEl) : null;

  let currentProducts = [];

  async function loadCategories() {
    try {
      const resp = await apiGet("/api/categories");
      if (resp.success && categorySelect) {
        categorySelect.innerHTML = '<option value="">Chọn danh mục</option>' +
          resp.data.map((c) => `<option value="${c.id}">${c.name}</option>`).join("");
      }
    } catch (e) { /* ignore */ }
  }

  function statusBadge(status) {
    return status === "ACTIVE"
      ? '<span class="badge-status bg-success-subtle text-success">Đang bán</span>'
      : '<span class="badge-status bg-secondary-subtle text-secondary">Ngừng bán</span>';
  }

  function stockBadge(qty, threshold) {
    if (qty <= 0) return '<span class="badge-status bg-danger-subtle text-danger">Hết hàng</span>';
    if (qty <= (threshold || 10)) return '<span class="badge-status bg-warning-subtle text-warning">Sắp hết</span>';
    return '<span class="badge-status bg-success-subtle text-success">Còn hàng</span>';
  }

  async function loadProducts() {
    try {
      const resp = await apiGet("/api/products");
      currentProducts = resp.success ? (resp.data || []) : [];
      render();
    } catch (e) {
      currentProducts = [];
      render();
    }
  }

  function render() {
    if (!tableBody) return;
    if (currentProducts.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">Chưa có sản phẩm nào</td></tr>`;
      return;
    }
    tableBody.innerHTML = currentProducts.map((p) => `
      <tr>
        <td><input type="checkbox" class="form-check-input" /></td>
        <td>
          <div class="d-flex align-items-center gap-2">
            <div class="product-thumb d-flex align-items-center justify-content-center text-brand"><i class="bi ${p.icon || "bi-box"}"></i></div>
            <span class="fw-semibold">${p.name}</span>
          </div>
        </td>
        <td>${p.categoryName || "-"}</td>
        <td class="fw-semibold">${formatVnd(p.price)}</td>
        <td>${p.stockQuantity}</td>
        <td>${stockBadge(p.stockQuantity, p.lowStockThreshold)}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-light" data-action="edit" data-id="${p.id}"><i class="bi bi-pencil"></i></button>
          <button class="btn btn-sm btn-light text-danger" data-action="delete" data-id="${p.id}"><i class="bi bi-trash3"></i></button>
        </td>
      </tr>`).join("");
  }

  function openCreate() {
    document.getElementById("productId").value = "";
    document.getElementById("productName").value = "";
    document.getElementById("productPrice").value = "";
    document.getElementById("productStock").value = "";
    document.getElementById("productStatus").value = "ACTIVE";
    document.getElementById("productCategory").value = "";
    modal.show();
  }

  function openEdit(id) {
    const p = currentProducts.find((x) => x.id === Number(id));
    if (!p) return;
    document.getElementById("productId").value = p.id;
    document.getElementById("productName").value = p.name;
    document.getElementById("productPrice").value = p.price;
    document.getElementById("productStock").value = p.stockQuantity;
    document.getElementById("productStatus").value = p.status;
    document.getElementById("productCategory").value = p.categoryId || "";
    modal.show();
  }

  async function save() {
    const id = document.getElementById("productId").value;
    const payload = {
      name: document.getElementById("productName").value,
      categoryId: document.getElementById("productCategory").value || null,
      price: document.getElementById("productPrice").value,
      stockQuantity: document.getElementById("productStock").value,
      status: document.getElementById("productStatus").value,
    };
    try {
      const resp = id
        ? await apiPut(`/api/products/${id}`, payload)
        : await apiPost("/api/products", payload);
      if (!resp.success) { alert(resp.message || "Lỗi lưu sản phẩm"); return; }
      modal.hide();
      loadProducts();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  async function remove(id) {
    if (!confirm("Xóa sản phẩm này?")) return;
    try {
      const resp = await apiDelete(`/api/products/${id}`);
      if (!resp.success) { alert(resp.message); return; }
      loadProducts();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  // Add product button
  const addBtn = document.querySelector('[data-bs-toggle="modal"][data-bs-target="#productModal"]');
  if (addBtn) addBtn.addEventListener("click", openCreate);

  // Table actions delegate
  if (tableBody) {
    tableBody.addEventListener("click", (e) => {
      const btn = e.target.closest("button[data-action]");
      if (!btn) return;
      if (btn.dataset.action === "edit") openEdit(btn.dataset.id);
      else if (btn.dataset.action === "delete") remove(btn.dataset.id);
    });
  }

  const saveBtn = document.getElementById("saveProductBtn");
  if (saveBtn) saveBtn.addEventListener("click", save);

  loadCategories();
  loadProducts();
};