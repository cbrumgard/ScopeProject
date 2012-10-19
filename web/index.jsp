<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta charset="UTF-8" />
<title>Scope Project</title>

<style type="text/css">
h2.center {text-align:center;}
</style>

</head>
<body>
    <h2 class="center">Welcome to the Scope Project</h2>
    
    <form method="get" action="participant/getMainPage">
        <input type="submit" value="participant login" />
    </form>
    <br />
    <br />
    <form method="get" action="admin/getMainPage">
        <input type="submit" value="admin login" />
    </form>
</body>
</html>