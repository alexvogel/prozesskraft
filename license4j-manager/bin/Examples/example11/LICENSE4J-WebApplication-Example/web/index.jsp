<%@ page import="com.license4j.*" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>LICENSE4J-WebApplication-Example</title>
        <link rel="stylesheet" type="text/css" href="main.css" />
    </head>
    <body>
        <div class="main">
            <h1>Example Web Application</h1>

            <h2>Some Text etc...</h2>
            <p>
                Lorem ipsum dolor sit amet, eu nam iuvaret adipisci explicari, 
                nulla congue concludaturque has in, sed at duis omnis simul. 
                Ignota iuvaret appareat pro ne, intellegat mnesarchum cum ne. 
                In simul admodum ius, ex diam constituto temporibus vis.
                Et illum inermis tacimates vix, has te summo semper. 
                Purto definiebas interpretaris vix ne, accumsan consequat.
            </p>
            <p>
                Sample
                <a href="/LICENSE4J-WebApplication-Example/HelloWorldServlet">HelloWorldServlet</a>.
            </p>

            <p>
                Sample licenses for testing is on.
                <a href="/LICENSE4J-WebApplication-Example/sample-licenses.jsp">sample licenses page</a>.
            </p>

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

                        out.print("<br/><a href=\"\" onClick=\"window.open('view-license.jsp','view license','width=500,height=400')\">View License String</a>");
                    } else {
                        out.print("NO LICENSE");
                    }
                %>
                <br/><br/>
                <a href="licensing.jsp">Licensing Page</a>
                </span>
            </div>
    </body>
</html>
