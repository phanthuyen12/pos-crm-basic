window.initAdminDashboard = function () {
  let chart = null;

  function setText(id, v) {
    const el = document.getElementById(id);
    if (el) el.textContent = v;
  }

  function statusBadge(status) {
    if (status === "COMPLETED") return '<span class="badge-status bg-success-subtle text-success">Hoàn thành</span>';
    if (status === "PENDING") return '<span class="badge-status bg-warning-subtle text-warning">Đang xử lý</span>';
    return '<span class="badge-status bg-danger-subtle text-danger">Đã hủy</span>';
  }

  function formatDateTime(dt) {
    if (!dt) return "-";
    const d = new Date(dt);
    return d.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" }) + ", " + d.toLocaleDateString("vi-VN");
  }

  async function load() {
    try {
      const resp = await apiGet("/api/dashboard");
      if (!resp.success) return;
      const d = resp.data;

      setText("statRevenue", formatVnd(d.todayRevenue));
      setText("statOrders", d.todayOrderCount);
      setText("statProducts", d.activeProductCount);
      setText("statCustomers", d.newCustomerCount);

      renderRecentOrders(d.recentOrders || []);
      renderChart(d.revenueLast7Days || []);
    } catch (e) { /* ignore */ }
  }

  function renderRecentOrders(orders) {
    const body = document.getElementById("recentOrdersBody");
    if (!body) return;
    if (orders.length === 0) {
      body.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">Chưa có đơn hàng</td></tr>`;
      return;
    }
    body.innerHTML = orders.map((o) => `
      <tr>
        <td class="fw-semibold">#${o.orderCode}</td>
        <td>${o.customerName || "Khách lẻ"}</td>
        <td class="text-muted">${formatDateTime(o.createdAt)}</td>
        <td class="fw-semibold">${formatVnd(o.totalAmount)}</td>
        <td>${statusBadge(o.status)}</td>
      </tr>`).join("");
  }

  function renderChart(data) {
    const ctx = document.getElementById("revenueChart");
    if (!ctx) return;
    const labels = data.map((d) => d.label);
    const values = data.map((d) => Math.round(Number(d.revenue) / 1000));

    if (chart) chart.destroy();
    chart = new Chart(ctx, {
      type: "line",
      data: {
        labels: labels,
        datasets: [{
          label: "Doanh thu (nghìn đồng)",
          data: values,
          borderColor: "#0a6ebd",
          backgroundColor: "rgba(10,110,189,0.12)",
          tension: 0.35,
          fill: true,
          pointRadius: 3
        }]
      },
      options: {
        plugins: { legend: { display: false } },
        scales: {
          y: { grid: { color: "#f1f2f4" } },
          x: { grid: { display: false } }
        }
      }
    });
  }

  load();
};