window.initAdminReports = function () {
  let orders = [];
  let chart = null;

  function setText(id, v) {
    const el = document.getElementById(id);
    if (el) el.textContent = v;
  }

  async function load() {
    try {
      const resp = await apiGet("/api/orders");
      orders = resp.success ? (resp.data || []) : [];
      render();
    } catch (e) { orders = []; render(); }
  }

  function render() {
    const completed = orders.filter((o) => o.status === "COMPLETED");
    const totalRevenue = completed.reduce((s, o) => s + Number(o.totalAmount), 0);
    const orderCount = orders.length;
    const avg = orderCount > 0 ? Math.round(totalRevenue / orderCount) : 0;

    setText("kpiRevenue", formatVnd(totalRevenue));
    setText("kpiOrders", orderCount);
    setText("kpiAvg", formatVnd(avg));

    // Employees performance (group by employee)
    const empMap = {};
    completed.forEach((o) => {
      const key = o.employeeId || "unknown";
      if (!empMap[key]) empMap[key] = { name: o.employeeName || "Khác", count: 0, revenue: 0 };
      empMap[key].count += 1;
      empMap[key].revenue += Number(o.totalAmount);
    });
    setText("kpiEmployees", Object.keys(empMap).length);

    renderEmpTable(empMap);
    renderChart(orders);
  }

  function renderEmpTable(empMap) {
    const body = document.getElementById("empPerfTableBody");
    if (!body) return;
    const rows = Object.values(empMap);
    if (rows.length === 0) {
      body.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">Chưa có dữ liệu</td></tr>`;
      return;
    }
    body.innerHTML = rows.map((r) => `
      <tr>
        <td class="fw-semibold">${r.name}</td>
        <td>-</td>
        <td>${r.count}</td>
        <td class="fw-semibold">${formatVnd(r.revenue)}</td>
        <td>${formatVnd(Math.round(r.revenue / r.count))}</td>
      </tr>`).join("");
  }

  function renderChart(orderList) {
    const ctx = document.getElementById("reportChart");
    if (!ctx) return;

    // Tổng doanh thu theo ngày (7 ngày gần nhất)
    const days = [];
    const revenue = [];
    for (let i = 6; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      const key = d.toISOString().slice(0, 10);
      days.push(d.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit" }));
      const dayTotal = orderList
        .filter((o) => o.status === "COMPLETED" && (o.createdAt || "").slice(0, 10) === key)
        .reduce((s, o) => s + Number(o.totalAmount), 0);
      revenue.push(Math.round(dayTotal / 1000));
    }

    if (chart) chart.destroy();
    chart = new Chart(ctx, {
      type: "bar",
      data: {
        labels: days,
        datasets: [{
          label: "Doanh thu (nghìn đồng)",
          data: revenue,
          backgroundColor: "#0a6ebd",
          borderRadius: 3,
          maxBarThickness: 28
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

  const filterBtn = document.querySelector(".btn-brand");
  if (filterBtn) filterBtn.addEventListener("click", load);

  load();
};