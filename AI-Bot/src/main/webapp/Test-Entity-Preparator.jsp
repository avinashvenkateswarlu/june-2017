<%-- 
    Document   : Test
    Created on : May 30, 2017, 2:14:38 PM
    Author     : root
--%>

<!DOCTYPE html>
<%@page import="com.hcl.ai_bot.common.CommonFuns"%>
<%@page import="org.json.*,java.io.*,java.util.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    </head>
    <body>
        <h1>
            
            <%
                HashMap<String,String> headers=new HashMap<String, String>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "Bearer "+application.getInitParameter("api.ai access token"));
                                String url=application.getInitParameter("api.ai base-url").toString()+"intents/2ac8f387-b23a-4a8c-b40a-2b6945d58337";
                                
                                String method="GET";


                                String parameters=null;
                                HashMap<Integer,Object> ob2=CommonFuns.requestHandler(headers, url, method, parameters);
                                out.print(ob2);
                %>
        </h1>
    </body>
</html>
