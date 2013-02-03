<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Initialize server page</title>

    <script type="text/javascript">
    
    function getInterfaces()
    {
    	jQuery('#getInterfacesProgressWheelImg').toggle();
    	
        jQuery.ajax(
            {
                url:      "/ScopeProject/admin/getNetworkInterfacesAction",
                async:    true,
                success: function(dataObject, textStatus, jqXHR)
                {
                    console.log(textStatus);
                    console.log(dataObject);
                    
                    console.log(dataObject.msgType);
                    
                    switch(dataObject.msgType)
                    {
                       case "login":
                          jQuery("#login-dialog-url").val(dataObject.data);
                          jQuery("#login-dialog").dialog("open");
                          break;
                          
                       case "stackTrace":
                          jQuery("#stacktrace-dialog-message").html(dataObject.data);
                          jQuery("#stacktrace-dialog").dialog("open");
                          break;
                          
                       case "error":
                          console.log(dataObject.data);
                          jQuery("#error-dialog-message").html(dataObject.data);
                          jQuery("#error-dialog").dialog("open");
                          break;
                          
                       case "data":
                           
                           console.log("Inside of data case");
                            
                           jQuery("#hostname-select").empty();
                           
                           for(var hostname in dataObject.data) 
                           {
                               console.log(hostname);
                               
                               jQuery("#hostname-select").append(
                            		 "<option value='"+hostname+"'>"+hostname+"</option>");
                           }
                           
                           jQuery('#getInterfacesProgressWheelImg').toggle();
                           
                           break;
                    }
                }
                
             });
    }
    
    
    function submitForm()
    {
    	console.log("submitForm");
    	
    	var hostname = jQuery("#hostname-select").val();
    	var port     = jQuery("#port").val();
    	
    	console.log("hostname = "+hostname+" port = "+port);
    	
    	jQuery("#progress-dialog").dialog("open");
    	
    	
    	//jQuery("#progressbar").progressbar(
        //        {
        //           value: 50
        //        });
    	
    	var p = { hostname: hostname, port: port};
    	console.log(p);
    	
    	jQuery.ajax(
    		   {
    			   url: "/ScopeProject/admin/startServerAction",
    			   async: true,
    			   type: "POST",
    			   data: { hostname: hostname, port: port},
    		   
    		       success: function(data, textStatus, jqXHR)
    		       {
    		    	   jQuery("#progress-dialog").dialog("close");
    		    	   
    		    	   processJSONResult(data, function(dataObject) 
   		    			   { 
   		    		           // Success
   		    		           getTestPage('/ScopeProject/admin/getServerStatusPage', {});
   		    			   });   
    		       }
    		   });
    	
    }
    
    jQuery(function() 
        {
    	   jQuery("#progress-dialog").dialog(
    			   {
    				   autoOpen: false,
    				   modal: true,
    			   });   
        });
    
    jQuery(document).ready(function() 
        { 
           console.log("HERE"); 
           getInterfaces(); 
        });
    </script>

</head>
<body>


<div id="progress-dialog" title="Starting Server">
    <img src="/ScopeProject/resources/images/progress_wheel.gif" /><br/>
    Waiting on the server...
    <!-- <div id="progressbar"></div>  -->
</div>

Enter the following information and press start
to run the server.

<form method="post" action="JavaScript:submitForm()">
    <table>
        <tr>
           <td>Hostname:</td>
           <td>
                <select id="hostname-select" style="width: 100%">
                    <option>Fetching...</option>
                </select>
           </td>
           <td style='vertical-align: middle;text-align: center;'>
                <img id='getInterfacesProgressWheelImg' 
                     style='display: none;'
                     src="/ScopeProject/resources/images/progress_wheel.gif" 
                     height="33%"/>
           </td>
        </tr>
        <tr>
            <td>Port:</td>
            <td><input type="text" id="port" value="5001"/></td>
        </tr>
        <tr>
            <td></td>
            <td><input type="submit" /></td>
        </tr>
    </table>
</form>

</body>
</html>