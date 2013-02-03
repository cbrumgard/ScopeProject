<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>ServerPage</title>
</head>
<body style="vertical-align: top;">

Server Information<br/><br/>

<s:if test="%{scopeServer != null}">
    Server Running!!!
    
    <br />
    Websocket: <s:property value="%{scopeServer.getMessengingService().getWebSocketURI()}"/>
</s:if>
<s:else>
    Server not initialized!!!
</s:else>


</body>
</html>