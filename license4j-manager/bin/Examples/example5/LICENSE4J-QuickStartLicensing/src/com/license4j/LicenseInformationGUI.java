/**
 * Main license information dialog. It also performs license validation and
 * activation tasks.
 */
package com.license4j;

import java.awt.Color;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

public class LicenseInformationGUI extends javax.swing.JDialog {

    // Product License class
    private ProductLicense productLicense;
    // return value
    private boolean returnValue;
    // file chooser for license file
    private final JFileChooser jFileChooser;

    /**
     * Creates new form MainGUI
     */
    public LicenseInformationGUI(java.awt.Frame parent, boolean modal, ProductLicense productLicense) {
        super(parent, modal);

        // Set look and feel
        setLookFeel();

        initComponents();

        this.productLicense = productLicense;

        this.setLocationRelativeTo(null);

        // Initialize file chooser.
        jFileChooser = new JFileChooser();
        jFileChooser.setMultiSelectionEnabled(false);
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    /**
     * Method to check for license.
     *
     * @return boolean
     */
    public boolean check() {
        if (productLicense.validate()) {
            returnValue = true;
        } else {
            /**
             * If license is not valid, we display this dialog.
             */
            display();
        }

        return returnValue;
    }

    public void display() {
        /**
         * Update text fields before displaying.
         */
        updateGUI(productLicense.getLicenseObject());
        super.setVisible(true);

        /**
         * We save license file when this dialog closed, actually you don't need
         * to save license file each time, you can customize and implement a
         * different method here.
         */
        productLicense.saveLicense();
    }

    private void updateGUI(License license) {
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
            if (license.getLicenseKey() != null) {
                if (productLicense.getLicenseKey() == productLicense.getTrialLicenseKey()) {
                    /**
                     * Compare with trial key, and display a TRIAL word at the
                     * end.
                     *
                     * Hide first 20 characters and display last 5.
                     */
                    licensejTextField.setText("XXXXX-XXXXX-XXXXX-XXXXX" + license.getLicenseKey().getTheKey().substring(license.getLicenseKey().getTheKey().lastIndexOf("-"), license.getLicenseKey().getTheKey().length()) + " TRIAL");
                } else {
                    licensejTextField.setText("XXXXX-XXXXX-XXXXX-XXXXX" + license.getLicenseKey().getTheKey().substring(license.getLicenseKey().getTheKey().lastIndexOf("-"), license.getLicenseKey().getTheKey().length()));
                }
            } else {
                /**
                 * If it is a license text, then it can have more information
                 * about license and customer.
                 */
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

            if (license.isActivationRequired()) {
                /**
                 * If license activation is required, display it.
                 */
                licenseStatusjTextField.setText(licenseStatusjTextField.getText() + " - ACTIVATION REQUIRED");
                expirationDatejTextField.setText(license.getLicenseActivationDaysRemaining(null) + " days left for activation");

                licenseStatusjTextField.setDisabledTextColor(Color.RED);

                /**
                 * Enable activation button.
                 */
                activatejButton.setEnabled(true);

                if (license.getLicenseActivationDaysRemaining(null) <= 0) {
                    /**
                     * If allowed activation period is passed, return false to
                     * check method; so customer must activate license.
                     */
                    returnValue = false;

                    expirationDatejTextField.setDisabledTextColor(Color.RED);
                }
            } else {
                activatejButton.setEnabled(false);
            }
            if (license.isActivationCompleted()) {
                /**
                 * If activation is already completed, display it.
                 */
                licenseStatusjTextField.setText(licenseStatusjTextField.getText() + " - ACTIVATION COMPLETED");

                activatejButton.setEnabled(false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        step1jPanel = new javax.swing.JPanel();
        closejButton = new javax.swing.JButton();
        activatejButton = new javax.swing.JButton();
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
        changeLicensejButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("License Validation");

        closejButton.setText("Close");
        closejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closejButtonActionPerformed(evt);
            }
        });

        activatejButton.setText("Activate");
        activatejButton.setEnabled(false);
        activatejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activatejButtonActionPerformed(evt);
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

        changeLicensejButton.setText("Change License");
        changeLicensejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeLicensejButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout step1jPanelLayout = new javax.swing.GroupLayout(step1jPanel);
        step1jPanel.setLayout(step1jPanelLayout);
        step1jPanelLayout.setHorizontalGroup(
            step1jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(step1jPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(step1jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(step1jPanelLayout.createSequentialGroup()
                        .addComponent(activatejButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 222, Short.MAX_VALUE)
                        .addComponent(changeLicensejButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closejButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(step1jPanelLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(1, 1, 1))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        step1jPanelLayout.setVerticalGroup(
            step1jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, step1jPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(step1jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closejButton)
                    .addComponent(activatejButton)
                    .addComponent(changeLicensejButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(step1jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(step1jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closejButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closejButtonActionPerformed

    private void activatejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activatejButtonActionPerformed
        final ProgressDialog progress = new ProgressDialog(null, true);

        /**
         * Activation is performed in SwingWorker thread because it may last a
         * few seconds.
         */
        SwingWorker worker = new SwingWorker() {
            License license = null;

            @Override
            protected void done() {
                progress.setVisible(false);

                switch (license.getActivationStatus()) {
                    case ACTIVATION_COMPLETED:
                        /**
                         * Since activation completed, we set
                         * activatedLicenseText.
                         */
                        productLicense.setActivatedLicenseText(license.getLicenseString());
                        updateGUI(license);

                        /**
                         * Activation is completed, BUT license may not be
                         * valid. e.g. It may be an already expired license. So
                         * return true if only activated license is valid.
                         */
                        if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
                            returnValue = true;
                        } else {
                            returnValue = false;
                        }

                        JOptionPane.showMessageDialog(null, license.getActivationStatus(), "License Activation OK", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, license.getActivationStatus(), "License Activation Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            protected Object doInBackground() {
                license = productLicense.activateLicense();

                return null;
            }
        };

        worker.execute();
        progress.setVisible(true);
    }//GEN-LAST:event_activatejButtonActionPerformed

    private void changeLicensejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeLicensejButtonActionPerformed
        /**
         * change license button is clicked, display license input gui.
         */
        LicenseInputGUI licenseInputGUI = new LicenseInputGUI(null, true, productLicense);
        License license = licenseInputGUI.display();

        if (license != null) {
            switch (license.getValidationStatus()) {
                case LICENSE_VALID:
                    /**
                     * If license is valid, return true and update gui.
                     */
                    returnValue = true;

                    /**
                     * Since this is a new license, clear out old activated
                     * license text.
                     */
                    productLicense.setActivatedLicenseText(null);

                    updateGUI(license);

                    JOptionPane.showMessageDialog(null, license.getValidationStatus(), "License OK", JOptionPane.INFORMATION_MESSAGE);
                    break;
                default:
                    returnValue = false;

                    JOptionPane.showMessageDialog(null, license.getValidationStatus(), "License Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_changeLicensejButtonActionPerformed

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
    private javax.swing.JButton activatejButton;
    private javax.swing.JButton changeLicensejButton;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField licenseStatusjTextField;
    private javax.swing.JTextField licensejTextField;
    private javax.swing.JTextField namejTextField;
    private javax.swing.JPanel step1jPanel;
    // End of variables declaration//GEN-END:variables
}
