/**
 * This is the main licensing dialog. Users will use this dialog to change,
 * activate and request trial license.
 */
package license4j.trial.example;

import com.license4j.ActivationStatus;
import com.license4j.License;
import com.license4j.ValidationStatus;
import static com.license4j.ValidationStatus.LICENSE_VALID;
import java.awt.Color;
import java.awt.Frame;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import license4j.complete.application.example.ProgressDialog;

public class MyProductLicenseJDialog extends javax.swing.JDialog {

    private MyProductLicense myProductLicense;
    private License license;

    /**
     * Creates new form LicenseJDialog
     */
    public MyProductLicenseJDialog(Frame parent, boolean modal, MyProductLicense myProductLicense, License license) {
        super(parent, modal);

        initComponents();

        this.setLocationRelativeTo(parent);

        this.myProductLicense = myProductLicense;
        this.license = license;

        updateGUI(license);
    }

    /**
     * This method updates GUI elements when license changed or activated.
     */
    private void updateGUI(License license) {
        /**
         * Clear all fields before updating.
         */
        licenseKeyjTextField.setText("");
        licenseValidationStatusjTextField.setText("");
        licenseActivationStatusjTextField.setText("");
        licenseExpirationDatejTextField.setText("");
        registrationNamejTextField.setText("");
        companyNamejTextField.setText("");

        if (license != null) {
            /**
             * License is not null, so there is at least a license key, so
             * display it.
             */
            licenseKeyjTextField.setText(myProductLicense.getLicenseKey());

            /**
             * Display license validation and activation status in a text field.
             */
            if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
                licenseValidationStatusjTextField.setForeground(Color.BLUE);
                licenseValidationStatusjTextField.setText("" + license.getValidationStatus());
            } else {
                licenseValidationStatusjTextField.setForeground(Color.RED);
                licenseValidationStatusjTextField.setText("" + license.getValidationStatus());
            }

            if (license.getActivationStatus() == ActivationStatus.ACTIVATION_COMPLETED) {
                licenseActivationStatusjTextField.setForeground(Color.BLUE);
                licenseActivationStatusjTextField.setText("" + license.getActivationStatus());
            } else {
                licenseActivationStatusjTextField.setForeground(Color.RED);
                licenseActivationStatusjTextField.setText("" + license.getActivationStatus());
            }

            /**
             * Only license text can store user information and details like
             * expiration date.
             */
            if (license.getLicenseText() != null) {
                registrationNamejTextField.setText(license.getLicenseText().getUserRegisteredTo());
                companyNamejTextField.setText(license.getLicenseText().getUserCompany());
                licenseExpirationDatejTextField.setText(license.getLicenseText().getLicenseExpireDate().toString());

                if (license.getLicenseText().getCustomSignedFeature("trial") != null
                        && license.getLicenseText().getCustomSignedFeature("trial").compareTo("yes") == 0) {
                    /**
                     * We set a custom signed feature named as "trial" and set
                     * its value to "yes" while generating license. If we find
                     * it, we know that this is a trial license.
                     */
                    licenseKeyjTextField.setText("TRIAL LICENSE - " + license.getLicenseText().getLicenseExpireDaysRemaining(null) + " days left");
                }

                // disable activate button, since license is already activated.
                activateLicensejButton.setEnabled(false);
            } else {
                registrationNamejTextField.setText("");
                companyNamejTextField.setText("");

                /**
                 * If it is a license key, then we can display license
                 * activation period here.
                 */
                if (license.getLicenseKey() != null) {
                    licenseExpirationDatejTextField.setText(license.getLicenseActivationDaysRemaining(null) + " days left for activation");
                }
            }

            if (license.getActivationStatus() == ActivationStatus.ACTIVATION_REQUIRED) {
                // if activation required, enable activate button
                activateLicensejButton.setEnabled(true);
            }
        } else {
            // no license found
            licenseValidationStatusjTextField.setText("LICENSE NOT FOUND");
            registrationNamejTextField.setText("");
            companyNamejTextField.setText("");

            activateLicensejButton.setEnabled(false);

            // enable request trial license button
            requestTrialLicensejButton.setEnabled(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        licenseKeyjTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        licenseValidationStatusjTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        licenseExpirationDatejTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        licenseActivationStatusjTextField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        registrationNamejTextField = new javax.swing.JTextField();
        companyNamejTextField = new javax.swing.JTextField();
        closejButton = new javax.swing.JButton();
        changeLicensejButton = new javax.swing.JButton();
        activateLicensejButton = new javax.swing.JButton();
        requestTrialLicensejButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("License");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("License Information"));

        jLabel1.setText("License Key:");

        licenseKeyjTextField.setEditable(false);

        jLabel2.setText("License Validation Status:");

        licenseValidationStatusjTextField.setEditable(false);

        jLabel5.setText("Expiration Date:");

        licenseExpirationDatejTextField.setEditable(false);

        jLabel6.setText("License Activation Status:");

        licenseActivationStatusjTextField.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(licenseValidationStatusjTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(licenseActivationStatusjTextField, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(licenseExpirationDatejTextField, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(licenseKeyjTextField))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(licenseKeyjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(licenseValidationStatusjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(licenseActivationStatusjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(licenseExpirationDatejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("User Information"));

        jLabel3.setText("Registration Name:");

        jLabel4.setText("Company Name:");

        registrationNamejTextField.setEditable(false);

        companyNamejTextField.setEditable(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(36, 36, 36)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(registrationNamejTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(companyNamejTextField))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(registrationNamejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(companyNamejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        closejButton.setText("Close");
        closejButton.setPreferredSize(new java.awt.Dimension(75, 23));
        closejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closejButtonActionPerformed(evt);
            }
        });

        changeLicensejButton.setText("Change License");
        changeLicensejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeLicensejButtonActionPerformed(evt);
            }
        });

        activateLicensejButton.setText("Activate License");
        activateLicensejButton.setEnabled(false);
        activateLicensejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activateLicensejButtonActionPerformed(evt);
            }
        });

