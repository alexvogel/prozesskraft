/*
 * LicenseUtils helper class to validate, activate license.

 */
package com.example;

import com.license4j.ActivationStatus;
import com.license4j.License;
import com.license4j.LicenseValidator;
import com.license4j.ValidationStatus;
import com.license4j.util.FileUtils;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;

public class LicenseUtils {

    /**
     * License file is saved to "conf" directory of running tomcat. It can be
     * saved into any other folder which tomcat process has write access.
     * License can also be saved into a database.
     */
    public static final String licenseFileOnDisk = System.getProperty("catalina.base") + File.separator + "conf" + File.separator + "app-license.lic";
    /**
     * Our unique public key for Example Web Application.
     */
    private static final String publickey = "30819f300d06092a864886f70d010101050003818d003081893032301006072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0004aebf8e1d8791b8de083dcd7f7b64725878fe2b6941ad7c4671d44f56G0281810093458f30e4bf4d3dce3a9c9aa11270b15f80cfa694d6d57e7938d9bb457223b3d2b2e7ffc4a2fcc4fe02a2a577978265bb796529554013c60128e33da33a9eebea3399a88f3c62ab82e4c8f9d69b0caf8927ab618fb9e126ae9aa869f14dc6a803RSA4102413SHA512withRSA489a9b440174fd7812fe4f2d4f9cc2b03ba937ebb23f257ebbf21b7cd120f8430203010001";

    /**
     * Variables needed for license key.
     */
    private static final String internalString = "my-hidden-string";
    private static final String nameforValidation = null;
    private static final String companyforValidation = null;
    private static final int hardwareIDMethod = 0;

    /**
     * Variables needed for license text.
     */
    private static final String productID = "example-web-application-id";
    private static final String productEdition = null;
    private static final String productVersion = null;

    public static License validate(String licenseString) {
        /**
         * a boolean variable to control license save to disk. We save license
         * to disk only if it is supplied by user. If it is read from disk which
         * was previously saved, it will not be overwritten.
         */
        boolean saveToDisk;

        /**
         * If given license string is null, read license file on disk to get a
         * previously saved license string.
         */
        if (licenseString == null) {
            saveToDisk = false;
            
            try {
                licenseString = FileUtils.readFile(licenseFileOnDisk).trim();
            } catch (IOException ex) {
                //Logger.getLogger(LicenseUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            saveToDisk = true;
            
            // trim string discard whitespaces.
            licenseString = licenseString.trim();
        }

        License license = null;

        if (licenseString != null && licenseString.length() <= 29) {
            /**
             * It is a 25 characters basic license key (separated with -
             * character).
             */

            license = validateLicenseKey(licenseString, saveToDisk);

            if (license.isActivationRequired()) {
                /**
                 * If license requires activation, silently activate license
                 * here and save it to disk.
                 */
                license = activateLicense(license, true);
            }
        } else if (licenseString != null && licenseString.length() > 29) {
            /**
             * It is a license text.
             */
            license = validateLicenseText(licenseString, saveToDisk);

            if (license.isActivationRequired()) {
                /**
                 * If license requires activation, silently activate license
                 * here and save it to disk.
                 */
                license = activateLicense(license, true);
            }
        }

        return license;
    }

    public static void delete(ServletContext sce) {
        File f = new File(licenseFileOnDisk);
        f.delete();
        sce.removeAttribute("license");
    }

    public static License validateLicenseKey(String key, boolean saveToDisk) {
        License licenseKeyObject = LicenseValidator.validate(
                key,
                publickey,
                internalString,
                nameforValidation,
                companyforValidation,
                hardwareIDMethod);

        /**
         * If key is valid and saveToDisk argument is true, then save license
         * key to license key file on disk to use on next startup.
         */
        if (saveToDisk && licenseKeyObject.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
            try {
                FileUtils.writeFile(licenseFileOnDisk, licenseKeyObject.getLicenseString());
            } catch (IOException ex) {
                System.err.println("ERROR: Cannot save validated license key to file: " + licenseFileOnDisk);
            }
        }

        /**
         * Return license key object to check validation status.
         */
        return licenseKeyObject;
    }

    public static License validateLicenseText(String text, boolean saveToDisk) {
        /**
         * If text is not null, then validate it.
         */
        License licenseTextObject = LicenseValidator.validate(
                text,
                publickey,
                productID,
                productEdition,
                productVersion,
                null,
                null);

        /**
         * If text is valid and saveToDisk argument is true, then save license
         * text to license text file on disk to use on next startup.
         */
        if (saveToDisk && licenseTextObject.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
            try {
                FileUtils.writeFile(licenseFileOnDisk, licenseTextObject.getLicenseString());
            } catch (IOException ex) {
                System.err.println("ERROR: Cannot save validated license text to file: " + licenseFileOnDisk);
            }
        }

        /**
         * Return license text object to check validation status.
         */
        return licenseTextObject;
    }

    public static License activateLicense(License license, boolean saveToDisk) {
        License activatedLicense = LicenseValidator.autoActivate(license);

        /**
         * If activation status is ACTIVATION_COMPLETED saveToDisk argument is
         * true, then save activated license to license file on disk to use on
         * next startup.
         */
        if (saveToDisk && activatedLicense.getActivationStatus() == ActivationStatus.ACTIVATION_COMPLETED) {
            try {
                FileUtils.writeFile(licenseFileOnDisk, activatedLicense.getLicenseString());
            } catch (IOException ex) {
                System.err.println("ERROR: Cannot save activated license to file: " + licenseFileOnDisk);
            }
        }

        return activatedLicense;
    }
}
