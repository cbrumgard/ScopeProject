<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Scope Admin Page</title>
   
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.9.0/themes/base/jquery-ui.css" />
    <script src="http://code.jquery.com/jquery-1.8.2.js"></script>
    <script src="http://code.jquery.com/ui/1.9.0/jquery-ui.js"></script>
    
   
    <!-- Functions -->
    <script src="/ScopeProject/resources/js/admin.js"></script>
    
    <style type="text/css">
/* 	body  */
/* 	{ */
/* 		font-family: "Trebuchet MS", "Helvetica", "Arial", "Verdana", */
/* 			"sans-serif"; */
/* 		font-size: 80%; */
/* 	} */
	
	h2.center 
	{
		text-align: "center";
	}
	
	.ui-menu 
	{
		width: 200px;
		margin-bottom: 2em;
		position:relative;
        z-index:10000;
	}
	
	div.menu 
	{
		width: 200px;
		background-color: white;
		float: left;
	}
	
	div.main 
	{
		/*width: 600px; */
		height: 100%;
		margin-left: 20px;
		background-color: white;
		float: left;
	}
	
	div.clear 
	{
		clear: both;
	}
	
	body.main 
	{
	    font-family: "Trebuchet MS", "Helvetica", "Arial", "Verdana",
            "sans-serif";
        font-size: 80%;
        
		background-color: #e1ddd9;
		font-size: 12px;
		color: #564b47;
		padding: 20px;
		margin: 0px;
		text-align: center;
	}
	
	div.center_panel 
	{
		text-align: left;
		vertical-align: middle;
		margin: 0px auto;
		padding: 0px;
		width: 800px;
		background-color: #ffffff;
		border: 1px;
		height: 600px;
	}
	</style>
    
</head>

<body class="main" background="/ScopeProject/resources/images/charcoal-gray-parchment-paper-texture.jpg">


    <div class="center_panel ui-widget-header ui-corner-all" style="background:white;">


    <!-- login dialog -->
    <div id="login-dialog" title="Login Required">
        <form>
            <table>
                <tr><td><label for="login-dialog-username">Username:</label></td>
                    <td><input type="text"     id="login-dialog-username"/></td>
                </tr>
                <tr>
                    <td><label for="login-dialog-password">Password:</label></td>
                    <td><input type="password" id="login-dialog-password"/></td>
                </tr>
            </table>
            <input type="hidden"   id="login-dialog-url"/>
        </form>
    </div>
    
    <!-- Error dialog -->
    <div id="error-dialog" title="Error Message">
        <p id="error-dialog-message"></p>
    </div>

    <!-- Stacktrace -->
    <div id="stacktrace-dialog" title="Stacktrace">
        <p id="stacktrace-dialog-message"></p>
    </div>
    
    <!-- Title -->
    <br />
    <h1 class="center" style='text-align: center;'>Admin Interface</h1>
    <hr width="80px">
    <br />
    
    <table>
        <tr>
            <td style="vertical-align: top;">
			    <!-- Menu area -->
				<div class="menu">
				    <ul id="menu">
				        <li>
				           <a href="#serverPage">Server</a>
				           <ul>
				               <li><a href="#serverPage">View</a></li>
				               <li><a href="#configureServerPage">Configure</a></li>
				           </ul>
				        </li>
			            <li>
			                <a href="#backendstorage">Storage</a>
			                <ul>
			                    <li><a href="#backendstorage">Configure</a></li>
			                </ul>
			            </li>
				        <li>
				           <a href="#createSession">Session</a>
				           <ul>
				               <li><a href="#createSession">Create</a>
				           </ul>
				        </li>
				        <li><a href="/ScopeProject/admin/logout.action" >Logout</a></li>
				    </ul> 
				</div>
	       </td>
           <td style="vertical-align: top;">
                <!-- Main area -->
                <div class="main" id="main_div" style="vertical-align: top;">
                </div>
           </td>
        </tr>   
    </table>
    
    <!-- <div class="clear"></div> -->
    
    </div>
</body>
</html>