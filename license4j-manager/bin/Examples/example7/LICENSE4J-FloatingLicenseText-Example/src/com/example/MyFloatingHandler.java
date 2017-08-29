package com.example;

import com.license4j.FloatingLicenseCheckTimerHandler;

/**
 * MyFloatingHandler extends abstract FloatingLicenseTimerTaskHandler and
 * override run() method.
 *
 * Timer task which is started after first license validation runs this as a
 * thread.
 *
 */
public class MyFloatingHandler extends FloatingLicenseCheckTimerHandler {

    /**
     * This is a dialog to display license information.
     */
    private LicenseInformationGUI licenseInformationGUI;

    public MyFloatingHandler(LicenseInformationGUI licenseInformationGUI) {
        this.licenseInformationGUI = licenseInformationGUI;
    }

    @Override
    public void run() {
        /**
         * LicenseInformationGUI has a method to update text fields according to
         * license validation status. So just call this updateGUI method to
         * update text fields.
         */
        licenseInformationGUI.updateGUI(getLicense());

        /**
         * Depending on license validation status, you can call any method here.
         */
        switch (getLicense().getValidationStatus()) {
            case LICENSE_VALID:
                // license is valid, just continue
                break;
            case LICENSE_EXPIRED:
                // license is expired, display a message to user or close application?
                System.err.println("Your license is expired.");
                break;
            case FLOATING_LICENSE_NOT_AVAILABLE_ALL_IN_USE:
                // all licenses are in use, not available.
                System.err.println("All licenses are in use.");
                break;
            case FLOATING_LICENSE_CLIENT_REJECTED:
                // client is rejected by server
                System.err.println("You are not allowed to get a license.");
                break;
            case FLOATING_LICENSE_NOT_FOUND:
                // license is not found on server, is it deleted?
                System.err.println("License cannot be found on server.");
                break;
            case FLOATING_LICENSE_SERVER_NOT_AVAILABLE:
                // license server cannot be contacted.
                System.err.println("Floating License Server is not available.");
                break;
            default:
                System.err.println(getLicense().getValidationStatus());
                break;
        }
    }

}
