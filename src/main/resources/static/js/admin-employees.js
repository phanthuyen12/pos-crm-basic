window.initAdminEmployees = function () {
  const tableBody = document.getElementById("employeeTableBody");
  const modalEl = document.getElementById("employeeModal");
  const modal = modalEl ? bootstrap.Modal.getOrCreateInstance(modalEl) : null;

  const roleLabels = { MANAGER: "Quản lý", CASHIER: "Thu ngân", WAITER: "Phục vụ", KITCHEN: "Bếp" };

  let employees = [];

  function statusBadge(status) {
    return status === "ACTIVE"
      ? '<span class="badge-status bg-success-subtle text-success">Đang làm</span>'
      : '<span class="badge-status bg-danger-subtle text-danger">Nghỉ việc</span>';
  }

  function formatDate(d) {
    if (!d) return "-";
    const dt = new Date(d);
    return dt.toLocaleDateString("vi-VN");
  }

  async function load() {
    try {
      const resp = await apiGet("/api/people?type=EMPLOYEE");
      employees = resp.success ? (resp.data || []) : [];
      render();
    } catch (e) {
      employees = [];
      render();
    }
  }

  function render() {
    if (!tableBody) return;
    if (employees.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">Chưa có nhân viên nào</td></tr>`;
      return;
    }
    tableBody.innerHTML = employees.map((e) => `
      <tr>
        <td class="fw-semibold">${e.employeeCode || "-"}</td>
        <td>
          <div class="d-flex align-items-center gap-2">
            <div class="mini-icon icon-blue"><i class="bi bi-person"></i></div>
            <span class="fw-semibold">${e.fullName || ""}</span>
          </div>
        </td>
        <td>${roleLabels[e.role] || "-"}</td>
        <td>${e.phone || "-"}</td>
        <td class="text-muted">${formatDate(e.hireDate)}</td>
        <td>${statusBadge(e.status)}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-light" data-action="edit" data-id="${e.id}"><i class="bi bi-pencil"></i></button>
          <button class="btn btn-sm btn-light text-danger" data-action="delete" data-id="${e.id}"><i class="bi bi-trash3"></i></button>
        </td>
      </tr>`).join("");
  }

  function openCreate() {
    document.getElementById("employeeId").value = "";
    document.getElementById("employeeName").value = "";
    document.getElementById("employeePhone").value = "";
    document.getElementById("employeeRole").value = "CASHIER";
    document.getElementById("employeeHireDate").value = "";
    document.getElementById("employeeStatus").value = "ACTIVE";
    modal.show();
  }

  function openEdit(id) {
    const e = employees.find((x) => x.id === Number(id));
    if (!e) return;
    document.getElementById("employeeId").value = e.id;
    document.getElementById("employeeName").value = e.fullName || "";
    document.getElementById("employeePhone").value = e.phone || "";
    document.getElementById("employeeRole").value = e.role || "CASHIER";
    document.getElementById("employeeHireDate").value = e.hireDate || "";
    document.getElementById("employeeStatus").value = e.status || "ACTIVE";
    modal.show();
  }

  async function save() {
    const id = document.getElementById("employeeId").value;
    const existing = id ? employees.find((employee) => employee.id === Number(id)) : null;
    const code = existing ? existing.employeeCode : "NV" + String(Date.now()).slice(-3);
    const payload = {
      type: "EMPLOYEE",
      fullName: document.getElementById("employeeName").value,
      phone: document.getElementById("employeePhone").value,
      employeeCode: code,
      role: document.getElementById("employeeRole").value,
      hireDate: document.getElementById("employeeHireDate").value || null,
      status: document.getElementById("employeeStatus").value,
    };
    try {
      const resp = id
        ? await apiPut(`/api/people/${id}`, payload)
        : await apiPost("/api/people", payload);
      if (!resp.success) { alert(resp.message || "Lỗi lưu nhân viên"); return; }
      modal.hide();
      load();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  async function remove(id) {
    if (!confirm("Xóa nhân viên này?")) return;
    try {
      const resp = await apiDelete(`/api/people/${id}`);
      if (!resp.success) { alert(resp.message); return; }
      load();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  const addBtn = document.querySelector('[data-bs-toggle="modal"][data-bs-target="#employeeModal"]');
  if (addBtn) addBtn.addEventListener("click", openCreate);

  if (tableBody) {
    tableBody.addEventListener("click", (e) => {
      const btn = e.target.closest("button[data-action]");
      if (!btn) return;
      if (btn.dataset.action === "edit") openEdit(btn.dataset.id);
      else if (btn.dataset.action === "delete") remove(btn.dataset.id);
    });
  }

  const saveBtn = document.getElementById("saveEmployeeBtn");
  if (saveBtn) saveBtn.addEventListener("click", save);

  load();
};
