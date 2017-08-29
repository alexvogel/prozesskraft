package license4j.examples;

import com.license4j.DefaultFloatingLicenseInvalidHandlerImpl;
import com.license4j.DefaultFloatingLicenseServerConnectionErrorHandlerImpl;
import com.license4j.License;
import com.license4j.LicenseValidator;
import static com.license4j.ValidationStatus.INCORRECT_SYSTEM_TIME;
import static com.license4j.ValidationStatus.LICENSE_EXPIRED;
import static com.license4j.ValidationStatus.LICENSE_INVALID;
import static com.license4j.ValidationStatus.LICENSE_MAINTENANCE_EXPIRED;
import static com.license4j.ValidationStatus.LICENSE_VALID;
import static com.license4j.ValidationStatus.MISMATCH_PRODUCT_EDITION;
import static com.license4j.ValidationStatus.MISMATCH_PRODUCT_VERSION;
import java.awt.Color;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class FloatingLicenseValidationJDialog extends javax.swing.JDialog {

    private String publickey
            = "30819f300d06092a864886f70d010101050003818d003081893032301006"
            + "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0"
            + "004bc2deef368ffe0e89c598855e74bc760aeb5c173a980cde5ecaba6deG"
            + "02818100ac3ca4bffe8bcf3e6f63ee7c72c8af6564bdbcd9530c4baa3a33"
            + "9120791bb33c5322db82e2810a198d479f805c40977a26466b59ad5554c2"
            + "3c8a114b9c1f2ff8cb1efb0e7b4dcc2ec1207aab4417797b67724e48a26c"
            + "70f905a275a7eed13b2b03RSA4102413SHA512withRSA8ee62ed51ff3e1c"
            + "4d4853bfaa40a500be77c73e0d72d98c26b38a45482faba3f0203010001";

    public FloatingLicenseValidationJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocationRelativeTo(null);
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
        registeredTojTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        companyjTextField2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        productNamejTextField = new javax.swing.JTextField();
        closejButton = new javax.swing.JButton();
        getLicensejButton = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        serverjTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        portjTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Floating License Text Validation");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("License Information"));

        jLabel1.setText("Full Name:");

        jLabel3.setText("Company:");

        jLabel4.setText("License Expire Date:");

        fulllnamejTextField.setEditable(false);

        emailjTextField.setEditable(false);

        expireDatejTextField.setEditable(false);

        jLabel6.setText("Validation Status:");

        validationStatusjTextField.setEditable(false);

        registeredTojTextField.setEditable(false);

        jLabel2.setText("Registered To:");

        companyjTextField2.setEditable(false);

        jLabel5.setText("E-Mail:");

        jLabel8.setText("License Text Obtained From Flotaing License Server:");

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel7.setText("Product Name:");

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
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(productNamejTextField)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(0, 207, Short.MAX_VALUE))
                    .addComponent(registeredTojTextField)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(productNamejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(expireDatejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(validationStatusjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        getLicensejButton.setText("Get License");
        getLicensejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getLicensejButtonActionPerformed(evt);
            }
        });

        jLabel9.setText("Floating License Server:");

        serverjTextField.setText("localhost");

        jLabel10.setText("Port:");

        portjTextField.setText("16090");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serverjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(portjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(getLicensejButton)
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
                    .addComponent(getLicensejButton)
                    .addComponent(jLabel9)
                    .addComponent(serverjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(portjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void getLicensejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getLicensejButtonActionPerformed
        String addr = serverjTextField.getText();
        String port = portjTextField.getText();

        if (addr == null || addr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Server name is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (port == null || port.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Port number is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            InetAddress host = InetAddress.getByName(addr);
            int portnumber = Integer.parseInt(port);

            License license = LicenseValidator.validate(
                    publickey,
                    "example-id", // Product ID
                    "professional", // Product edition
                    "1.0", // Current product version
                    null, // Current date
                    null, // Current product release date
                    host,
                    portnumber,
                    null,
                    new DefaultFloatingLicenseInvalidHandlerImpl("License Invalid, System.exit will be called.", true),
                    new DefaultFloatingLicenseServerConnectionErrorHandlerImpl("Server Connection Error, System.exit will be called.", true));

            switch (license.getValidationStatus()) {
                case LICENSE_VALID:
                    fulllnamejTextField.setText(license.getLicenseText().getUserFullName());
                    productNamejTextField.setText(license.getLicenseText().getLicenseProductName());
                    emailjTextField.setText(license.getLicenseText().getUserEMail());
                    registeredTojTextField.setText(license.getLicenseText().getUserRegisteredTo());
                    companyjTextField2.setText(license.getLicenseText().getUserCompany());

                    if (license.getLicenseText().getLicenseExpireDate() != null) {
                        expireDatejTextField.setText(new SimpleDateFormat("dd MMMMM yyyy").format(license.getLicenseText().getLicenseExpireDate()));
                    }

                    validationStatusjTextField.setForeground(Color.BLUE);
                    validationStatusjTextField.setText("VALID");

                    jTextArea1.setText(license.getLicenseString());

                    JOptionPane.showMessageDialog(null, "License valid.", "Information", JOptionPane.INFORMATION_MESSAGE);

                    break;
                case LICENSE_INVALID:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("INVALID");

                    JOptionPane.showMessageDialog(null, "License invalid.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case LICENSE_EXPIRED:
                    fulllnamejTextField.setText(license.getLicenseText().getUserFullName());
                    productNamejTextField.setText(license.getLicenseText().getLicenseProductName());
                    emailjTextField.setText(license.getLicenseText().getUserEMail());
                    registeredTojTextField.setText(license.getLicenseText().getUserRegisteredTo());
                    companyjTextField2.setText(license.getLicenseText().getUserCompany());

                    if (license.getLicenseText().getLicenseExpireDate() != null) {
                        expireDatejTextField.setText(new SimpleDateFormat("dd MMMMM yyyy").format(license.getLicenseText().getLicenseExpireDate()));
                    }

                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("EXPIRED");

                    jTextArea1.setText(license.getLicenseString());

                    JOptionPane.showMessageDialog(null, "License expired.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case LICENSE_MAINTENANCE_EXPIRED:
                    fulllnamejTextField.setText(license.getLicenseText().getUserFullName());
                    productNamejTextField.setText(license.getLicenseText().getLicenseProductName());
                    emailjTextField.setText(license.getLicenseText().getUserEMail());
                    registeredTojTextField.setText(license.getLicenseText().getUserRegisteredTo());
                    companyjTextField2.setText(license.getLicenseText().getUserCompany());

                    if (license.getLicenseText().getLicenseExpireDate() != null) {
                        expireDatejTextField.setText(new SimpleDateFormat("dd MMMMM yyyy").format(license.getLicenseText().getLicenseExpireDate()));
                    }

                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("MAINTENANCE EXPIRED");

                    JOptionPane.showMessageDialog(null, "License maintenance expired.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case INCORRECT_SYSTEM_TIME:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("SYSTEM DATE INCORRECT");

                    JOptionPane.showMessageDialog(null, "System date incorrect.", "Error", JOptionPane.ERROR_MESSAGE);

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
                case FLOATING_LICENSE_SERVER_NOT_AVAILABLE:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("CAN NOT CONNECT TO FLOATING LICENSE SERVER");

                    JOptionPane.showMessageDialog(null, "Can not connect to floating license server.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case FLOATING_LICENSE_NOT_FOUND:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("FLOATING LICENSE NOT FOUND");

                    JOptionPane.showMessageDialog(null, "Floating license not found.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case FLOATING_LICENSE_NOT_AVAILABLE_ALL_IN_USE:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("FLOATING LICENSE NOT AVAILABLE ALL IN USE");

                    JOptionPane.showMessageDialog(null, "All floating licenses are in use.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                default:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("UNKNOWN ERROR");

                    JOptionPane.showMessageDialog(null, "Unknown error.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
            }

        } catch (UnknownHostException ex) {
            Logger.getLogger(FloatingLicenseValidationJDialog.class.getName()).log(Level.SEVERE, null, ex);

            JOptionPane.showMessageDialog(null, "Unkown host: " + addr, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_getLicensejButtonActionPerformed

    private void closejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closejButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closejButtonActionPerformed

    public static void main(String args[]) {
        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                FloatingLicenseValidationJDialog dialog = new FloatingLicenseValidationJDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton closejButton;
    private javax.swing.JTextField companyjTextField2;
    private javax.swing.JTextField emailjTextField;
    private javax.swing.JTextField expireDatejTextField;
    private javax.swing.JTextField fulllnamejTextField;
    private javax.swing.JButton getLicensejButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
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
    private javax.swing.JTextField portjTextField;
    private javax.swing.JTextField productNamejTextField;
    private javax.swing.JTextField registeredTojTextField;
    private javax.swing.JTextField serverjTextField;
    private javax.swing.JTextField validationStatusjTextField;
    // End of variables declaration//GEN-END:variables
}
