<%-- 
    Document   : test
    Created on : Jun 9, 2017, 1:06:06 PM
    Author     : root
--%>

<%@page import="com.hcl.testing.Pp"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>
            <%
                Pp p=new Pp();
                p.Pp(application, out);
                %>
        </h1>
    </body>
</html>