        requestTrialLicensejButton.setText("Request Trial License");
        requestTrialLicensejButton.setEnabled(false);
        requestTrialLicensejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                requestTrialLicensejButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(activateLicensejButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(requestTrialLicensejButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeLicensejButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closejButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(closejButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(changeLicensejButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(activateLicensejButton)
                    .addComponent(requestTrialLicensejButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void changeLicensejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeLicensejButtonActionPerformed
        // Display dialog to get the key.
        String key = new ChangeBasicProductKeyJDialog(null, true).showDialog();

        if (key != null) {
            // set license key and type
            myProductLicense.setLicenseKey(key);

            // validate license
            License newLicense = myProductLicense.validateLicenseKey();

            /**
             * Get the validation status, and perform required actions.
             *
             * Here, If license is valid, it continues to run. If
             * ValidationStatus is other than LICENSE_VALID, it displays a
             * message dialog.
             */
            switch (newLicense.getValidationStatus()) {
                case LICENSE_VALID:
                    /**
                     * If license is valid, save it to file, and CLEAR activated
                     * license text field because new license key must be
                     * activated again.
                     */
                    myProductLicense.setActivatedLicenseText(null);
                    myProductLicense.saveLicense();

                    // update GUI
                    updateGUI(license);

                    break;
                default:
                    JOptionPane.showMessageDialog(null, "License error: " + license.getValidationStatus(), "License Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_changeLicensejButtonActionPerformed

    private void closejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closejButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closejButtonActionPerformed

    private void activateLicensejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activateLicensejButtonActionPerformed
        // activate button clicked.

        License activatedLicense = null;

        final ProgressDialog progress = new ProgressDialog(null, true);

        /**
         * License validation method runs in SwingWorker, you can use threads in
         * a better way.
         */
        SwingWorker worker = new SwingWorker() {
            // License license = null;

            @Override
            protected void done() {
                progress.setVisible(false);
            }

            @Override
            protected License doInBackground() {
                return myProductLicense.activateLicense();
            }
        };

        worker.execute();
        progress.setVisible(true);

        try {
            activatedLicense = (License) worker.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(MyProductLicense.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(MyProductLicense.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Basically get the activation status and show a message dialog.
        // You can make anything according to obtained activation status.
        if (activatedLicense != null
                && activatedLicense.getActivationStatus() == ActivationStatus.ACTIVATION_COMPLETED) {
            license = activatedLicense;

            // If activation is completed, set and save it.
            myProductLicense.setActivatedLicenseText(license.getLicenseString());
            myProductLicense.saveLicense();

            updateGUI(license);

            JOptionPane.showMessageDialog(null, "License successfully activated.", "License Activated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Activation error: " + activatedLicense.getActivationStatus(), "Activation Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_activateLicensejButtonActionPerformed

    private void requestTrialLicensejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_requestTrialLicensejButtonActionPerformed
        /**
         * When request trial license button is clicked, set your TRIAL license
         * key and auto activate it.
         *
         * Below license key is valid and hosted on Online.License4J examples.
         */
        String yourTrialKey = "QJ4GK-96PF7-M6CSA-AWGUE-TTQSA";

        // set license key in myProductLicense
        myProductLicense.setLicenseKey(yourTrialKey);

        // then use activateLicensejButtonActionPerformed method again to activate.
        activateLicensejButtonActionPerformed(evt);
    }//GEN-LAST:event_requestTrialLicensejButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (license != null && license.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
            // Valid license installed
        } else {
            // A valid license is not installed, so exit JVM (or use another way to close application).
            JOptionPane.showMessageDialog(this, "There is no valid license, software will exit.", "No Valid License", JOptionPane.ERROR_MESSAGE);

            System.exit(-1);
        }
    }//GEN-LAST:event_formWindowClosing
    /**
     *
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton activateLicensejButton;
    private javax.swing.JButton changeLicensejButton;
    private javax.swing.JButton closejButton;
    private javax.swing.JTextField companyNamejTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField licenseActivationStatusjTextField;
    private javax.swing.JTextField licenseExpirationDatejTextField;
    private javax.swing.JTextField licenseKeyjTextField;
    private javax.swing.JTextField licenseValidationStatusjTextField;
    private javax.swing.JTextField registrationNamejTextField;
    private javax.swing.JButton requestTrialLicensejButton;
    // End of variables declaration//GEN-END:variables
}
