<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Login Page</title>
<style type="text/css">
h2.center { text-align: center; }
</style>
</head>

<s:set var="redirect" value="%{#session.originalPath}" />

<s:if test="%{#redirect == ''}">
    <s:set var="redirect" value="%{'admin/login'}" />
</s:if>

<body>
    <h2 class="center">Admin Login</h2>
    <br />
    <s:form action="%{redirect}" method="post">
        <s:textfield label="Username" key="userID" />
        <s:password label="Password" key="password" />
        <s:submit />
    </s:form>
</body>
</html>