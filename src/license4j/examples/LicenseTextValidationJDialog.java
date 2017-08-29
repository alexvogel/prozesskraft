package license4j.examples;

import static com.license4j.ActivationStatus.ACTIVATION_COMPLETED;
import static com.license4j.ActivationStatus.ACTIVATION_SERVER_CONNECTION_ERROR;
import static com.license4j.ActivationStatus.ALREADY_ACTIVATED_ON_ANOTHER_COMPUTER;
import static com.license4j.ActivationStatus.LICENSE_NOT_FOUND_ON_ACTIVATION_SERVER;
import static com.license4j.ActivationStatus.MULTIPLE_ACTIVATION_LIMIT_REACHED;
import com.license4j.License;
import com.license4j.LicenseValidator;
import static com.license4j.ValidationStatus.INCORRECT_SYSTEM_TIME;
import static com.license4j.ValidationStatus.LICENSE_EXPIRED;
import static com.license4j.ValidationStatus.LICENSE_INVALID;
import static com.license4j.ValidationStatus.LICENSE_MAINTENANCE_EXPIRED;
import static com.license4j.ValidationStatus.LICENSE_VALID;
import static com.license4j.ValidationStatus.MISMATCH_HARDWARE_ID;
import static com.license4j.ValidationStatus.MISMATCH_PRODUCT_EDITION;
import static com.license4j.ValidationStatus.MISMATCH_PRODUCT_ID;
import static com.license4j.ValidationStatus.MISMATCH_PRODUCT_VERSION;
import com.license4j.util.FileUtils;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class LicenseTextValidationJDialog extends javax.swing.JDialog {

    private License license;
    private final JFileChooser jFileChooser1;
    private String publickey =
            "30819f300d06092a864886f70d010101050003818d003081893032301006"
            + "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0"
            + "0044f19c44ee47bc7a7f61af76c1b060a750b9bee5c9907452c8f34d42fG"
            + "02818100a1f9ed4070844ab588914b06f68f49f36ba581ee589901bce5a8"
            + "66c3753f32b320313b1f7be69753d97993848feb81351bc9f9df23fb0c93"
            + "a06a56c964a2d37d216e8fb557fc1d8b5dd9c740052f66afde48b0d515fe"
            + "ce608c04ced6e11475f003RSA4102413SHA512withRSA9103c41df57a5d4"
            + "8969e961326cf1e7233f4cd1c0d7121204a6da690e21a17ab0203010001";

    public LicenseTextValidationJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocationRelativeTo(null);

        //this.setSize(600, 300);

        // Initialize file chooser.
        jFileChooser1 = new JFileChooser();

        File currentDirectory = new File(".");
        jFileChooser1.setCurrentDirectory(currentDirectory);
        jFileChooser1.setMultiSelectionEnabled(false);
        jFileChooser1.setFileSelectionMode(JFileChooser.FILES_ONLY);

        /* Hide activate button */
        activatejButton.setVisible(false);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        fulllnamejTextField = new javax.swing.JTextField();
        emailjTextField = new javax.swing.JTextField();
        expireDatejTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        validationStatusjTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        activationStatusjTextField = new javax.swing.JTextField();
        registeredTojTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        companyjTextField2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel9 = new javax.swing.JLabel();
        productNamejTextField = new javax.swing.JTextField();
        closejButton = new javax.swing.JButton();
        installLicensejButton = new javax.swing.JButton();
        activatejButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("License Text Validation");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("License Information"));

        jLabel1.setText("Full Name:");

        jLabel3.setText("Company:");

        jLabel4.setText("License Expire Date:");

        fulllnamejTextField.setEditable(false);

        emailjTextField.setEditable(false);

        expireDatejTextField.setEditable(false);

        jLabel6.setText("Validation Status:");

        validationStatusjTextField.setEditable(false);

        jLabel7.setText("Activation Status:");

        activationStatusjTextField.setEditable(false);

        registeredTojTextField.setEditable(false);

        jLabel2.setText("Registered To:");

        companyjTextField2.setEditable(false);

        jLabel5.setText("E-Mail:");

        jLabel8.setText("Activation code or license text:");

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel9.setText("Product Name:");

        productNamejTextField.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(22, 22, 22)
                            .addComponent(jLabel6))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(59, 59, 59)
                            .addComponent(jLabel3))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(productNamejTextField)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(registeredTojTextField)
                    .addComponent(activationStatusjTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                    .addComponent(validationStatusjTextField)
                    .addComponent(expireDatejTextField)
                    .addComponent(emailjTextField)
                    .addComponent(fulllnamejTextField)
                    .addComponent(companyjTextField2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(fulllnamejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(registeredTojTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(emailjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(companyjTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(productNamejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(expireDatejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(validationStatusjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(activationStatusjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 18, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        closejButton.setText("Close");
        closejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closejButtonActionPerformed(evt);
            }
        });

        installLicensejButton.setText("Install License");
        installLicensejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installLicensejButtonActionPerformed(evt);
            }
        });

        activatejButton.setText("Activate");
        activatejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activatejButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(activatejButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 354, Short.MAX_VALUE)
                        .addComponent(installLicensejButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(closejButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closejButton)
                    .addComponent(installLicensejButton)
                    .addComponent(activatejButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void installLicensejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installLicensejButtonActionPerformed
        String licenseString = null;

        int r = jFileChooser1.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File licensefile = jFileChooser1.getSelectedFile();
            try {
                licenseString = FileUtils.readFile(licensefile.getAbsolutePath());


            } catch (IOException ex) {
                Logger.getLogger(LicenseTextValidationJDialog.class.getName()).log(Level.SEVERE, null, ex);

                JOptionPane.showMessageDialog(null, "Cannot read license file.", "License File IO Error", JOptionPane.ERROR_MESSAGE);

                return;
            }

            /* Clear text area for new license */
            jTextArea1.setText("");

            license = LicenseValidator.validate(
                    licenseString, // License string read from file
                    publickey,
                    "examples", // Product ID
                    null, // Product edition
                    null, // Current product version
                    null, // Current date
                    null); // Current product release date


            switch (license.getValidationStatus()) {
                case LICENSE_VALID:
                    fulllnamejTextField.setText(license.getLicenseText().getUserFullName());
                    productNamejTextField.setText(license.getLicenseText().getLicenseProductName());
                    emailjTextField.setText(license.getLicenseText().getUserEMail());
                    registeredTojTextField.setText(license.getLicenseText().getUserRegisteredTo());
                    companyjTextField2.setText(license.getLicenseText().getUserEMail());

                    Date expireDate = license.getLicenseText().getLicenseExpireDate();
                    if (expireDate != null) {
                        expireDatejTextField.setText(new SimpleDateFormat("dd MMMMM yyyy").format(expireDate));
                    }

                    validationStatusjTextField.setForeground(Color.BLUE);
                    validationStatusjTextField.setText("VALID");

                    JOptionPane.showMessageDialog(null, "License valid.", "Information", JOptionPane.INFORMATION_MESSAGE);

                    /* Check for activation status */
                    if (license.isActivationRequired()) {
                        /* Show activate button */
                        activatejButton.setVisible(true);
                        if (license.getLicenseText().getLicenseActivationDaysRemaining(new Date()) >= 0) {
                            activationStatusjTextField.setForeground(Color.BLUE);
                            activationStatusjTextField.setText("ACTIVATION REQUIRED " + license.getLicenseText().getLicenseActivationDaysRemaining(new Date()) + " DAYS LEFT");
                        } else {
                            activationStatusjTextField.setForeground(Color.RED);
                            activationStatusjTextField.setText("ACTIVATION REQUIRED");

                        }
                    } else {
                        activationStatusjTextField.setForeground(Color.BLUE);
                        activationStatusjTextField.setText("ACTIVATION NOT REQUIRED");
                        
                        activatejButton.setVisible(false);
                    }
                    break;
                case LICENSE_INVALID:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("INVALID");

                    JOptionPane.showMessageDialog(null, "License invalid.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case LICENSE_EXPIRED:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("EXPIRED");

                    JOptionPane.showMessageDialog(null, "License expired.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case LICENSE_MAINTENANCE_EXPIRED:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("MAINTENANCE EXPIRED");

                    JOptionPane.showMessageDialog(null, "License maintenance expired.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case INCORRECT_SYSTEM_TIME:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("SYSTEM DATE INCORRECT");

                    JOptionPane.showMessageDialog(null, "System date incorrect.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case MISMATCH_HARDWARE_ID:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("INVALID HARDWARE ID");

                    JOptionPane.showMessageDialog(null, "Hardware ID mismatch.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case MISMATCH_PRODUCT_ID:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("INVALID PRODUCT ID");

                    JOptionPane.showMessageDialog(null, "Product ID mismatch.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case MISMATCH_PRODUCT_EDITION:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("INVALID PRODUCT EDITION");

                    JOptionPane.showMessageDialog(null, "Product edition mismatch.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case MISMATCH_PRODUCT_VERSION:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("INVALID PRODUCT VERSION");

                    JOptionPane.showMessageDialog(null, "Product version mismatch.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                default:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("UNKNOWN ERROR");

                    JOptionPane.showMessageDialog(null, "Unknown error.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
            }
        }
    }//GEN-LAST:event_installLicensejButtonActionPerformed

    private void closejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closejButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closejButtonActionPerformed

    private void activatejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activatejButtonActionPerformed
        /* Activate license */
        License activatedLicense = LicenseValidator.autoActivate(license);

        /* Check activation status */
        switch (activatedLicense.getActivationStatus()) {
            case ACTIVATION_COMPLETED:
                activationStatusjTextField.setForeground(Color.BLUE);
                activationStatusjTextField.setText("ACTIVATION COMPLETED");

                jTextArea1.setText(activatedLicense.getLicenseString());
                jTextArea1.setCaretPosition(0);

                break;
            case ALREADY_ACTIVATED_ON_ANOTHER_COMPUTER:
                activationStatusjTextField.setForeground(Color.RED);
                activationStatusjTextField.setText("ALREADY ACTIVATED ON ANOTHER PC");

                break;
            case MULTIPLE_ACTIVATION_LIMIT_REACHED:
                activationStatusjTextField.setForeground(Color.RED);
                activationStatusjTextField.setText("MAXIMUM ALLOWED ACTIVATION COUNT REACHED");

                break;
            case ACTIVATION_SERVER_CONNECTION_ERROR:
                activationStatusjTextField.setForeground(Color.RED);
                activationStatusjTextField.setText("ACTIVATION SERVER CONNECTION ERROR");

                break;
            case LICENSE_NOT_FOUND_ON_ACTIVATION_SERVER:
                activationStatusjTextField.setForeground(Color.RED);
                activationStatusjTextField.setText("LICENSE NOT FOUND");

                break;
            default:
                activationStatusjTextField.setForeground(Color.RED);
                activationStatusjTextField.setText("UNKNOWN ERROR");

                break;
        }
    }//GEN-LAST:event_activatejButtonActionPerformed

    public static void main(String args[]) {
        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                LicenseTextValidationJDialog dialog = new LicenseTextValidationJDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton activatejButton;
    private javax.swing.JTextField activationStatusjTextField;
    private javax.swing.JButton closejButton;
    private javax.swing.JTextField companyjTextField2;
    private javax.swing.JTextField emailjTextField;
    private javax.swing.JTextField expireDatejTextField;
    private javax.swing.JTextField fulllnamejTextField;
    private javax.swing.JButton installLicensejButton;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField productNamejTextField;
    private javax.swing.JTextField registeredTojTextField;
    private javax.swing.JTextField validationStatusjTextField;
    // End of variables declaration//GEN-END:variables
}
