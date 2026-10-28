/**
 * Shared API client. Mọi API trả về wrapper { success, message, data }.
 */
async function apiGet(url) {
  const res = await fetch(url);
  return await res.json();
}

async function apiPost(url, body) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : undefined,
  });
  return await res.json();
}

async function apiPut(url, body) {
  const res = await fetch(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  return await res.json();
}

async function apiDelete(url) {
  const res = await fetch(url, { method: "DELETE" });
  return await res.json();
}

function formatVnd(amount) {
  const n = Number(amount || 0);
  return n.toLocaleString("vi-VN") + "₫";
}

// ===== Auth helpers =====
const AUTH_KEY = "posapp_user";

function getCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem(AUTH_KEY) || "null");
  } catch (e) {
    return null;
  }
}

function setCurrentUser(user) {
  localStorage.setItem(AUTH_KEY, JSON.stringify(user));
}

function clearCurrentUser() {
  localStorage.removeItem(AUTH_KEY);
}

// MANAGER có quyền vào admin; các role khác chỉ vào màn hình POS.
function isAdmin(user) {
  return user && user.role === "MANAGER";
}

// Chặn truy cập trang trái phép: nếu chưa đăng nhập -> login; nếu vào admin nhưng
// không phải MANAGER -> về POS. Gọi ở mỗi trang.
function guardPage(requiredRole) {
  const user = getCurrentUser();
  if (!user) {
    window.location.href = "/login.html";
    return null;
  }
  if (requiredRole === "admin" && !isAdmin(user)) {
    window.location.href = "/client/pos.html";
    return null;
  }
  return user;
}