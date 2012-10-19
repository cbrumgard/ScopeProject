<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Session creation</title>

    <script src="http://code.jquery.com/jquery-latest.min.js"></script> 

 <div id="fillin" 
    EBA='<s:textfield label="Participant Count" key="participantCount" /><br/>
         <s:textfield label="initialNumNodes" key="initialNumNodes"/><br/>
         <s:textfield label="M variable" key="m" /><br/>
         <s:textfield label="P probability" key="p"/><br/>
         <s:textfield label="Q probability" key="q"/><br/>
         <s:submit />'
          
     USER='<s:file name="user" key="user" label="file" /><br /> 
           <s:submit />'
 />
         


</head>
<body>

 
    <script type="text/javascript">
    
    function validateEBA()
    {
    	
    }
    
    
    function test()
    {
        var graphSelect = document.getElementById("graphSelect");
        var graphType   = graphSelect.options[graphSelect.selectedIndex].value;
     
        var form = document.getElementById("extraFormData");
        
        /* Extended BA */
        if(graphType == "EBA")
        {
        	form.innerHTML = document.getElementById("fillin").attributes["EBA"].value;
        	
        /* User input graph */
        }else if(graphType == "USER") 
        {
        	form.innerHTML = document.getElementById("fillin").attributes["USER"].value;
		}  
    }
    </script>

   
    
    <!-- Form for graph entry parameters -->
    <s:form id="createSessionForm" method="post" enctype="multipart/form-data" 
        action="admin/createSession">
        <table>
        <tr>
        <td>
        <s:textfield label="Session Name" key="sessionName" /><br />
        <s:select id="graphSelect" label="Select Graph Type" key="graphType" 
                  onchange="test()" 
                  list='#{"":"", "EBA":"EBA", "USER":"User"}' />
        </td>
        </tr>
        
        <tr><td id="extraFormData" />
        </tr>
        </table>
    </s:form>
   
</body>
</html>