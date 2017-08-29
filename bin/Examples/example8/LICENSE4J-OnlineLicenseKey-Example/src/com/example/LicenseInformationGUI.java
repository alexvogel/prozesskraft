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
import javax.swing.SwingWorker;
import javax.swing.UIManager;

public class LicenseInformationGUI extends javax.swing.JDialog {

    /**
     * The file to save floating license server and port number.
     */
    private final String licenseConfigFile = System.getProperty("user.home") + File.separator + "sample-app-floating-online-file.conf";

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

        final ProgressDialog progress = new ProgressDialog(null, true);

        SwingWorker worker = new SwingWorker() {
            boolean licenseok;

            @Override
            protected void done() {
                progress.setVisible(false);
            }

            @Override
            protected Object doInBackground() {
                try {
                    /**
                     * New validateFloatingLicenseText with less arguments and
                     * new handler.
                     */
                    licenseObject = LicenseValidator.validateOnlineLicenseKey(
                            // Example Online License key on Online.License4J, it works. 
                            keyjTextField.getText(),
                            // public key. REPLACE WITH YOUR PUBLIC KEY
                            "30819f300d06092a864886f70d010101050003818d003081893032301006\n"
                            + "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n"
                            + "0044f19c44ee47bc7a7f61af76c1b060a750b9bee5c9907452c8f34d42fG\n"
                            + "02818100a1f9ed4070844ab588914b06f68f49f36ba581ee589901bce5a8\n"
                            + "66c3753f32b320313b1f7be69753d97993848feb81351bc9f9df23fb0c93\n"
                            + "a06a56c964a2d37d216e8fb557fc1d8b5dd9c740052f66afde48b0d515fe\n"
                            + "ce608c04ced6e11475f003RSA4102413SHA512withRSA9103c41df57a5d4\n"
                            + "8969e961326cf1e7233f4cd1c0d7121204a6da690e21a17ab0203010001",
                            "examples", // product id. REPLACE WITH YOUR PRODUCT ID
                            null, // product edition
                            null, // product vesion
                            myFloatingHandler);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return null;
            }
        };

        worker.execute();
        progress.setVisible(true);

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
        usecountjTextField.setText("");
        usetimejTextField.setText("");

        // change colors to default
        // quick fix: get the original color from the other textfield.
        licenseStatusjTextField.setDisabledTextColor(licensejTextField.getDisabledTextColor());
        expirationDatejTextField.setDisabledTextColor(licensejTextField.getDisabledTextColor());

        if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
            if (license.getLicenseText().getLicenseExpireDate() != null) {
                expirationDatejTextField.setText(license.getLicenseText().getLicenseExpireDate().toString());
            }

            licensejTextField.setText("License Text (ID: " + license.getLicenseText().getLicenseID() + ")");

            namejTextField.setText(license.getLicenseText().getUserFullName());
            emailjTextField.setText(license.getLicenseText().getUserEMail());
            companyjTextField.setText(license.getLicenseText().getUserCompany());

            usecountjTextField.setText(license.getUseCountCurrent() + " / " + license.getUseCountAllowed());
            usetimejTextField.setText(license.getUseTimeCurrent() / 60 + " mins / " + license.getUseTimeLimitAllowed() / 60 + " mins");
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

    /**
     * Load properties from configuration file.
     *
     * You can store other type of information here.
     */
    private void loadLicenseConfigFile() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(licenseConfigFile));

            String key = properties.getProperty("key");

            if (key != null) {
                keyjTextField.setText(key);
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
            if (!keyjTextField.getText().isEmpty()) {
                properties.setProperty("key", keyjTextField.getText());
            } else {
                properties.remove("key");
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
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        usecountjTextField = new javax.swing.JTextField();
        usetimejTextField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        emailjTextField = new javax.swing.JTextField();
        companyjTextField = new javax.swing.JTextField();
        namejTextField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        keyjTextField = new javax.swing.JTextField();
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

        jLabel8.setText("Use Count/Allowed:");

        jLabel9.setText("Use Time/Allowed:");

        usecountjTextField.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        usecountjTextField.setEnabled(false);

        usetimejTextField.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        usetimejTextField.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(licenseStatusjTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                            .addComponent(licensejTextField)
                            .addComponent(expirationDatejTextField, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addComponent(usetimejTextField))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(usecountjTextField)))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(usecountjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(usetimejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Online License Key Information"));

        keyjTextField.setText("URRQ9-6ZWF4-UQ5LE-QDKXB-S2QU3");

        jLabel7.setText("Online License Key:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(keyjTextField)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(keyjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addGap(0, 338, Short.MAX_VALUE)
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField keyjTextField;
    private javax.swing.JTextField licenseStatusjTextField;
    private javax.swing.JTextField licensejTextField;
    private javax.swing.JTextField namejTextField;
    private javax.swing.JTextField usecountjTextField;
    private javax.swing.JTextField usetimejTextField;
    private javax.swing.JButton validatejButton;
    // End of variables declaration//GEN-END:variables
}
