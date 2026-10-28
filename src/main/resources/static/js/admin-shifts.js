window.initAdminShifts = function () {
  const shiftCards = document.getElementById("shiftCards");
  const scheduleBody = document.getElementById("scheduleTableBody");
  const scheduleDate = document.getElementById("scheduleDate");
  const shiftTemplateModalEl = document.getElementById("shiftTemplateModal");
  const assignModalEl = document.getElementById("assignShiftModal");
  const shiftTemplateModal = shiftTemplateModalEl ? bootstrap.Modal.getOrCreateInstance(shiftTemplateModalEl) : null;
  const assignModal = assignModalEl ? bootstrap.Modal.getOrCreateInstance(assignModalEl) : null;

  let shifts = [];
  let employees = [];

  const icons = ["bi-sunrise", "bi-brightness-high", "bi-moon-stars"];
  const iconClasses = ["icon-amber", "icon-blue", "icon-rose"];

  function todayStr() {
    const d = new Date();
    return d.toISOString().slice(0, 10);
  }

  function formatTime(t) {
    return t ? String(t).slice(0, 5) : "";
  }

  function statusBadge(status) {
    if (status === "COMPLETED") return '<span class="badge-status bg-secondary-subtle text-secondary">Đã hoàn thành</span>';
    if (status === "IN_PROGRESS") return '<span class="badge-status bg-success-subtle text-success">Đang làm</span>';
    return '<span class="badge-status bg-warning-subtle text-warning">Chưa bắt đầu</span>';
  }

  async function loadShifts() {
    try {
      const resp = await apiGet("/api/shifts");
      shifts = resp.success ? (resp.data || []) : [];
      renderShiftCards();
    } catch (e) { shifts = []; renderShiftCards(); }
  }

  async function loadEmployees() {
    try {
      const resp = await apiGet("/api/people?type=EMPLOYEE");
      employees = resp.success ? (resp.data || []) : [];
      const sel = document.getElementById("assignEmployee");
      if (sel) {
        sel.innerHTML = employees.map((e) => `<option value="${e.id}">${e.fullName || ""}</option>`).join("");
      }
    } catch (e) { /* ignore */ }
  }

  async function loadSchedule() {
    if (!scheduleBody) return;
    const date = scheduleDate && scheduleDate.value ? scheduleDate.value : todayStr();
    try {
      const resp = await apiGet(`/api/shift-assignments?workDate=${date}`);
      const assignments = resp.success ? (resp.data || []) : [];
      renderSchedule(assignments);
    } catch (e) { scheduleBody.innerHTML = ""; }
  }

  function renderShiftCards() {
    if (!shiftCards) return;
    if (shifts.length === 0) {
      shiftCards.innerHTML = `<div class="col-12 text-center text-muted py-4">Chưa có ca làm việc nào</div>`;
      return;
    }
    shiftCards.innerHTML = shifts.map((s, i) => {
      const ic = icons[i % icons.length];
      const icc = iconClasses[i % iconClasses.length];
      return `
      <div class="col-md-4">
        <div class="card p-3 h-100">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <div class="mini-icon ${icc}"><i class="bi ${ic}"></i></div>
            <span class="badge-status ${s.active ? "bg-success-subtle text-success" : "bg-secondary-subtle text-secondary"}">${s.active ? "Đang áp dụng" : "Tạm ngưng"}</span>
          </div>
          <h6 class="fw-bold mb-1">${s.name}</h6>
          <p class="text-muted mb-0" style="font-size: 0.82rem;">${formatTime(s.startTime)} — ${formatTime(s.endTime)} · Cần ${s.requiredStaff} nhân viên</p>
        </div>
      </div>`;
    }).join("");
  }

  function renderSchedule(assignments) {
    if (!scheduleBody) return;
    if (assignments.length === 0) {
      scheduleBody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">Chưa có phân ca trong ngày này</td></tr>`;
      return;
    }
    scheduleBody.innerHTML = assignments.map((a) => `
      <tr>
        <td class="fw-semibold">${a.employeeName || "-"}</td>
        <td>${a.shiftName || "-"}</td>
        <td class="text-muted">${a.workDate}</td>
        <td class="text-muted">${formatTime(a.checkInTime) || "-"}</td>
        <td>${statusBadge(a.status)}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-light" data-action="checkin" data-id="${a.id}" title="Chấm công vào"><i class="bi bi-box-arrow-in-right"></i></button>
          <button class="btn btn-sm btn-light" data-action="checkout" data-id="${a.id}" title="Chấm công ra"><i class="bi bi-box-arrow-right"></i></button>
          <button class="btn btn-sm btn-light text-danger" data-action="delete" data-id="${a.id}"><i class="bi bi-trash3"></i></button>
        </td>
      </tr>`).join("");
  }

  function openShiftModal() {
    document.getElementById("shiftId").value = "";
    document.getElementById("shiftName").value = "";
    document.getElementById("shiftStart").value = "06:00";
    document.getElementById("shiftEnd").value = "14:00";
    document.getElementById("shiftRequired").value = "3";
    document.getElementById("shiftActive").value = "true";
    shiftTemplateModal.show();
  }

  async function saveShift() {
    const id = document.getElementById("shiftId").value;
    const payload = {
      name: document.getElementById("shiftName").value,
      startTime: document.getElementById("shiftStart").value,
      endTime: document.getElementById("shiftEnd").value,
      requiredStaff: document.getElementById("shiftRequired").value,
      active: document.getElementById("shiftActive").value === "true",
    };
    try {
      const resp = id
        ? await apiPut(`/api/shifts/${id}`, payload)
        : await apiPost("/api/shifts", payload);
      if (!resp.success) { alert(resp.message || "Lỗi lưu ca"); return; }
      shiftTemplateModal.hide();
      loadShifts();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  async function openAssignModal() {
    // cập nhật danh sách ca + ngày hiện tại
    try {
      const resp = await apiGet("/api/shifts");
      shifts = resp.success ? (resp.data || []) : [];
    } catch (e) { /* ignore */ }
    const sel = document.getElementById("assignShift");
    if (sel) {
      sel.innerHTML = shifts.map((s) => `<option value="${s.id}">${s.name} (${formatTime(s.startTime)} - ${formatTime(s.endTime)})</option>`).join("");
    }
    const dateInput = document.getElementById("assignDate");
    if (dateInput) dateInput.value = todayStr();
    assignModal.show();
  }

  async function saveAssign() {
    const payload = {
      employeeId: document.getElementById("assignEmployee").value,
      shiftId: document.getElementById("assignShift").value,
      workDate: document.getElementById("assignDate").value,
    };
    try {
      const resp = await apiPost("/api/shift-assignments", payload);
      if (!resp.success) { alert(resp.message || "Lỗi phân ca"); return; }
      assignModal.hide();
      loadSchedule();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  async function checkIn(id) {
    try {
      const resp = await apiPost(`/api/shift-assignments/${id}/check-in`, null);
      if (!resp.success) { alert(resp.message); return; }
      loadSchedule();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  async function checkOut(id) {
    try {
      const resp = await apiPost(`/api/shift-assignments/${id}/check-out`, null);
      if (!resp.success) { alert(resp.message); return; }
      loadSchedule();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  async function removeAssign(id) {
    if (!confirm("Xóa phân ca này?")) return;
    try {
      const resp = await apiDelete(`/api/shift-assignments/${id}`);
      if (!resp.success) { alert(resp.message); return; }
      loadSchedule();
    } catch (e) { alert("Lỗi: " + e.message); }
  }

  // bind
  const createShiftBtn = document.querySelector('[data-bs-target="#shiftTemplateModal"]');
  if (createShiftBtn) createShiftBtn.addEventListener("click", openShiftModal);

  const assignBtn = document.querySelector('[data-bs-target="#assignShiftModal"]');
  if (assignBtn) assignBtn.addEventListener("click", openAssignModal);

  document.getElementById("saveShiftBtn").addEventListener("click", saveShift);
  document.getElementById("saveAssignBtn").addEventListener("click", saveAssign);

  if (scheduleDate) scheduleDate.addEventListener("change", loadSchedule);
  if (scheduleBody) {
    scheduleBody.addEventListener("click", (e) => {
      const btn = e.target.closest("button[data-action]");
      if (!btn) return;
      if (btn.dataset.action === "checkin") checkIn(btn.dataset.id);
      else if (btn.dataset.action === "checkout") checkOut(btn.dataset.id);
      else if (btn.dataset.action === "delete") removeAssign(btn.dataset.id);
    });
  }

  if (scheduleDate) scheduleDate.value = todayStr();

  loadShifts();
  loadEmployees();
  loadSchedule();
};