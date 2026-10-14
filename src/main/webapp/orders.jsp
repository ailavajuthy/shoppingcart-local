<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>Order History</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<nav>
    <span><a href="products">ShopCart</a></span>
    <div>
        <a href="products">Products</a>
        <a href="cart">My Cart</a>
        <a href="logout">Logout</a>
    </div>
</nav>

<div class="container">
    <h2>Order History</h2>

    <%
        List<Map<String, Object>> orders =
                (List<Map<String, Object>>) request.getAttribute("orders");
        if (orders == null || orders.isEmpty()) {
    %>
    <p>No orders yet. <a href="products">Start shopping</a></p>
    <% } else { %>
    <table>
        <thead>
        <tr>
            <th>Order ID</th>
            <th>Product</th>
            <th>Qty</th>
            <th>Unit Price</th>
            <th>Order Total</th>
            <th>Date</th>
            <th>Status</th>
        </tr>
        </thead>
        <tbody>
        <% for (Map<String, Object> o : orders) { %>
        <tr>
            <td>#<%= o.get("order_id") %></td>
            <td><%= o.get("product_name") %></td>
            <td><%= o.get("quantity") %></td>
            <td>Rs. <%= o.get("unit_price") %></td>
            <td>Rs. <%= o.get("total_amount") %></td>
            <td><%= o.get("order_date") %></td>
            <td><span class="status"><%= o.get("status") %></span></td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <% } %>
</div>
</body>
</html>