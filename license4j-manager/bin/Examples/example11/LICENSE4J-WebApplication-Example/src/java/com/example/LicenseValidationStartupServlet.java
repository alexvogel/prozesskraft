/**
 * LicenseValidationStartupServlet.
 *
 */
package com.example;

import com.license4j.License;
import com.license4j.ValidationStatus;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;

@WebListener
@WebServlet(loadOnStartup = 1)
public class LicenseValidationStartupServlet implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        /**
         * When context is initialized we check the license on disk.
         */
        License license = LicenseUtils.validate(null);

        /**
         * For demonstration, print some license info to stdout.
         */
        System.out.println("----------------------------------------------------");
        if (license != null) {
            System.out.println("License Validation Status: " + license.getValidationStatus());
            System.out.println("License Activation Status: " + license.getActivationStatus());

            if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID
                    && license.getLicenseText() != null) {
                /**
                 * If license is valid and it is a license text, display
                 * registration name. Only license text can hold user
                 * information etc.
                 */
                System.out.println("License Registered To: " + license.getLicenseText().getUserRegisteredTo());

                /**
                 * License quantity can be used to limit number of user
                 * connections or number of user registrations on web
                 * application. HTTPSession can easily be used to track number
                 * of connected users then compared to license quantity.
                 */
                System.out.println("License Quantity: " + license.getLicenseText().getLicenseQuantity());
            }

            System.out.println("License File: " + LicenseUtils.licenseFileOnDisk);

            /**
             * After all validation check, if license is valid, we set an
             * attribute with name 'license' in context to access License object
             * from other servlets or JSP pages.
             */
            sce.getServletContext().setAttribute("license", license);
        } else {
            System.out.println("NO LICENSE FOUND");
        }
        System.out.println("----------------------------------------------------");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
