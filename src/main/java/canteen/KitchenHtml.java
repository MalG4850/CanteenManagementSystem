package canteen;

import java.util.List;

public final class KitchenHtml {
    private static final String HTML_TEMPLATE = """
        <!DOCTYPE html>
        <html lang="en" data-bs-theme="dark">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <meta name="apple-mobile-web-app-capable" content="yes">
            <title>Kitchen Display - %s</title>
            <!-- Bootstrap 5 CSS (dark mode via data-bs-theme) -->
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
                  integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
            <!-- Shoelace (Web Components) -->
            <script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.15.0/cdn/shoelace-autoloader.js"></script>
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    min-height: 100vh;
                    padding: 16px;
                }
                .header {
                    display: flex; justify-content: space-between; align-items: center;
                    margin-bottom: 16px; padding: 0 8px;
                }
                .header h1 { font-size: 1.4rem; font-weight: 600; }
                .header .time { font-size: 0.85rem; font-family: monospace; }
                
                .section { margin-bottom: 24px; padding: 0 8px; }
                .section.hidden { display: none; }
                .section-title {
                    display: flex; align-items: center; gap: 8px;
                    font-size: 0.9rem; font-weight: 600; color: var(--bs-body-color);
                    margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid var(--bs-border-color);
                }
                .section-title .dot {
                    width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0;
                }
                .dot.pending { background: #f59e0b; }
                .dot.preparing { background: #3b82f6; }
                .dot.ready { background: #10b981; }
                .order-card {
                    background: var(--bs-card-bg); border-radius: 12px; padding: 16px;
                    margin-bottom: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.08);
                    border-left: 4px solid transparent;
                    transition: transform 0.15s, box-shadow 0.15s;
                }
                .order-card:active { transform: scale(0.98); }
                .order-card.pending { border-color: #f59e0b; }
                .order-card.preparing { border-color: #3b82f6; }
                .order-card.ready { border-color: #10b981; }
                .order-header {
                    display: flex; justify-content: space-between; align-items: flex-start;
                    margin-bottom: 12px; gap: 8px;
                }
                .order-id { font-size: 1.1rem; font-weight: 700; }
                .order-time { font-size: 0.75rem; color: var(--bs-secondary); font-family: monospace; }
                .order-customer { font-size: 0.9rem; margin-bottom: 4px; }
                .order-items { font-size: 0.85rem; line-height: 1.6; }
                .order-item { display: flex; justify-content: space-between; padding: 2px 0; }
                .order-item-name { flex: 1; }
                .order-item-qty { color: var(--bs-secondary); margin-left: 8px; font-variant-numeric: tabular-nums; }
                .order-total { font-weight: 600; margin-top: 8px; padding-top: 8px; border-top: 1px solid var(--bs-border-color); font-size: 0.95rem; }
                .status-select {
                    width: 100%; margin-top: 8px;
                }
                .empty-state {
                    text-align: center; padding: 32px 16px; color: var(--bs-secondary);
                }
                .empty-state svg { width: 48px; height: 48px; margin-bottom: 12px; opacity: 0.5; }
                .scanner-btn {
                    position: fixed; bottom: 24px; left: 24px; right: 24px;
                    padding: 16px; font-size: 1.1rem; font-weight: 600;
                    background: var(--bs-primary); color: var(--bs-primary-text-emphasis);
                    border: none; border-radius: 12px;
                    cursor: pointer; box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                    z-index: 100; display: flex; align-items: center; justify-content: center; gap: 8px;
                }
                .scanner-btn:active { transform: scale(0.98); }
                .modal {
                    position: fixed; inset: 0; background: rgba(0,0,0,0.8);
                    display: none; align-items: center; justify-content: center; z-index: 1000;
                }
                .modal.active { display: flex; }
                .modal-content {
                    background: var(--bs-card-bg); border-radius: 16px; padding: 24px;
                    width: 90%; max-width: 400px; text-align: center;
                }
                .modal video { width: 100%; aspect-ratio: 1; border-radius: 12px; background: #000; }
                .modal .close-btn {
                    margin-top: 16px; padding: 12px 24px; font-size: 1rem;
                    background: var(--bs-secondary-bg); border: none; border-radius: 8px; cursor: pointer;
                }
                .manual-input { margin-top: 16px; display: none; }
                .manual-input input {
                    width: 100%; padding: 12px; font-size: 1rem; border: 2px solid var(--bs-border-color);
                    border-radius: 8px; text-align: center; text-transform: uppercase;
                }
                .manual-input input:focus { outline: none; border-color: var(--bs-primary); }
                .toast {
                    position: fixed; bottom: 100px; left: 50%; transform: translateX(-50%);
                    background: var(--bs-dark); color: var(--bs-light);
                    padding: 12px 24px; border-radius: 8px; font-weight: 500; opacity: 0; pointer-events: none;
                    transition: opacity 0.3s; z-index: 2000;
                }
                .toast.show { opacity: 1; }
                .toast.success { background: #10b981; }
                .toast.error { background: #ef4444; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Kitchen Display</h1>
                <div class="time" id="clock">--:--:--</div>
            </div>

            

            <section class="section" id="section-pending">
                <div class="section-title"><span class="dot pending"></span> Pending Orders</div>
                <div id="list-pending"></div>
            </section>

            <section class="section" id="section-preparing">
                <div class="section-title"><span class="dot preparing"></span> Preparing</div>
                <div id="list-preparing"></div>
            </section>

            <section class="section" id="section-ready">
                <div class="section-title"><span class="dot ready"></span> Ready for Pickup</div>
                <div id="list-ready"></div>
            </section>

            <button class="scanner-btn" id="scanBtn">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3 7V5a2 2 0 0 1 2-2h2M17 3h2a2 2 0 0 1 2 2v2M21 17v2a2 2 0 0 1-2 2h-2M7 21H5a2 2 0 0 1-2-2v-2"></path>
                    <rect x="7" y="7" width="10" height="10" rx="2"></rect>
                </svg>
                Scan QR Code
            </button>

            <div class="modal" id="scanModal">
                <div class="modal-content">
                    <video id="scanVideo" playsinline></video>
                    <div class="manual-input" id="manualInput">
                        <input type="text" id="manualToken" placeholder="Enter Order Token Manually" autocomplete="off">
                    </div>
                    <button class="close-btn" id="closeScan">Close Scanner</button>
                </div>
            </div>

            <div class="toast" id="toast"></div>

            <script type="module">
                import { setDefaultAnimation } from "https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.15.0/cdn/shoelace-autoloader.js";
                // ===== State =====
                let activeFilters = {pending: true, preparing: true, ready: true};
                let scanStream = null;
                let scanInterval = null;

                // ===== Clock =====
                function updateClock() {
                    const now = new Date();
                    document.getElementById('clock').textContent =
                        now.toLocaleTimeString('en-IN', {hour12: false});
                }
                setInterval(updateClock, 1000);
                updateClock();

                // ===== Toast =====
                function showToast(msg, type = 'success') {
                    const toast = document.getElementById('toast');
                    toast.textContent = msg;
                    toast.className = 'toast ' + type + ' show';
                    setTimeout(() => toast.classList.remove('show'), 3000);
                }

                // ===== Status change handler (Shoelace select) =====
                function changeStatus(select) {
                    const token = select.dataset.token;
                    const action = select.value;
                    const original = select.value;
                    select.disabled = true;
                    fetch('/kitchen/scan', {
                        method: 'POST',
                        headers: {'Content-Type':'application/x-www-form-urlencoded'},
                        body: `token=${encodeURIComponent(token)}&action=${encodeURIComponent(action)}`
                    }).then(res=>res.json()).then(data=>{
                        if(data.success){
                            showToast(`Order #${data.orderId} - ${data.display}`);
                            loadOrders();
                        } else {
                            showToast(data.error || 'Failed', 'error');
                            select.value = original;
                        }
                        select.disabled = false;
                    }).catch(()=>{
                        showToast('Network error', 'error');
                        select.value = original;
                        select.disabled = false;
                    });
                }

                // ===== Filters =====
                document.querySelectorAll('.filter-btn').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const status = btn.dataset.status;
                        activeFilters[status] = !activeFilters[status];
                        btn.classList.toggle('active', activeFilters[status]);
                        document.getElementById('section-' + status).classList.toggle('hidden', !activeFilters[status]);
                    });
                });

                // ===== Render Orders =====
                function renderOrder(order, status) {
                    const statuses = ['PENDING','PREPARING','READY','SERVED'];
                    const options = statuses.map(s => `<sl-option value="${s}" ${s===status?'selected':''}>${s}</sl-option>`).join('');
                    const shortToken = order.token.slice(0, 8) + '...';

                    return `
                        <div class="order-card ${status.toLowerCase()}">
                            <div class="order-header">
                                <span class="order-id">#${order.orderId}</span>
                                <span class="order-time">${order.time}</span>
                            </div>
                            <div class="order-customer">Customer: ${order.customer} (${order.mobile})</div>
                            <div class="order-items">
                                ${order.items ? order.items.map(item =>
                                    `<div class="order-item">
                                        <span class="order-item-name">${item.name}</span>
                                        <span class="order-item-qty">x${item.qty} - Rs.${item.total.toFixed(2)}</span>
                                    </div>`
                                ).join('') : ''}
                            </div>
                            <div class="order-total">Total: Rs.${order.total.toFixed(2)}</div>
                            <sl-select class="status-select ${status.toLowerCase()}" data-token="${order.token}" onchange="changeStatus(this)">
                                ${options}
                            </sl-select>
                        </div>
                    `;
                }

                function renderEmpty(status) {
                    const icons = {
                        pending: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2M9 5v14"></path></svg>',
                        preparing: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"></circle><path d="M12 6v6l4 2"></path></svg>',
                        ready: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>'
                    };
                    const texts = {
                        pending: 'No pending orders',
                        preparing: 'Nothing cooking yet',
                        ready: 'No orders ready'
                    };
                    return `<div class="empty-state">${icons[status]}<div>${texts[status]}</div></div>`;
                }

                function renderList(orders, status, listId) {
                    const container = document.getElementById(listId);
                    if (orders.length === 0) {
                        container.innerHTML = renderEmpty(status.toLowerCase());
                        return;
                    }
                    container.innerHTML = orders.map(o => renderOrder(o, status)).join('');
                }

                // ===== Load Orders =====
                async function loadOrders() {
                    try {
                        const res = await fetch('/kitchen/api/orders');
                        const data = await res.json();
                        if (activeFilters.pending) renderList(data.pending, 'PENDING', 'list-pending');
                        if (activeFilters.preparing) renderList(data.preparing, 'PREPARING', 'list-preparing');
                        if (activeFilters.ready) renderList(data.ready, 'READY', 'list-ready');
                    } catch (err) {
                        console.error('Failed to load orders:', err);
                    }
                }

                // Poll every 5 seconds
                setInterval(loadOrders, 5000);
                loadOrders();

                // ===== QR Scanner =====
                const scanBtn = document.getElementById('scanBtn');
                const scanModal = document.getElementById('scanModal');
                const scanVideo = document.getElementById('scanVideo');
                const closeScan = document.getElementById('closeScan');
                const manualInput = document.getElementById('manualInput');
                const manualToken = document.getElementById('manualToken');

                const hasBarcodeDetector = 'BarcodeDetector' in window;

                scanBtn.addEventListener('click', async () => {
                    scanModal.classList.add('active');
                    manualInput.style.display = 'none';

                    if (hasBarcodeDetector) {
                        try {
                            scanStream = await navigator.mediaDevices.getUserMedia({
                                video: {facingMode: 'environment', width: {ideal: 1280}, height: {ideal: 720}}
                            });
                            scanVideo.srcObject = scanStream;
                            await scanVideo.play();
                            startBarcodeDetection();
                        } catch (err) {
                            console.warn('Camera access denied:', err);
                            fallbackToManual();
                        }
                    } else {
                        fallbackToManual();
                    }
                });

                function fallbackToManual() {
                    manualInput.style.display = 'block';
                    manualToken.focus();
                }

                function startBarcodeDetection() {
                    const detector = new BarcodeDetector({formats: ['qr_code']});
                    scanInterval = setInterval(async () => {
                        try {
                            const barcodes = await detector.detect(scanVideo);
                            if (barcodes.length > 0) {
                                const token = barcodes[0].rawValue;
                                stopScanner();
                                await processToken(token);
                            }
                        } catch (err) {
                            // ignore detection errors
                        }
                    }, 300);
                }

                function stopScanner() {
                    if (scanInterval) clearInterval(scanInterval);
                    if (scanStream) {
                        scanStream.getTracks().forEach(t => t.stop());
                        scanStream = null;
                    }
                    scanVideo.srcObject = null;
                }

                closeScan.addEventListener('click', () => {
                    stopScanner();
                    scanModal.classList.remove('active');
                    manualInput.style.display = 'none';
                    manualToken.value = '';
                });

                manualToken.addEventListener('keypress', async (e) => {
                    if (e.key === 'Enter' && manualToken.value.trim()) {
                        await processToken(manualToken.value.trim());
                    }
                });

                async function processToken(token) {
                    try {
                        const res = await fetch('/kitchen/scan', {
                            method: 'POST',
                            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                            body: `token=${encodeURIComponent(token)}&action=next`
                        });
                        const data = await res.json();
                        if (data.success) {
                            showToast(`Order #${data.orderId} - ${data.display}`);
                            loadOrders();
                            closeScan.click();
                        } else {
                            showToast(data.error || 'Invalid QR code', 'error');
                        }
                    } catch (err) {
                        showToast('Network error', 'error');
                    }
                }

                // Close modal on backdrop click
                scanModal.addEventListener('click', (e) => {
                    if (e.target === scanModal) closeScan.click();
                });

                
            </script>
        </body>
        </html>
        """;

