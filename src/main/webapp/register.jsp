<!DOCTYPE html>
<html>
<head>
    <title>Register</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<div class="container">
    <h2>Create Account</h2>
    <% String error=(String) request.getAttribute("error"); %>
    <% if (error != null){%>
    <p style="color:red;"><%= error %></p>
    <%}%>
    <form action="register" method="post">
        <input type="text"     name="name"     placeholder="Full Name"    required>
        <input type="email"    name="email"    placeholder="Email"        required>
        <input type="password" name="password" placeholder="Password"     required>
        <input type="text"     name="phone"    placeholder="Phone Number" required>
        <button type="submit">Register</button>
    </form>
    <p>Already have an account? <a href="login.jsp">Login</a></p>
</div>
</body>
</html>