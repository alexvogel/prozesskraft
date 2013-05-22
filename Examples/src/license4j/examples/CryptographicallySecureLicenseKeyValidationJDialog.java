package license4j.examples;

import static com.license4j.ActivationStatus.ACTIVATION_COMPLETED;
import static com.license4j.ActivationStatus.ACTIVATION_SERVER_CONNECTION_ERROR;
import static com.license4j.ActivationStatus.ALREADY_ACTIVATED_ON_ANOTHER_COMPUTER;
import static com.license4j.ActivationStatus.LICENSE_NOT_FOUND_ON_ACTIVATION_SERVER;
import static com.license4j.ActivationStatus.MULTIPLE_ACTIVATION_LIMIT_REACHED;
import com.license4j.License;
import com.license4j.LicenseValidator;
import java.awt.Color;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class CryptographicallySecureLicenseKeyValidationJDialog extends javax.swing.JDialog {

    private String publickey =
            "30819f300d06092a864886f70d010101050003818d003081893032301006"
            + "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0"
            + "0044f19c44ee47bc7a7f61af76c1b060a750b9bee5c9907452c8f34d42fG"
            + "02818100a1f9ed4070844ab588914b06f68f49f36ba581ee589901bce5a8"
            + "66c3753f32b320313b1f7be69753d97993848feb81351bc9f9df23fb0c93"
            + "a06a56c964a2d37d216e8fb557fc1d8b5dd9c740052f66afde48b0d515fe"
            + "ce608c04ced6e11475f003RSA4102413SHA512withRSA9103c41df57a5d4"
            + "8969e961326cf1e7233f4cd1c0d7121204a6da690e21a17ab0203010001";
    
    private License license;

    public CryptographicallySecureLicenseKeyValidationJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocationRelativeTo(null);

        /* Hide activate button at first */
        activatejButton.setVisible(false);

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        activatejButton = new javax.swing.JButton();
        changeProductKeyjButton = new javax.swing.JButton();
        closejButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        activationStatusjTextField = new javax.swing.JTextField();
        validationStatusjTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cryptographically Secure License Key Validation");

        activatejButton.setText("Activate");
        activatejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activatejButtonActionPerformed(evt);
            }
        });

        changeProductKeyjButton.setText("Change Product Key");
        changeProductKeyjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeProductKeyjButtonActionPerformed(evt);
            }
        });

        closejButton.setText("Close");
        closejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closejButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("License Information"));

        activationStatusjTextField.setEditable(false);

        validationStatusjTextField.setEditable(false);

        jLabel6.setText("Validation Status:");

        jLabel7.setText("Activation Status:");

        jLabel1.setText("Activation code or license text:");

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(activationStatusjTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
                    .addComponent(validationStatusjTextField)
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(validationStatusjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(activationStatusjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(activatejButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 332, Short.MAX_VALUE)
                        .addComponent(changeProductKeyjButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(closejButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closejButton)
                    .addComponent(changeProductKeyjButton)
                    .addComponent(activatejButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void activatejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activatejButtonActionPerformed
        License activatedLicense = LicenseValidator.autoActivate(license); // Validated license

        switch (activatedLicense.getActivationStatus()) {
            case ACTIVATION_COMPLETED:
                activationStatusjTextField.setForeground(Color.BLUE);
                activationStatusjTextField.setText("ACTIVATION COMPLETED");

                jTextArea1.setText(activatedLicense.getLicenseString());
                jTextArea1.setCaretPosition(0);

                JOptionPane.showMessageDialog(null, "Activation completed.", "Information", JOptionPane.INFORMATION_MESSAGE);

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

    private void changeProductKeyjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeProductKeyjButtonActionPerformed
        ChangeCryptographicProductKeyJDialog changeCryptographicProductKeyJDialog = new ChangeCryptographicProductKeyJDialog(null, true);
        String theKey = changeCryptographicProductKeyJDialog.showDialog();

        if (theKey != null) {
            // clear activation code field if set before
            jTextArea1.setText("");

            license = LicenseValidator.validate(
                    theKey,
                    publickey,
                    null,
                    null,
                    null,
                    0);

            switch (license.getValidationStatus()) {
                case LICENSE_VALID:
                    validationStatusjTextField.setForeground(Color.BLUE);
                    validationStatusjTextField.setText("VALID");

                    /* Check for activation status */
                    if (license.isActivationRequired()) {
                        /* Show activate button */
                        activatejButton.setVisible(true);

                        if (license.getLicenseActivationDaysRemaining(new Date()) >= 0) {
                            activationStatusjTextField.setForeground(Color.BLUE);
                            activationStatusjTextField.setText("ACTIVATION REQUIRED " + license.getLicenseActivationDaysRemaining(new Date()) + " DAYS LEFT");
                        } else {
                            activationStatusjTextField.setForeground(Color.RED);
                            activationStatusjTextField.setText("ACTIVATION REQUIRED");

                        }
                    } else {
                        activationStatusjTextField.setForeground(Color.BLUE);
                        activationStatusjTextField.setText("ACTIVATION NOT REQUIRED");
                    }

                    JOptionPane.showMessageDialog(null, "License valid.", "Information", JOptionPane.INFORMATION_MESSAGE);

                    break;
                case LICENSE_INVALID:
                    validationStatusjTextField.setForeground(Color.RED);
                    validationStatusjTextField.setText("INVALID");

                    JOptionPane.showMessageDialog(null, "License invalid.", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
            }
        }
    }//GEN-LAST:event_changeProductKeyjButtonActionPerformed

    private void closejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closejButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closejButtonActionPerformed

    public static void main(String args[]) {
        try {
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //</editor-fold>
        } catch (Exception ex) {
            Logger.getLogger(LicenseTextValidationJDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                CryptographicallySecureLicenseKeyValidationJDialog dialog = new CryptographicallySecureLicenseKeyValidationJDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton changeProductKeyjButton;
    private javax.swing.JButton closejButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField validationStatusjTextField;
    // End of variables declaration//GEN-END:variables
}
