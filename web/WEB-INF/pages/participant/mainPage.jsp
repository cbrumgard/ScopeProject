<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Participant Page</title>
</head>
<body>

Welcome participant <%=session.getAttribute("participantID")%> <br />

*list of sessions <br />
*join session   <br />

<br />
<form method="POST" action="participant/logout">
    <input type="submit" value="logout" />
</form>

</body>
</html>