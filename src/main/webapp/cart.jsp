<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>My Cart</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<nav>
    <span><a href="products">ShopCart</a></span>
    <div>
        <a href="products">Products</a>
        <a href="orders">Order History</a>
        <a href="logout">Logout</a>
    </div>
</nav>

<div class="container">
    <h2>My Cart</h2>

    <% String error = (String) request.getAttribute("error");
        if (error != null) { %>
    <p class="error"><%= error %></p>
    <% } %>

    <%
        List<Map<String, Object>> items =
                (List<Map<String, Object>>) request.getAttribute("cartItems");
        if (items == null || items.isEmpty()) {
    %>
    <p>Your cart is empty. <a href="products">Browse products</a></p>
    <% } else { %>
    <table>
        <thead>
        <tr>
            <th>Product</th>
            <th>Price</th>
            <th>Quantity</th>
            <th>Total</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <% for (Map<String, Object> item : items) { %>
        <tr>
            <td><%= item.get("product_name") %></td>
            <td>Rs. <%= item.get("price") %></td>
            <td>
                <form action="cart" method="post" style="display:inline">
                    <input type="hidden" name="action"     value="update">
                    <input type="hidden" name="product_id" value="<%= item.get("product_id") %>">
                    <input type="number" name="quantity"   value="<%= item.get("quantity") %>" min="1" style="width:60px">
                    <button type="submit">Update</button>
                </form>
            </td>
            <td>Rs. <%= item.get("item_total") %></td>
            <td>
                <form action="cart" method="post" style="display:inline">
                    <input type="hidden" name="action"     value="remove">
                    <input type="hidden" name="product_id" value="<%= item.get("product_id") %>">
                    <button type="submit" class="btn-danger">Remove</button>
                </form>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>

    <div class="cart-total">
        <h3>Total: Rs. <%= request.getAttribute("cartTotal") %></h3>
        <form action="checkout" method="post">
            <button type="submit" class="btn-success">Place Order</button>
        </form>
    </div>
    <% } %>
</div>
</body>
</html>