package license4j.trial.example;

import com.license4j.License;
import com.license4j.LicenseValidator;
import com.license4j.ValidationStatus;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class MyProductLicense {

    /**
     * TODO: CHANGE THIS PUBLIC KEY WITH YOUR PRODUCTS PUBLIC KEY
     */
    public static final String publicKey
            = "30819f300d06092a864886f70d010101050003818d003081893032301006"
            + "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0"
            + "0044f19c44ee47bc7a7f61af76c1b060a750b9bee5c9907452c8f34d42fG"
            + "02818100a1f9ed4070844ab588914b06f68f49f36ba581ee589901bce5a8"
            + "66c3753f32b320313b1f7be69753d97993848feb81351bc9f9df23fb0c93"
            + "a06a56c964a2d37d216e8fb557fc1d8b5dd9c740052f66afde48b0d515fe"
            + "ce608c04ced6e11475f003RSA4102413SHA512withRSA9103c41df57a5d4"
            + "8969e961326cf1e7233f4cd1c0d7121204a6da690e21a17ab0203010001";

    /**
     * License Key
     */
    private String licenseKey;
    /**
     * Activated License Text
     */
    private String activatedLicenseText;
    /**
     * Properties to keep license key/text and save/load from/to disk
     */
    private Properties properties;
    /**
     * licenseFile file path TODO: WHERE DO YOU WANT TO SAVE LICENSE, DEFINE
     * HERE.
     */
    private final String licenseFile = System.getProperty("user.home") + File.separator + "sample-app-license-file.lic";

    public MyProductLicense() {
        /**
         * Initialize Properties object.
         */
        properties = new Properties();

        /**
         * Load properties from license file.
         */
        try {
            properties.load(new FileInputStream(licenseFile));

            licenseKey = properties.getProperty("license-key");
            activatedLicenseText = properties.getProperty("activated-license-text");
        } catch (IOException ex) {
            /**
             * IOException is thrown if there is no license file or license file
             * is cannot be read.
             */
        }
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
                    // TODO: REPLACE WITH YOUR INTERNAL HIDDEN STRING
                    "some-internal-string", // REQUIRED IF BASIC LICENSE KEY - Internal hidden string
                    null, // Customer name for validation
                    null, // Customer's company name for validation
                    0); // Hardware ID selection
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
                    // TODO: REPLACE WITH YOUR PRODUCT ID
                    "examples", // REQUIRED - product id
                    // TODO: REPLACE WITH YOUR PRODUCT EDITION
                    null, // product edition if needed
                    // TODO: REPLACE WITH YOUR PRODUCT VERSION
                    null, // product version if needed
                    null, // current date, null for current date
                    null); // product release date if needed
        }

        return license;
    }

    public License activateLicense() {
        /**
         * First, validate license key and get a valid license object.
         */
        License license = validateLicenseKey();

        License activatedLicense = null;

        /**
         * After validating license key, if it is valid and requires activation
         * go and activate it.
         */
        if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID
                && license.isActivationRequired()) {

            activatedLicense = LicenseValidator.autoActivate(license);
        }

        return activatedLicense;
    }

    /**
     * Save license information to license file.
     */
    public boolean saveLicense() {
        try {
            if (licenseKey != null) {
                properties.setProperty("license-key", licenseKey);
            } else {
                properties.remove("license-key");
            }

            if (activatedLicenseText != null) {
                properties.setProperty("activated-license-text", activatedLicenseText);
            } else {
                properties.remove("activated-license-text");
            }

            properties.store(new FileOutputStream(licenseFile, false), null);

            return true;
        } catch (IOException ex) {
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
    }

    public String getActivatedLicenseText() {
        return activatedLicenseText;
    }

    public void setActivatedLicenseText(String activatedLicenseText) {
        this.activatedLicenseText = activatedLicenseText;
    }

}
