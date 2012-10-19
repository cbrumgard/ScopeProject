<%@page import="javax.naming.Context"%>
<%@page import="javax.naming.InitialContext"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Backend Storage page</title>

<script type="text/javascript">

function changeSelection()
{
	var strategy = document.getElementsByName("strategy")[0].value;
	var options = document.getElementsByName("options");
	
	document.writeln(strategy);
}

</script>



</head>
<body>

Backend storage page!!!

<br/>

<!-- Backend is not initialized -->
<s:if test="%{initialized == false}">
    Backend storage page is not initialized. <br />
    
    <s:form method="post" 
            label="Initialization Parameters" 
            action="InitializeBackendStorageAction">
        <s:select name="strategy" 
                  key="strategy"
                  label="Strategy" 
                  list="#{'SimpleStrategy':'SimpleStrategy'}" 
                  onchange="changeSelection();"
                 />
        <s:div id="options" name="options">
            <!-- Extra form stuff goes here -->
            
            <s:select label="Replication Factor"
                      key="replicationFactor" 
                      list="#{'1':'1', '2':'2', '3':'3'}" />
            
        </s:div>
        <s:submit value="initialize backend storage" /> 
    </s:form>
    
</s:if>

<s:else>

</s:else>

</body>
</html>