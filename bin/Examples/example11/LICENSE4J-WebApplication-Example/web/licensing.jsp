<%@ page import="com.license4j.*" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>LICENSE4J-WebApplication-Example Licensing</title>
        <link rel="stylesheet" type="text/css" href="main.css" />
    </head>
    <body>
        <div class="main">
            <h1>Example Web Application</h1>

            <h2>Licensing Page</h2>
            <p>
                Paste your license key or license text below to install. A textarea
                is given to support both a license key and a license text. Other input
                fields can be used depending on selected license type.
            </p>

            <br />

            <form action="LicensingServlet" method="post">
                <textarea style="width:500px;height:200px;" name="licenseString"></textarea><br/>
                <input type="submit" value="Submit">
            </form>

            <div class="licenseinfo">
                <%
                    if (getServletContext().getAttribute("license") != null) {
                        if (getServletContext().getAttribute("license") instanceof License) {
                            License license = (License) getServletContext().getAttribute("license");
                            out.print("License Validation Status: " + license.getValidationStatus());
                            out.print("<br/>");
                            out.print("License Activation Status: " + license.getActivationStatus());

                            /**
                             * If license is valid and it is a license text,
                             * display registration name for information. Only
                             * license text can hold user information etc.
                             */
                            if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID
                                    && license.getLicenseText() != null) {
                                out.print("<br/>");
                                out.print("License Registered To: " + license.getLicenseText().getUserRegisteredTo());
                                out.print("<br/>");
                                out.print("License Quantity: " + license.getLicenseText().getLicenseQuantity());
                            }
                        }
                    } else {
                        out.print("NO LICENSE");
                    }
                %>
                <br/><br/>
                <%
                    if (getServletContext().getAttribute("license") != null) {
                %>
                <form action="LicensingServlet" method="post">
                    <input type="hidden" name="delete" value="delete"/>
                    <input type="submit" value="Delete Current License">
                    <%
                        }
                    %>

                    <br /><br/>
                    <a href="/LICENSE4J-WebApplication-Example/">Back to main page</a>
                </form>
                </span>
            </div>
        </div>
    </body>
</html>
