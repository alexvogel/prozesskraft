package license4j.examples;


import com.license4j.DefaultOnlineLicenseKeyCheckTimerHandlerImpl;
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
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;

public class OnlineBasicLicenseKeyValidationJDialog extends javax.swing.JDialog {

    private String publickey
            = "30819f300d06092a864886f70d010101050003818d003081893032301006\n"
            + "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n"
            + "0044f19c44ee47bc7a7f61af76c1b060a750b9bee5c9907452c8f34d42fG\n"
            + "02818100a1f9ed4070844ab588914b06f68f49f36ba581ee589901bce5a8\n"
            + "66c3753f32b320313b1f7be69753d97993848feb81351bc9f9df23fb0c93\n"
            + "a06a56c964a2d37d216e8fb557fc1d8b5dd9c740052f66afde48b0d515fe\n"
            + "ce608c04ced6e11475f003RSA4102413SHA512withRSA9103c41df57a5d4\n"
            + "8969e961326cf1e7233f4cd1c0d7121204a6da690e21a17ab0203010001";

    public OnlineBasicLicenseKeyValidationJDialog(java.awt.Frame parent, boolean modal) {
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
        jLabel11 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        usageTimejTextField = new javax.swing.JTextField();
        usageCountTextField = new javax.swing.JTextField();
        closejButton = new javax.swing.JButton();
        getLicensejButton = new javax.swing.JButton();

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

        registeredTojTextField.setEditable(false);

        jLabel2.setText("Registered To:");

        companyjTextField2.setEditable(false);

        jLabel5.setText("E-Mail:");

        jLabel8.setText("License Text Obtained From License Server:");

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel7.setText("Product Name:");

        productNamejTextField.setEditable(false);

        jLabel11.setText("Key:");

        jTextField1.setText("SL5IA-ZMGXW-C34PT-KFRA2-5MVB2");

        jLabel9.setText("License Usage Count:");

        jLabel10.setText("License Use Time:");

        usageTimejTextField.setEditable(false);

        usageCountTextField.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel11)
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
                                .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(productNamejTextField)
                            .addComponent(jScrollPane1)
                            .addComponent(registeredTojTextField)
                            .addComponent(validationStatusjTextField)
                            .addComponent(expireDatejTextField)
                            .addComponent(emailjTextField)
                            .addComponent(fulllnamejTextField)
                            .addComponent(companyjTextField2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(0, 248, Short.MAX_VALUE))
                            .addComponent(jTextField1)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(usageCountTextField)
                            .addComponent(usageTimejTextField))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(usageCountTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(usageTimejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 439, Short.MAX_VALUE)
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
                    .addComponent(getLicensejButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void getLicensejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getLicensejButtonActionPerformed
        License /* Validate key*/ license = LicenseValidator.validate(
                        jTextField1.getText(), // product key
                        publickey,
                        "examples", // product id
                        "Professional", // product edition
                        "1", // product version
                        null, // current date
                        null, // current product version release date
                        new DefaultOnlineLicenseKeyCheckTimerHandlerImpl("Online Key Validation Error, System.exit will be called.", true));

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

                if (license.getUseCountAllowed() > 0) {
                // If license is an online license key and use limit is set, display it.
                    // use count is increased on each run.
                    usageCountTextField.setText(license.getUseCountCurrent()
                            + " / "
                            + license.getUseCountAllowed());
                } else {
                    usageCountTextField.setText("Not applicable");
                }
                if (license.getUseTimeLimitAllowed() > 0) {
                    // If license is an online license key and use time limit is set, display it.
                    usageTimejTextField.setText(splitToComponentTimes(license.getUseTimeCurrent())
                            + " / "
                            + splitToComponentTimes(license.getUseTimeLimitAllowed()));
                } else {
                    usageTimejTextField.setText("Not applicable");
                }

                JOptionPane.showMessageDialog(null, "License valid.", "Information", JOptionPane.INFORMATION_MESSAGE);

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
            case FLOATING_LICENSE_ALLOWED_USE_COUNT_REACHED:
                validationStatusjTextField.setForeground(Color.RED);
                validationStatusjTextField.setText("FLOATING_LICENSE_ALLOWED_USE_COUNT_REACHED");

                JOptionPane.showMessageDialog(null, "Allowed use count reached.", "Error", JOptionPane.ERROR_MESSAGE);

                break;
            case FLOATING_LICENSE_ALLOWED_USE_TIME_REACHED:
                validationStatusjTextField.setForeground(Color.RED);
                validationStatusjTextField.setText("FLOATING_LICENSE_ALLOWED_USE_TIME_REACHED");

                JOptionPane.showMessageDialog(null, "Allowed use time limit reached.", "Error", JOptionPane.ERROR_MESSAGE);

                break;
            default:
                validationStatusjTextField.setForeground(Color.RED);
                validationStatusjTextField.setText("UNKNOWN ERROR");

                JOptionPane.showMessageDialog(null, "Unknown error.", "Error", JOptionPane.ERROR_MESSAGE);

                break;
        }
    }//GEN-LAST:event_getLicensejButtonActionPerformed

    private String splitToComponentTimes(long longVal) {
        longVal = longVal / 1000;
        int h = (int) longVal / 3600;
        int remainder = (int) longVal - h * 3600;
        int m = remainder / 60;
        remainder = remainder - m * 60;
        int s = remainder;

        String hours;
        String minutes;
        String seconds;
        if (h < 10) {
            hours = "0" + String.valueOf(h);
        } else {
            hours = String.valueOf(h);
        }
        if (m < 10) {
            minutes = "0" + String.valueOf(m);
        } else {
            minutes = String.valueOf(m);
        }
        if (s < 10) {
            seconds = "0" + String.valueOf(s);
        } else {
            seconds = String.valueOf(s);
        }

        return hours + ":" + minutes + ":" + seconds;
    }

    private void closejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closejButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closejButtonActionPerformed

    public static void main(String args[]) {
        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                OnlineBasicLicenseKeyValidationJDialog dialog = new OnlineBasicLicenseKeyValidationJDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JLabel jLabel11;
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
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField productNamejTextField;
    private javax.swing.JTextField registeredTojTextField;
    private javax.swing.JTextField usageCountTextField;
    private javax.swing.JTextField usageTimejTextField;
    private javax.swing.JTextField validationStatusjTextField;
    // End of variables declaration//GEN-END:variables
}
