package com.license4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductLicense {

    /**
     * Product public key.
     */
    private String publicKey;

    /**
     * Will be used for license validation.
     */
    private String internalHiddenString;
    private String productID;
    /**
     * Activation Server, null to use Online.License4J
     */
    private String activationServer;
    /**
     * License Key
     */
    private String licenseKey;
    /**
     * License Text
     */
    private String licenseText;
    /**
     * Activated License Text
     */
    private String activatedLicenseText;
    /**
     * Properties to keep license key/text and save/load from/to disk
     */
    private Properties properties;

    /**
     * Trial license
     *
     * Only one of then should be set!
     */
    private String trialLicenseKey;
    private String trialLicenseText;

    /**
     * licenseFile file path, license and activations will be saved to.
     */
    private String licenseFile;

    public ProductLicense(
            String fileToSaveLicense,
            String publicKey,
            String trialLicenseKey,
            String trialLicenseText,
            String internalHiddenString,
            String productID,
            String activationServer
    ) {
        /**
         * Initialize Properties object.
         */
        properties = new Properties();

        this.licenseFile = fileToSaveLicense;
        this.publicKey = publicKey;
        this.trialLicenseKey = trialLicenseKey;
        this.trialLicenseText = trialLicenseText;
        this.internalHiddenString = internalHiddenString;
        this.productID = productID;
        this.activationServer = activationServer;

        // load license from file.
        loadLicense();
    }

    /**
     * Validate method, it checks for license key or license text, then
     * activated license text and validates.
     *
     * @return
     */
    public boolean validate() {
        License license = null;

        if (licenseKey != null) {
            license = validateLicenseKey();
        } else if (licenseText != null) {
            license = validateLicenseText();
        }

        if (license != null) {
            if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
                if (license.isActivationRequired()) {
                    if (activatedLicenseText != null) {
                        License activatedLicense = validateActivatedLicenseText();

                        if (activatedLicense.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
                            /**
                             * Return true only if ValidationStatus is LICENSE_VALID.
                             */
                            return true;
                        }
                    }
                } else {
                    /**
                     * Return true since activation is not required.
                     */
                    return true;
                }
            }
        }

        /**
         * By default returns false.
         */
        return false;
    }

    /**
     * Same method as validate. But this returns License object so we can update
     * LicenseInformationGUI text fields.
     *
     * @return
     */
    public License getLicenseObject() {
        License license = null;

        if (licenseKey != null) {
            license = validateLicenseKey();
        } else if (licenseText != null) {
            license = validateLicenseText();
        }

        if (license != null) {
            if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
                if (license.isActivationRequired()) {
                    if (activatedLicenseText != null) {
                        license = validateActivatedLicenseText();
                    }
                }
            }
        }

        return license;
    }

    /**
     * Validate License Key
     */
    public License validateLicenseKey() {
        License license = null;

        if (licenseKey != null) {
            license = LicenseValidator.validate(
                    licenseKey, // REQUIRED - license key
                    publicKey, // REQUIRED - public key
                    internalHiddenString, // REQUIRED IF BASIC LICENSE KEY - Internal hidden string
                    null, // Customer name for validation
                    null, // Customer's company name for validation
                    0); // Hardware ID selection
        }

        return license;
    }

    /**
     * Validate License Text
     */
    public License validateLicenseText() {
        License license = null;

        if (licenseText != null) {
            license = LicenseValidator.validate(
                    licenseText, // REQUIRED - license string
                    publicKey, // REQUIRED - public key
                    productID, // REQUIRED - product id
                    null, // product edition if needed
                    null, // product version if needed
                    null, // current date, null for current date
                    null); // product release date if needed
        }

        return license;
    }

    /**
     * Validate Activated License Text
     */
    public License validateActivatedLicenseText() {
        License license = null;

        if (activatedLicenseText != null) {
            license = LicenseValidator.validate(
                    activatedLicenseText, // REQUIRED - license string
                    publicKey, // REQUIRED - public key
                    productID, // REQUIRED - product id
                    null, // product edition if needed
                    null, // product version if needed
                    null, // current date, null for current date
                    null); // product release date if needed
        }

        return license;
    }

    public License activateLicense() {
        /**
         * First, validate license key or license text and get a valid license
         * object.
         */
        License license = null;
        if (licenseKey != null) {
            license = validateLicenseKey();
        } else if (licenseText != null) {
            license = validateLicenseText();
        }

        License activatedLicense = null;

        /**
         * After validating license key/text, if it is valid and requires
         * activation go and activate it.
         */
        if (license != null
                && license.getValidationStatus() == ValidationStatus.LICENSE_VALID
                && license.isActivationRequired()) {

            activatedLicense = LicenseValidator.autoActivate(license, activationServer);
        }

        return activatedLicense;
    }

    public boolean loadLicense() {
        /**
         * Load properties from license file.
         *
         * You can store other type of information here.
         */
        try {
            properties.load(new FileInputStream(licenseFile));

            licenseKey = properties.getProperty("license-key");
            licenseText = properties.getProperty("license-text");
            activatedLicenseText = properties.getProperty("activated-license-text");

            return true;
        } catch (IOException ex) {
            Logger.getLogger(ProductLicense.class.getName()).log(Level.SEVERE, null, ex);
            /**
             * IOException is thrown if there is no license file or license file
             * is cannot be read.
             */
        }

        return false;
    }

    /**
     * Save license information to license file.
     *
     * You can store other type of information here.
     */
    public boolean saveLicense() {
        try {
            if (licenseKey != null) {
                properties.setProperty("license-key", licenseKey);
            } else {
                properties.remove("license-key");
            }

            if (licenseText != null) {
                properties.setProperty("license-text", licenseText);
            } else {
                properties.remove("license-text");
            }

            if (activatedLicenseText != null) {
                properties.setProperty("activated-license-text", activatedLicenseText);
            } else {
                properties.remove("activated-license-text");
            }

            properties.store(new FileOutputStream(licenseFile, false), null);

            /**
             * You can also encrypt license file on disk to make it difficult to
             * read.
             */
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ProductLicense.class.getName()).log(Level.SEVERE, null, ex);
            /**
             * Cannot save license file !!!
             */
        }

        return false;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;

        /**
         * Either license key or license text can be set.
         */
        this.licenseText = null;
    }

    public String getLicenseText() {
        return licenseText;
    }

    public void setLicenseText(String licenseText) {
        this.licenseText = licenseText;

        /**
         * Either license key or license text can be set.
         */
        this.licenseKey = null;
    }

    public String getActivatedLicenseText() {
        return activatedLicenseText;
    }

    public void setActivatedLicenseText(String activatedLicenseText) {
        this.activatedLicenseText = activatedLicenseText;
    }

    public String getTrialLicenseKey() {
        return trialLicenseKey;
    }

    public String getTrialLicenseText() {
        return trialLicenseText;
    }
}
