/**
 * Main license information dialog. It also performs license validation.
 */
package com.example;

import com.license4j.License;
import com.license4j.LicenseValidator;
import com.license4j.ValidationStatus;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

public class LicenseInformationGUI extends javax.swing.JDialog {

    /**
     * The file to save floating license server and port number.
     */
    private final String licenseConfigFile = System.getProperty("user.home") + File.separator + "sample-app-floating-license-file.conf";

    private MyFloatingHandler myFloatingHandler;

    private License licenseObject;

    /**
     * Creates new form LicenseInformationGUI
     */
    public LicenseInformationGUI(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        // Set look and feel
        setLookFeel();

        initComponents();

        this.setLocationRelativeTo(parent);

        /**
         * Load config file to get floating license server and port.
         */
        loadLicenseConfigFile();
    }

    /**
     * Method to check for license.
     *
     */
    public boolean check() {
        /**
         * IMPORTANT: When validate method called and license is valid, it
         * starts timer tasks. So when you call validate method again, it starts
         * timer tasks again; and there are left many timer tasks if you don't
         * close old ones.
         *
         * Check if license object is not null, and call releaseFloatingLicense
         * method to release license and stop running timer tasks; new tasks
         * will start when validate method called.
         */
        if (licenseObject != null) {
            licenseObject.releaseFloatingLicense();
        }

        /**
         * initialize MyFloatingHandler
         */
        myFloatingHandler = new MyFloatingHandler(this);

        String host = hostnamejTextField.getText() + ":" + portnumberjTextField.getText();

        /**
         * New validateFloatingLicenseText with less arguments and new handler.
         */
        licenseObject = LicenseValidator.validateFloatingLicenseText(
                // public key. REPLACE WITH YOUR PUBLIC KEY
                "30819f300d06092a864886f70d010101050003818d003081893032301006"
                + "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0"
                + "0045716eba4393657fdd7747063488c5c98f536f60b918ae7e0ed091ec6G"
                + "0281810095b4f3e12abec7a86d2a7f2111c0da5dab76b342e3be8beba5ef"
                + "41e7efd7a67745ea6cb28473caa863919ce3576ba4376301e950237f6d99"
                + "7b1d00f7290473666b06a5bccc85357d99703cafd7ac5a94f90f77e90ef4"
                + "3912aab0a4992683cf3803RSA4102413SHA512withRSAfd8d33995e9e7df"
                + "9f983fbc124ea05c447965f7704499fb117acbf91789e2e0f0203010001",
                // product id. REPLACE WITH YOUR PRODUCT ID
                "test",
                null,
                null,
                host,
                myFloatingHandler);

        /**
         * Call updateGUI with new license object.
         */
        updateGUI(licenseObject);

        if (licenseObject.getValidationStatus() != ValidationStatus.LICENSE_VALID) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * The method to update text fields etc.
     */
    public void updateGUI(License license) {
        // First clear all fields.
        licensejTextField.setText("");
        licenseStatusjTextField.setText("");
        expirationDatejTextField.setText("");
        namejTextField.setText("");
        emailjTextField.setText("");
        companyjTextField.setText("");

        // change colors to default
        // quick fix: get the original color from the other textfield.
        licenseStatusjTextField.setDisabledTextColor(licensejTextField.getDisabledTextColor());
        expirationDatejTextField.setDisabledTextColor(licensejTextField.getDisabledTextColor());

        if (license != null) {
            if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
                if (license.getLicenseText().getLicenseExpireDate() != null) {
                    expirationDatejTextField.setText(license.getLicenseText().getLicenseExpireDate().toString());
                }

                licensejTextField.setText("License Text (ID: " + license.getLicenseText().getLicenseID() + ")");

                namejTextField.setText(license.getLicenseText().getUserFullName());
                emailjTextField.setText(license.getLicenseText().getUserEMail());
                companyjTextField.setText(license.getLicenseText().getUserCompany());
            }

            /**
             * Display ValidationStatus
             */
            licenseStatusjTextField.setText(license.getValidationStatus().toString());

            /**
             * Maybe add some color.
             */
            if (license.getValidationStatus() != ValidationStatus.LICENSE_VALID) {
                licenseStatusjTextField.setDisabledTextColor(Color.RED);
            }
        }
    }

