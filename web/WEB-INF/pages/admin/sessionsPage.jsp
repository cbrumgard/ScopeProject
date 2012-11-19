<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Sessions Page</title>

<script type="text/javascript">

jQuery(document).ready(

	    function() 
	    {
	    	// Creates the session tabs (hidden)
	        jQuery("#session_tabs").tabs(
	                {

	                });
	    	
	    
	        for(key in adminSessionsToWatch)
	        {
	        	addSessionTab(key);
	        };
	        
	      
	    });

</script>

</head>
<body>

<div id="session_tabs">
    <ul> </ul>
</div>
</body>
</html>