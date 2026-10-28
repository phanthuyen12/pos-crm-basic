/**
 * Layout loader: injects the shared sidebar/topbar partials so each page
 * only needs to contain its own content, not the repeated chrome markup.
 */
function initLayout(type) {
  const user = guardPage(type === "client" ? "pos" : "admin");
  if (!user) return Promise.reject(new Error("Chưa đăng nhập hoặc không đủ quyền"));

  const sidebarSlot = document.getElementById("layout-sidebar");
  const topbarSlot = document.getElementById("layout-topbar");
  const topbarFile = type === "client" ? "/partials/client-topbar.html" : "/partials/admin-topbar.html";

  const jobs = [];
  if (sidebarSlot) {
    jobs.push(
      fetch("/partials/admin-sidebar.html")
        .then((res) => res.text())
        .then((html) => (sidebarSlot.outerHTML = html))
    );
  }
  if (topbarSlot) {
    jobs.push(
      fetch(topbarFile)
        .then((res) => res.text())
        .then((html) => (topbarSlot.outerHTML = html))
    );
  }

  return Promise.all(jobs).then(() => {
    document.querySelectorAll("[data-page-title]").forEach((el) => {
      el.textContent = window.PAGE_TITLE || "";
    });
    populateCurrentUser(user);
    document.querySelectorAll("[data-logout]").forEach((link) => {
      link.addEventListener("click", (event) => {
        event.preventDefault();
        clearCurrentUser();
        window.location.href = "/login.html";
      });
    });
    highlightActiveNav();
    wireSidebarToggle();
  });
}

function populateCurrentUser(user) {
  const roleNames = {
    MANAGER: "Quản lý",
    CASHIER: "Thu ngân",
    WAITER: "Phục vụ",
    KITCHEN: "Bếp",
  };
  const initials = (user.fullName || user.employeeCode || "NV")
    .trim()
    .split(/\s+/)
    .slice(-2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
  document.querySelectorAll("[data-user-name]").forEach((el) => {
    el.textContent = user.fullName || user.employeeCode;
  });
  document.querySelectorAll("[data-user-role]").forEach((el) => {
    el.textContent = roleNames[user.role] || user.role || "Nhân viên";
  });
  document.querySelectorAll("[data-user-avatar]").forEach((el) => {
    el.textContent = initials;
  });
  document.querySelectorAll("[data-admin-link]").forEach((el) => {
    el.classList.toggle("d-none", !isAdmin(user));
  });
}

function highlightActiveNav() {
  const path = window.location.pathname;
  document.querySelectorAll(".sidebar-nav .nav-link").forEach((link) => {
    if (link.getAttribute("href") === path) {
      link.classList.add("active");
    }
  });
}

function wireSidebarToggle() {
  const toggleBtn = document.querySelector("[data-sidebar-toggle]");
  const overlay = document.querySelector(".sidebar-overlay");

  if (toggleBtn) {
    toggleBtn.addEventListener("click", () => {
      document.body.classList.toggle("sidebar-open");
    });
  }
  if (overlay) {
    overlay.addEventListener("click", () => {
      document.body.classList.remove("sidebar-open");
    });
  }
}

/**
 * Lightweight, non-blocking notification — built on Bootstrap's own Toast
 * component so no extra dependency is needed.
 */
function showToast(message, variant) {
  const container = document.getElementById("toastContainer");
  if (!container) return;

  const icons = { success: "bi-check-circle-fill", danger: "bi-exclamation-circle-fill", info: "bi-info-circle-fill" };
  const icon = icons[variant] || icons.info;

  const el = document.createElement("div");
  el.className = `toast align-items-center border-0 text-bg-${variant || "info"}`;
  el.setAttribute("role", "status");
  el.setAttribute("aria-live", "polite");
  el.innerHTML = `
    <div class="d-flex">
      <div class="toast-body"><i class="bi ${icon} me-2"></i>${message}</div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
    </div>`;

  container.appendChild(el);
  const toast = new bootstrap.Toast(el, { delay: 3200 });
  el.addEventListener("hidden.bs.toast", () => el.remove());
  toast.show();
}