    /**
     * Load properties from configuration file.
     *
     * You can store other type of information here.
     */
    private void loadLicenseConfigFile() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(licenseConfigFile));

            String hostname = properties.getProperty("hostname");
            String portnumber = properties.getProperty("portnumber");

            if (hostname != null) {
                hostnamejTextField.setText(hostname);
            }
            if (portnumber != null) {
                portnumberjTextField.setText(portnumber);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Save information to license configuration file.
     *
     * You can store other type of information here.
     */
    private void saveLicenseConfigFile() {
        Properties properties = new Properties();
        try {
            if (!hostnamejTextField.getText().isEmpty()) {
                properties.setProperty("hostname", hostnamejTextField.getText());
            } else {
                properties.remove("hostname");
            }

            if (!portnumberjTextField.getText().isEmpty()) {
                properties.setProperty("portnumber", String.valueOf(portnumberjTextField.getText()));
            } else {
                properties.remove("portnumber");
            }

            properties.store(new FileOutputStream(licenseConfigFile, false), null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closejButton = new javax.swing.JButton();
        validatejButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        licensejTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        expirationDatejTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        licenseStatusjTextField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        emailjTextField = new javax.swing.JTextField();
        companyjTextField = new javax.swing.JTextField();
        namejTextField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        portnumberjTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        hostnamejTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("License Validation");

        closejButton.setText("Close");
        closejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closejButtonActionPerformed(evt);
            }
        });

        validatejButton.setText("Validate");
        validatejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validatejButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("License Information"));

        jLabel1.setText("License:");

        licensejTextField.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        licensejTextField.setEnabled(false);

        jLabel2.setText("Expiration Date:");

        expirationDatejTextField.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        expirationDatejTextField.setEnabled(false);

        jLabel6.setText("License Status:");

        licenseStatusjTextField.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        licenseStatusjTextField.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(licenseStatusjTextField)
                    .addComponent(licensejTextField)
                    .addComponent(expirationDatejTextField, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jLabel2, jLabel6});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(licensejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(licenseStatusjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(expirationDatejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Licensee Information"));

        jLabel3.setText("Name:");

        jLabel4.setText("E-mail:");

        jLabel5.setText("Company:");

        emailjTextField.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        emailjTextField.setEnabled(false);

        companyjTextField.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        companyjTextField.setEnabled(false);

        namejTextField.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        namejTextField.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(emailjTextField)
                    .addComponent(namejTextField)
                    .addComponent(companyjTextField))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel3, jLabel4, jLabel5});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(namejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(emailjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(companyjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Floating License Server Information"));

        jLabel8.setText("Port Number:");

        jLabel7.setText("Host Name:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(49, 49, 49)
                .addComponent(hostnamejTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(portnumberjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(hostnamejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(portnumberjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(validatejButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closejButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(validatejButton)
                    .addComponent(closejButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closejButtonActionPerformed
        /**
         * Save configuration file when close button clicked.
         */
        saveLicenseConfigFile();

        dispose();
    }//GEN-LAST:event_closejButtonActionPerformed

    private void validatejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validatejButtonActionPerformed
        /**
         * Run license check method.
         */
        check();
    }//GEN-LAST:event_validatejButtonActionPerformed

    private void setLookFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LicenseInformationGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(LicenseInformationGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(LicenseInformationGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(LicenseInformationGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closejButton;
    private javax.swing.JTextField companyjTextField;
    private javax.swing.JTextField emailjTextField;
    private javax.swing.JTextField expirationDatejTextField;
    private javax.swing.JTextField hostnamejTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField licenseStatusjTextField;
    private javax.swing.JTextField licensejTextField;
    private javax.swing.JTextField namejTextField;
    private javax.swing.JTextField portnumberjTextField;
    private javax.swing.JButton validatejButton;
    // End of variables declaration//GEN-END:variables
}