    public static String generatePage(OrderService orderService) {
        List<Order> pending = orderService.getPendingOrders();
        List<Order> preparing = orderService.getPreparingOrders();
        List<Order> ready = orderService.getReadyOrders();

        // Pre-render initial HTML for faster first paint
        String pendingHtml = ordersToHtml(pending, "PENDING");
        String preparingHtml = ordersToHtml(preparing, "PREPARING");
        String readyHtml = ordersToHtml(ready, "READY");

        String canteenName = AppConfig.getInstance().get("canteen.name", "College Canteen");

        String page = String.format(HTML_TEMPLATE, canteenName);
        // The JS will load initial data via API, so we just return the template
        return page;
    }

    private static String ordersToHtml(List<Order> orders, String status) {
        if (orders.isEmpty()) {
            return "<div class=\"empty-state\">No " + status.toLowerCase() + " orders</div>";
        }
        StringBuilder sb = new StringBuilder();
        for (Order o : orders) {
            String nextAction = switch (status) {
                case "PENDING" -> "PREPARING";
                case "PREPARING" -> "READY";
                default -> "SERVED";
            };
            String btnText = switch (status) {
                case "PENDING" -> "Start Preparing";
                case "PREPARING" -> "Mark Ready";
                default -> "Mark Served";
            };
            sb.append("<div class=\"order-card ").append(status.toLowerCase()).append("\">");
            sb.append("<div class=\"order-header\">");
            sb.append("<span class=\"order-id\">#").append(o.getOrderId()).append("</span>");
            sb.append("<span class=\"order-time\">").append(o.getFormattedOrderDate()).append("</span>");
            sb.append("</div>");
            sb.append("<div class=\"order-customer\">Customer: ").append(escapeHtml(o.getCustomerName()))
              .append(" (").append(o.getCustomerMobile()).append(")</div>");
            sb.append("<div class=\"order-items\">");
            for (OrderItem item : o.getItems()) {
                sb.append("<div class=\"order-item\">");
                sb.append("<span class=\"order-item-name\">").append(escapeHtml(item.getItemName())).append("</span>");
                sb.append("<span class=\"order-item-qty\">x").append(item.getQuantity())
                  .append(" - Rs.").append(String.format("%.2f", item.getLineTotal())).append("</span>");
                sb.append("</div>");
            }
            sb.append("</div>");
            sb.append("<div class=\"order-total\">Total: Rs.").append(String.format("%.2f", o.getTotalAmount())).append("</div>");
            if (!"SERVED".equals(status)) {
                sb.append("<button class=\"action-btn ").append(status.toLowerCase()).append("\" data-token=\"").append(o.getQrToken())
                  .append("\" data-action=\"").append(nextAction).append("\">").append(btnText).append("</button>");
            }
            sb.append("</div>");
        }
        return sb.toString();
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&").replace("<", "<").replace(">", ">").replace("\"", "");
    }
}