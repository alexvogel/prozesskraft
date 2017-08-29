/**
 * HelloWorldServlet. Simple servlet to display a message. It also checks for
 * license at the bottom of page.
 */
package com.example;

import com.license4j.License;
import com.license4j.ValidationStatus;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "HelloWorldServlet", urlPatterns = {"/HelloWorldServlet"})
public class HelloWorldServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        /**
         * License object is stored in servlet context.
         */
        License license = (License) getServletContext().getAttribute("license");

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet LicensingServlet</title>");
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"main.css\" />");
            out.println("</head>");
            out.println("<body>");
            out.println("<div class=\"main\">");
            out.println("<h1>Example Web Application</h1>");
            out.println("<h2>Hello World...</h2>");
            out.println("<br/><br/>");
            out.println("<p>");

            out.println("<div class=\"licenseinfo\">");
            if (license != null) {
                /**
                 * If license is not null, then print validation and activation
                 * status on page.
                 */
                out.println("License Validation Status: " + license.getValidationStatus() + "<br/>");
                out.println("License Activation Status: " + license.getActivationStatus() + "<br/>");

                if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID
                        && license.getLicenseText() != null) {
                    /**
                     * If license is valid and it is a license text, display
                     * registration name for information. Only license text can
                     * hold user information etc.
                     */
                    out.println("License Registered To: " + license.getLicenseText().getUserRegisteredTo() + "<br/>");
                    out.println("License Quantity: " + license.getLicenseText().getLicenseQuantity());
                }
            } else {
                /**
                 * LicenseUtils class validate method returns null if given
                 * license string is null.
                 */
                out.println("NO LICENSE");
            }
            out.println("</div>");

            out.println("<br/><br/>");
            out.println("<a href=\"/LICENSE4J-WebApplication-Example/\">Back to main page</a>");
            out.println("</p>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
