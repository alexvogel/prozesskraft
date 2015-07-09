/*
 * License Key GUI to validate license on defined floating license server.
 */
package com.example.licensing.OnlineLicenseKey;

import com.license4j.FloatingLicenseCheckTimerHandler;
import com.license4j.License;
import com.license4j.LicenseValidator;
import com.license4j.ValidationStatus;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.commons.io.FileUtils;

/**
 * OnlineLicenseKeyGUI.
 *
 * Example license keys found on Online.License4J
 *
 * YQIU9-IHBXI-U5LAQ-GFNED-RU2BV
 *
 * ZFIGX-9EIBB-7AILW-KNQM7-K9YRL
 *
 */
public class OnlineLicenseKeyGUI extends javax.swing.JDialog {

    /**
     * MODIFY BELOW THIS LINE ACCORDING TO YOUR PRODUCT AND LICENSE DETAILS.
     * **********************************************************************
     *
     * You should modify following variables according to your product and
     * license details. Required variables must be defined, all others optional
     * and depends on your licensing implementation.
     */
    /* REQUIRED. There is a unique public key for each product. */
    private final String publicKey
            = "30819f300d06092a864886f70d010101050003818d003081893032301006"
            + "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0"
            + "0044f19c44ee47bc7a7f61af76c1b060a750b9bee5c9907452c8f34d42fG"
            + "02818100a1f9ed4070844ab588914b06f68f49f36ba581ee589901bce5a8"
            + "66c3753f32b320313b1f7be69753d97993848feb81351bc9f9df23fb0c93"
            + "a06a56c964a2d37d216e8fb557fc1d8b5dd9c740052f66afde48b0d515fe"
            + "ce608c04ced6e11475f003RSA4102413SHA512withRSA9103c41df57a5d4"
            + "8969e961326cf1e7233f4cd1c0d7121204a6da690e21a17ab0203010001";

    /* REQUIRED. The product ID is used to validate license key. */
    private final String productID = "examples";

    /**
     * OPTIONAL. If you are using edition and version validation, you should
     * define this product's edition and version here.
     */
    private final String productEdition = null;
    private final String productVersion = null;
    /**
     * ********************************************************************
     * MODIFY ABOVE THIS LINE ACCORDING TO YOUR PRODUCT AND LICENSE DETAILS.
     */
    /**
     * This is the file in which floating license server information (ip and
     * port number) will be saved.
     */
    private final String licenseOnlineKeyOnDisk = System.getProperty("user.home") + File.separator + "MyApplicationLicenseOnlineKey.lic";

    /* License object; validate method return this object. */
    private License licenseObject;

    /* OnlineLicenseKeyGUI constructor. */
    public OnlineLicenseKeyGUI(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocationRelativeTo(parent);
    }

    @Override
    public void dispose() {
        /**
         * We easily override dispose method here to exit main application if
         * license is not valid. Maybe you can display an informative error
         * message before exiting application.
         */
        if (licenseObject != null && licenseObject.getValidationStatus() != ValidationStatus.LICENSE_VALID) {
            System.exit(-1);
        }

        super.dispose();
    }

