<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Subject Login Page</title>

<script src="http://crypto-js.googlecode.com/svn/tags/3.0.2/build/rollups/sha1.js"></script>
<script type="text/javascript">
function generateID()
{
    // Gets the id input
    var input = document.forms["sessionForm"]["email"].value;
    
    // Removes the email field */
    document.forms["sessionForm"].removeChild(document.forms["sessionForm"]["email"]);
    
    // Calculates the sha-1 hash of the input
    var subjectID = CryptoJS.SHA1(input);
    
    // Adds the id to the form 
    var newOption   = document.createElement("input"); 
    newOption.name  = "participantID";
    newOption.type  = "hidden";
    newOption.value = subjectID;
    document.forms["sessionForm"].appendChild(newOption); 
    
    // Submit the field 
    document.forms["sessionForm"].submit();
}
</script>

</head>
<body>
	
	<%
	    String url = (String) session.getAttribute("originalPath"); 
	
	    if(StringUtils.isBlank(url))
	    {
	    	url = "participant/getMainPage";
	    }
	%>
	
	<form name="sessionForm" method="POST" action=<%=url%>>
	    Email Address: <input name="email" type="text"/><br/>
	                   <input type="button" value="submit" onclick="generateID()">
	</form>


</body>
</html>