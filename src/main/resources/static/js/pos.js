/**
 * Called after the layout (and its console search box) has been injected.
 * Kết nối backend qua REST API: tải sản phẩm, khách hàng và tạo đơn hàng thật.
 */
window.initPos = function () {
  const TAX_RATE = 0.08;
  let products = []; // dữ liệu sản phẩm từ backend
  let activeCategory = "all";
  let searchTerm = "";
  let currentOrderId = null;
  let appliedVoucher = null;
  let voucherSubtotal = null;
  const currentUser = getCurrentUser();
  const cart = {}; // id -> { product, qty, note, favorite }

  const productGrid = document.getElementById("productGrid");
  const cartItemsEl = document.getElementById("cartItems");
  const cartNoteBar = document.getElementById("cartNoteBar");
  const cartNoteText = document.getElementById("cartNoteText");
  const subtotalEl = document.getElementById("subtotal");
  const discountEl = document.getElementById("discount");
  const taxEl = document.getElementById("tax");
  const grandTotalEl = document.getElementById("grandTotal");
  const productSearchEl = document.getElementById("productSearch");

  const VN_PHONE_REGEX = /^(0|\+84)(3|5|7|8|9)\d{8}$/;

  const newOrderGate = document.getElementById("newOrderGate");
  const posWorkspace = document.getElementById("posWorkspace");
  const newOrderForm = document.getElementById("newOrderForm");
  const newOrderPhone = document.getElementById("newOrderPhone");
  const newOrderName = document.getElementById("newOrderName");
  const phoneError = document.getElementById("phoneError");
  const createOrderBtn = document.getElementById("createOrderBtn");
  const skipToGuestBtn = document.getElementById("skipToGuestBtn");
  const newOrderBtn = document.getElementById("newOrderBtn");
  const currentOrderNoEl = document.getElementById("currentOrderNo");
  const currentOrderCustomerEl = document.getElementById("currentOrderCustomer");
  const customerSelect = document.getElementById("customerSelect");
  let phoneTouched = false;
  let isCreatingOrder = false;
  let customers = [];

  function renderProducts() {
    const filtered = products.filter((p) => {
      const matchCategory = activeCategory === "all" || (p.category && p.category.slug === activeCategory);
      const matchSearch = p.name.toLowerCase().includes(searchTerm.toLowerCase());
      return matchCategory && matchSearch;
    });

    if (filtered.length === 0) {
      productGrid.innerHTML = `<div class="text-center text-muted py-5" style="grid-column: 1 / -1;">
        <i class="bi bi-search fs-1 d-block mb-2"></i>Không tìm thấy sản phẩm
      </div>`;
      return;
    }

    productGrid.innerHTML = filtered
      .map(
        (p) => `
      <div class="product-card" data-id="${p.id}">
        <div class="product-thumb-wrap">
          <span class="price-badge">${formatVnd(p.price)}</span>
          <i class="bi ${p.icon || "bi-box"}"></i>
        </div>
        <p class="product-name text-clamp-2">${p.name}</p>
      </div>`
      )
      .join("");
  }

  function renderCart() {
    const items = Object.values(cart);

    if (items.length === 0) {
      cartItemsEl.innerHTML = `<div class="cart-empty">
        <i class="bi bi-cart3"></i>
        Giỏ hàng trống<br />Chọn sản phẩm để bắt đầu
      </div>`;
    } else {
      cartItemsEl.innerHTML = items
        .map(
          (item, index) => `
        <div class="cart-item" data-id="${item.product.id}">
          <div class="cart-item-index">${index + 1}.</div>
          <div class="cart-item-body">
            <div class="cart-item-top">
              <p class="cart-item-name">${item.product.name}</p>
              <button class="cart-fav-btn ${item.favorite ? "active" : ""}" data-action="favorite">
                <i class="bi ${item.favorite ? "bi-star-fill" : "bi-star"}"></i>
              </button>
            </div>
            <button class="cart-item-note ${item.note ? "has-note" : ""}" data-action="note">
              ${item.note ? item.note : "+ Thêm ghi chú"}
            </button>
            <div class="cart-item-bottom">
              <div class="qty-control">
                <button data-action="decrease">-</button>
                <span>${item.qty}</span>
                <button data-action="increase">+</button>
              </div>
              <div class="cart-item-prices">
                <span class="unit-price">${formatVnd(item.product.price)}</span>
                <span class="line-total">${formatVnd(item.product.price * item.qty)}</span>
              </div>
              <button class="cart-remove-btn" data-action="remove"><i class="bi bi-x-lg"></i></button>
            </div>
          </div>
        </div>`
        )
        .join("");
    }

    const totalQty = items.reduce((sum, item) => sum + item.qty, 0);
    if (totalQty > 0) {
      cartNoteBar.classList.add("visible");
      cartNoteText.textContent = `Đơn hàng có ${totalQty} món, vui lòng kiểm tra trước khi thanh toán.`;
    } else {
      cartNoteBar.classList.remove("visible");
    }

    const subtotal = items.reduce((sum, item) => sum + item.product.price * item.qty, 0);
    if (appliedVoucher && voucherSubtotal !== subtotal) {
      appliedVoucher = null;
      voucherSubtotal = null;
      const message = document.getElementById("voucherMessage");
      message.textContent = "Giỏ hàng đã thay đổi. Hãy áp dụng lại voucher.";
      message.className = "small mb-2 text-warning";
    }
    const discount = appliedVoucher ? Math.min(Number(appliedVoucher.discountAmount), subtotal) : 0;
    const tax = Math.round((subtotal - discount) * TAX_RATE);
    const total = subtotal - discount + tax;

    subtotalEl.textContent = formatVnd(subtotal);
    discountEl.textContent = formatVnd(discount);
    taxEl.textContent = formatVnd(tax);
    grandTotalEl.textContent = formatVnd(total);
  }

  function addToCart(productId) {
    const product = products.find((p) => p.id === productId);
    if (!product) return;
    if (cart[productId]) {
      cart[productId].qty += 1;
    } else {
      cart[productId] = { product, qty: 1, note: "", favorite: false };
    }
    renderCart();
  }

  function resetFilters() {
    activeCategory = "all";
    searchTerm = "";
    productSearchEl.value = "";
    document.querySelectorAll(".pos-category-pill").forEach((p) => p.classList.remove("active"));
    document.querySelector('.pos-category-pill[data-category="all"]').classList.add("active");
    renderProducts();
  }

  function setPhoneError(message) {
    phoneError.textContent = message || "";
    newOrderPhone.closest(".input-icon-group").classList.toggle("field-invalid", Boolean(message));
  }

  function validatePhoneField() {
    const raw = newOrderPhone.value.replace(/\s+/g, "");
    if (newOrderPhone.value !== raw) newOrderPhone.value = raw;
    if (!raw) {
      setPhoneError("");
      return true;
    }
    const valid = VN_PHONE_REGEX.test(raw);
    setPhoneError(valid ? "" : "Số điện thoại không đúng định dạng (VD: 0901234567)");
    return valid;
  }

  function setCreateOrderLoading(loading) {
    isCreatingOrder = loading;
    createOrderBtn.disabled = loading;
    createOrderBtn.querySelector(".btn-label").classList.toggle("d-none", loading);
    createOrderBtn.querySelector(".btn-loading").classList.toggle("d-none", !loading);
  }

  function showGate() {
    Object.keys(cart).forEach((key) => delete cart[key]);
    renderCart();
    newOrderForm.reset();
    phoneTouched = false;
    setPhoneError("");
    setCreateOrderLoading(false);
    currentOrderId = null;
    appliedVoucher = null;
    voucherSubtotal = null;
    document.getElementById("voucherCode").value = "";
    document.getElementById("voucherMessage").textContent = "";
    document.getElementById("voucherMessage").className = "small mb-2";
    renderCart();
    posWorkspace.classList.add("d-none");
    newOrderGate.classList.remove("d-none");
    if (window.matchMedia("(min-width: 992px)").matches) {
      newOrderPhone.focus();
    }
  }

  async function startNewOrder(phone, name) {
    setCreateOrderLoading(true);
    try {
      // Tìm/ghi nhận khách hàng theo số điện thoại (nếu có)
      let customerId = null;
      if (phone) {
        try {
          const resp = await apiGet(`/api/people?type=CUSTOMER`);
          if (resp.success) {
            const match = (resp.data || []).find((c) => c.phone === phone);
            if (match) customerId = match.id;
          }
        } catch (e) { /* bỏ qua, bán cho khách lẻ */ }
      }

      currentOrderNoEl.textContent = "Đang tạo...";
      const label = name ? `${name}${phone ? " - " + phone : ""}` : phone ? phone : "Khách lẻ";
      currentOrderCustomerEl.textContent = label;

      // Lưu customerId đã chọn để dùng khi thanh toán
      window.__currentCustomerId = customerId;

      currentOrderNoEl.textContent = "DH" + String(Date.now()).slice(-4);
      currentOrderId = null;

      setCreateOrderLoading(false);
      newOrderGate.classList.add("d-none");
      posWorkspace.classList.remove("d-none");
      productSearchEl.focus();
      showToast(`Đã tạo đơn cho ${label} — chọn sản phẩm để bán`, "success");
    } catch (e) {
      setCreateOrderLoading(false);
      showToast("Không thể tạo đơn: " + e.message, "danger");
    }
  }

  async function checkout() {
    if (Object.keys(cart).length === 0) {
      alert("Giỏ hàng đang trống. Vui lòng chọn sản phẩm trước khi thanh toán.");
      return;
    }
    const items = Object.values(cart).map((item) => ({
      productId: item.product.id,
      quantity: item.qty,
      note: item.note || null,
      productName: item.product.name,
      unitPrice: item.product.price,
    }));

    const checkout = {
      customerId: window.__currentCustomerId || null,
      employeeId: currentUser ? currentUser.id : null,
      voucherId: appliedVoucher ? appliedVoucher.voucherId : null,
      voucherCode: appliedVoucher ? appliedVoucher.code : null,
      orderType: "TAKEAWAY",
      note: null,
      items: items,
      customerName: currentOrderCustomerEl.textContent || "Khách lẻ",
      orderCodePreview: currentOrderNoEl.textContent,
    };
    sessionStorage.setItem("posapp_checkout", JSON.stringify(checkout));
    window.location.href = "/client/payment.html";
  }

  async function applyVoucher() {
    const codeInput = document.getElementById("voucherCode");
    const message = document.getElementById("voucherMessage");
    const code = codeInput.value.trim();
    const subtotal = Object.values(cart).reduce((sum, item) => sum + item.product.price * item.qty, 0);
    if (!code) {
      appliedVoucher = null;
      message.textContent = "Nhập mã voucher trước khi áp dụng.";
      message.className = "small mb-2 text-danger";
      renderCart();
      return;
    }
    try {
      const resp = await apiPost("/api/vouchers/apply", { code, subtotal });
      if (!resp.success) {
        appliedVoucher = null;
        message.textContent = resp.message || "Voucher không hợp lệ.";
        message.className = "small mb-2 text-danger";
      } else {
        appliedVoucher = resp.data;
        voucherSubtotal = subtotal;
        message.textContent = `Đã áp dụng ${resp.data.code}: giảm ${formatVnd(resp.data.discountAmount)}`;
        message.className = "small mb-2 text-success";
      }
      renderCart();
    } catch (e) {
      appliedVoucher = null;
      message.textContent = "Không thể kiểm tra voucher: " + e.message;
      message.className = "small mb-2 text-danger";
      renderCart();
    }
  }

  // ===== Load dữ liệu ban đầu =====
  async function loadData() {
    try {
      const pResp = await apiGet("/api/products?activeOnly=true");
      if (pResp.success) {
        // Lấy slug category cho từng sản phẩm qua categoryName
        products = (pResp.data || []).map((p) => ({
          id: p.id,
          name: p.name,
          price: Number(p.price),
          icon: p.icon,
          category: { slug: p.categorySlug || "" },
        }));
      } else {
        products = [];
      }
      renderProducts();
    } catch (e) {
      products = [];
      renderProducts();
      showToast("Không tải được sản phẩm từ backend", "danger");
    }

    try {
      const cResp = await apiGet("/api/people?type=CUSTOMER");
      if (cResp.success) {
        customers = cResp.data || [];
        renderCustomerOptions();
      }
    } catch (e) { /* không có khách cũng không sao */ }
  }

  function renderCustomerOptions() {
    if (!customerSelect) return;
    const current = customerSelect.value;
    let html = `<option value="guest" selected>Khách lẻ</option>`;
    html += customers.map((c) =>
      `<option value="${c.id}">${c.fullName || ""} - ${c.phone || ""}</option>`
    ).join("");
    customerSelect.innerHTML = html;
    customerSelect.value = current === "" ? "guest" : current;
  }

  // Category filter
  document.getElementById("categoryBar").addEventListener("click", (e) => {
    const btn = e.target.closest(".pos-category-pill");
    if (!btn) return;
    document.querySelectorAll(".pos-category-pill").forEach((p) => p.classList.remove("active"));
    btn.classList.add("active");
    activeCategory = btn.dataset.category;
    renderProducts();
  });

  // Search
  productSearchEl.addEventListener("input", (e) => {
    searchTerm = e.target.value;
    renderProducts();
  });

  // Add to cart from grid
  productGrid.addEventListener("click", (e) => {
    const card = e.target.closest(".product-card");
    if (!card) return;
    addToCart(Number(card.dataset.id));
  });

  // Cart item actions
  cartItemsEl.addEventListener("click", (e) => {
    const row = e.target.closest(".cart-item");
    if (!row) return;
    const id = Number(row.dataset.id);
    const action = e.target.closest("button")?.dataset.action;
    const item = cart[id];
    if (!item) return;

    if (action === "increase") {
      item.qty += 1;
    } else if (action === "decrease") {
      item.qty -= 1;
      if (item.qty <= 0) delete cart[id];
    } else if (action === "remove") {
      delete cart[id];
    } else if (action === "favorite") {
      item.favorite = !item.favorite;
    } else if (action === "note") {
      const note = prompt("Ghi chú / món thêm:", item.note || "");
      if (note !== null) item.note = note.trim();
    }
    renderCart();
  });

  // Clear cart
  document.getElementById("clearCartBtn").addEventListener("click", () => {
    if (Object.keys(cart).length === 0) return;
    if (!confirm("Xoá toàn bộ sản phẩm trong giỏ hàng?")) return;
    Object.keys(cart).forEach((key) => delete cart[key]);
    renderCart();
  });

  // Payment method selection
  document.querySelectorAll(".payment-method-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      document.querySelectorAll(".payment-method-btn").forEach((b) => b.classList.remove("active"));
      btn.classList.add("active");
    });
  });

  // Hold order / print receipt
  document.getElementById("holdOrderBtn").addEventListener("click", () => {
    if (Object.keys(cart).length === 0) {
      alert("Giỏ hàng đang trống, không có gì để giữ lại.");
      return;
    }
    alert("Đã giữ đơn hàng. Bạn có thể tiếp tục bán đơn khác và quay lại sau.");
  });

  document.getElementById("printBtn").addEventListener("click", () => {
    if (Object.keys(cart).length === 0) {
      alert("Giỏ hàng đang trống, không có gì để in.");
      return;
    }
    window.print();
  });

  // Checkout
  document.getElementById("checkoutBtn").addEventListener("click", checkout);
  document.getElementById("applyVoucherBtn").addEventListener("click", applyVoucher);
  document.getElementById("voucherCode").addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      applyVoucher();
    }
  });

  // New order gate
  newOrderPhone.addEventListener("blur", () => {
    phoneTouched = true;
    validatePhoneField();
  });

  newOrderPhone.addEventListener("input", () => {
    if (phoneTouched) setPhoneError("");
  });

  newOrderForm.addEventListener("submit", (e) => {
    e.preventDefault();
    if (isCreatingOrder) return;

    phoneTouched = true;
    if (!validatePhoneField()) {
      newOrderPhone.focus();
      return;
    }
    startNewOrder(newOrderPhone.value.trim(), newOrderName.value.trim());
  });

  skipToGuestBtn.addEventListener("click", () => {
    if (isCreatingOrder) return;
    newOrderForm.reset();
    setPhoneError("");
    startNewOrder("", "");
  });

  newOrderBtn.addEventListener("click", () => {
    if (Object.keys(cart).length > 0 && !confirm("Tạo đơn mới sẽ xoá giỏ hàng hiện tại. Tiếp tục?")) {
      return;
    }
    showGate();
  });

  // Console tabs
  document.getElementById("posTabs").addEventListener("click", (e) => {
    const tab = e.target.closest(".pos-tab");
    if (!tab) return;
    if (tab.dataset.tab !== "sale") {
      alert("Tính năng này đang được phát triển.");
      return;
    }
    document.querySelectorAll(".pos-tab").forEach((t) => t.classList.remove("active"));
    tab.classList.add("active");
  });

  document.getElementById("refreshPosBtn").addEventListener("click", resetFilters);

  // Keyboard shortcuts: F8 focuses search, F9 triggers checkout
  document.addEventListener("keydown", (e) => {
    if (e.key === "F8") {
      e.preventDefault();
      productSearchEl.focus();
    } else if (e.key === "F9") {
      e.preventDefault();
      checkout();
    }
  });

  loadData();
  renderCart();

  if (window.matchMedia("(min-width: 992px)").matches) {
    newOrderPhone.focus();
  }
};