    /**
     * Method to check for license on server.
     *
     * It connects to defined license server and tries to get the license.
     */
    public License checkLicense() {
        try {
            /**
             * We use Apache commons-io (FileUtils class) to easily read string
             * from file.
             */
            String onlineKey = FileUtils.readFileToString(new File(licenseOnlineKeyOnDisk));

            licenseObject = LicenseValidator.validateOnlineLicenseKey(
                    onlineKey,
                    publicKey,
                    productID,
                    productEdition,
                    productVersion,
                    //http://YourServer.com/algas/validateobk // If you have your own server.
                    new FloatingTimerHandler() // timer handler class is defined at the end of this file.
            );

            updateGUIFieldsWithLicenseObject();

            return licenseObject;

        } catch (IOException ex) {
            Logger.getLogger(OnlineLicenseKeyGUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        /**
         * If license cannot be obtained from server, return null.
         */
        return null;
    }

    /* This method updates fields on window with license object. */
    private void updateGUIFieldsWithLicenseObject() {
        if (licenseObject != null) {
            switch (licenseObject.getValidationStatus()) {
                case LICENSE_VALID:
                    licenseStatusjTextField.setText("VALID");
                    licenseStatusjTextField.setForeground(Color.BLUE);
                    break;
                default:
                    licenseStatusjTextField.setText(licenseObject.getValidationStatus().toString());
                    licenseStatusjTextField.setForeground(Color.red);
            }

            /**
             * Since we use a floating license text, it has details in it.
             */
            if (licenseObject.getLicenseText() != null) {
                if (licenseObject.getLicenseText().getLicenseExpireDate() != null) {
                    licenseExpirationDatejTextField.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(licenseObject.getLicenseText().getLicenseExpireDate()));

                    /* If license expire date is before current date, then set expiration date field color red. */
                    if (licenseObject.getLicenseText().getLicenseExpireDate().before(new Date())) {
                        licenseExpirationDatejTextField.setForeground(Color.red);
                    } else {
                        licenseExpirationDatejTextField.setForeground(Color.BLUE);
                    }
                }

                /* User information fields here. */
                namejTextField.setText(licenseObject.getLicenseText().getUserFullName() != null ? licenseObject.getLicenseText().getUserFullName() : "");
                emailjTextField.setText(licenseObject.getLicenseText().getUserEMail() != null ? licenseObject.getLicenseText().getUserEMail() : "");
                companyjTextField.setText(licenseObject.getLicenseText().getUserCompany() != null ? licenseObject.getLicenseText().getUserCompany() : "");
            } else {
                /**
                 * License Text is null, means it cannot be obtained from
                 * server. So we clear GUI fields.
                 */
                namejTextField.setText("");
                emailjTextField.setText("");
                companyjTextField.setText("");
            }
        } else {
            licenseStatusjTextField.setText("NO LICENSE");
            licenseStatusjTextField.setForeground(Color.red);

            /**
             * No license; clear GUI fields.
             */
            namejTextField.setText("");
            emailjTextField.setText("");
            companyjTextField.setText("");
        }
    }

    /**
     * Following "Generated Code" is generated by Netbeans form editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        licenseExpirationDatejTextField = new javax.swing.JTextField();
        licenseStatusjTextField = new javax.swing.JTextField();
        closejButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        namejTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        emailjTextField = new javax.swing.JTextField();
        companyjTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        changeProductKeyjButton = new javax.swing.JButton();
        progressjLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Licensing");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Product License Information"));

        jLabel1.setText("License Status:");

        jLabel2.setText("License Expiration Date:");

        licenseExpirationDatejTextField.setEditable(false);

        licenseStatusjTextField.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(licenseExpirationDatejTextField)
                    .addComponent(licenseStatusjTextField))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(licenseStatusjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(licenseExpirationDatejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        closejButton.setText("Close");
        closejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closejButtonActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Product is Licensed to"));

        jLabel4.setText("Name:");

        namejTextField.setEditable(false);

        jLabel5.setText("e-mail:");

        emailjTextField.setEditable(false);

        companyjTextField.setEditable(false);

        jLabel6.setText("Company:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(87, 87, 87)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(namejTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(emailjTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(companyjTextField, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(namejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(emailjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(companyjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        changeProductKeyjButton.setText("Change Product Key");
        changeProductKeyjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeProductKeyjButtonActionPerformed(evt);
            }
        });

        progressjLabel.setForeground(java.awt.Color.red);
        progressjLabel.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(progressjLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                        .addGap(97, 97, 97)
                        .addComponent(changeProductKeyjButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closejButton))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closejButton)
                    .addComponent(changeProductKeyjButton)
                    .addComponent(progressjLabel))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {changeProductKeyjButton, closejButton});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void changeProductKeyjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeProductKeyjButtonActionPerformed
        /**
         * Basically we show an input dialog to get online license key. It will
         * be better to use a JDialog with required key information input
         * fields.
         */
        String key = JOptionPane.showInputDialog(null, "Enter License Key", "License Key", JOptionPane.QUESTION_MESSAGE);

        if (key != null) {
            key = key.trim(); // trim it because user may copy/paste with spaces etc

            /**
             * Save key information on disk.
             */
            try {
                FileUtils.writeStringToFile(new File(licenseOnlineKeyOnDisk), key);
            } catch (IOException ex) {
                Logger.getLogger(OnlineLicenseKeyGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * We use a SwingWorker here because it will connect to license server
         * to get license, and it may take 1-2 seconds.
         */
        SwingWorker<License, Void> worker = new SwingWorker<License, Void>() {
            @Override
            protected void done() {
                try {
                    /**
                     * We get license object to a temporary object to check for
                     * ValidationStatus.
                     */
                    License temporaryLicenseObject = (License) get();

                    /**
                     * If license can be obtained from server, update GUI etc.
                     */
                    if (temporaryLicenseObject.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
                        /**
                         * If previously a license is obtained from server,
                         * there is a timer task running at background,
                         * therefore release previous license (it will stop
                         * timer task also).
                         */
                        if (licenseObject != null && licenseObject.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
                            licenseObject.releaseFloatingLicense();
                        }

                        licenseObject = temporaryLicenseObject;

                        updateGUIFieldsWithLicenseObject();
                    } else {
                        /**
                         * If license cannot be obtained from server, display
                         * error message.
                         */
                        JOptionPane.showMessageDialog(null, "License error: " + temporaryLicenseObject.getValidationStatus(), "License Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(OnlineLicenseKeyGUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(OnlineLicenseKeyGUI.class.getName()).log(Level.SEVERE, null, ex);
                }

                progressjLabel.setText("");

                /**
                 * Enable button again.
                 */
                changeProductKeyjButton.setEnabled(true);
            }

            @Override
            protected License doInBackground() {
                String key = "";
                /**
                 * Read key information on disk.
                 */
                try {
                    key = FileUtils.readFileToString(new File(licenseOnlineKeyOnDisk));
                } catch (IOException ex) {
                    Logger.getLogger(OnlineLicenseKeyGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                /**
                 * First validate license and get a temporary license object to
                 * check for validation status later.
                 */
                return LicenseValidator.validateOnlineLicenseKey(
                        key,
                        publicKey,
                        productID,
                        productEdition,
                        productVersion,
                        //licenseServer // If you have your own server.
                        new FloatingTimerHandler() // timer handler class is defined at the end of this file.
                );
            }
        };
        worker.execute();

        progressjLabel.setText("Validating ...");

        /**
         * It is good to disable "change product key" button while runtime
         * library tries to get license.
         */
        changeProductKeyjButton.setEnabled(false);
    }//GEN-LAST:event_changeProductKeyjButtonActionPerformed

    private void closejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closejButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closejButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton changeProductKeyjButton;
    private javax.swing.JButton closejButton;
    private javax.swing.JTextField companyjTextField;
    private javax.swing.JTextField emailjTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField licenseExpirationDatejTextField;
    private javax.swing.JTextField licenseStatusjTextField;
    private javax.swing.JTextField namejTextField;
    private javax.swing.JLabel progressjLabel;
    // End of variables declaration//GEN-END:variables
}

/**
 * This is a simple handler extending FloatingLicenseCheckTimerHandler.
 *
 * Basically, it prints license validation status on each timer run. In a
 * software product it should also close or warn user if license is not valid.
 *
 * To see FloatingTimerHandler run while testing, set 'License Check Period'
 * value to a low value (e.g. 1 minute) on step 5 in license generation wizard
 * for floating license text.
 */
class FloatingTimerHandler extends FloatingLicenseCheckTimerHandler {

    public FloatingTimerHandler() {
    }

    @Override
    public void run() {
        if (getLicense().getValidationStatus() == ValidationStatus.LICENSE_VALID) {
            /* License is valid */
        } else {
            /**
             * License is not valid, maybe server is down or administrator
             * rejected access to this client.
             *
             * Therefore, stop running your software here, OR if you like just
             * display an error message.
             */

            System.err.println("License error: " + getLicense().getValidationStatus());
        }
    }
}
