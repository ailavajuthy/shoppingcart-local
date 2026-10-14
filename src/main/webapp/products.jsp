<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>Products</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<nav>
    <span><a href="products">ShopCart</a></span>
    <div>
        <a href="cart">My Cart</a>
        <a href="orders">Order History</a>
        <a href="logout">Logout</a>
    </div>
</nav>

<div class="container">


    <form action="products" method="get" class="filter-form">
        <%
            String selectedCategory=request.getParameter("category");
            if(selectedCategory==null)
                selectedCategory="all";
        %>
        <input type="text" name="search" placeholder="Search products...">

        <select name="category" onchange="this.form.submit()">
            <option value="all" <%=selectedCategory.equals("all")?"selected":"" %>>All Categories</option>
            <option value="Electronics" <%=selectedCategory.equals("Electronics")?"selected":"" %>>Electronics</option>
            <option value="Footwear" <%=selectedCategory.equals("Footwear")?"selected":"" %>>Footwear</option>
            <option value="Books" <%=selectedCategory.equals("Books")?"selected":"" %>>Books</option>
            <option value="Accessories" <%=selectedCategory.equals("Accessories")?"selected":"" %>>Accessories</option>
            <option value="Sports" <%=selectedCategory.equals("Sports")?"selected":"" %>>Sports</option>
        </select>

    </form>
    <% if(request.getAttribute("error")!=null){ %>
    <p class="error"><%=request.getAttribute("error")%></p>
    <% } %>
    <div class="product-grid">
        <%
            List<Map<String,Object>> products=
                    (List<Map<String,Object>>)request.getAttribute("products");

            if(products==null||products.isEmpty()){
        %>
        <p class="no-results">No products found</p>
        <%
        }else{
            for(Map<String,Object> p:products){
                int stock=(int)p.get("stock_quantity");
        %>

        <div class="card">
            <h3><%=p.get("product_name")%></h3>
            <p class="category"><%=p.get("category")%></p>
            <p class="desc"><%=p.get("description")%></p>
            <p class="price">Rs. <%=p.get("price")%></p>

            <% if(stock==0){ %>
            <p class="out-of-stock">Out of stock</p>
            <% }else{ %>
            <p class="in-stock"><%=stock%> in stock</p>
            <form action="cart" method="post">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="product_id" value="<%=p.get("product_id")%>">
                <input type="number" name="quantity" value="1" min="1" max="<%=stock%>">
                <button type="submit">Add to Cart</button>
            </form>
            <% } %>
        </div>

        <%
                }
            }
        %>
    </div>



</div>
</body>
</html>