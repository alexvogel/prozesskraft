
<%@ page import="com.license4j.*" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>LICENSE4J-WebApplication-Example</title>
        <style>
            PRE {font-size:10pt;line-height:12px;}
        </style>
    </head>
    <body>
        <div class="main">
            <pre>
                <%
                    if (getServletContext().getAttribute("license") != null) {
                        if (getServletContext().getAttribute("license") instanceof License) {
                            License license = (License) getServletContext().getAttribute("license");
                            out.print("<br/>");
                            out.print(license.getLicenseString());
                        }
                    } else {
                        out.print("NO LICENSE");
                    }
                %>
            </pre>
        </div>
    </body>
</html>
