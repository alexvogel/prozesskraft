package com.example;

import com.license4j.License;
import com.license4j.LicenseValidator;
import com.license4j.ValidationStatus;
import com.license4j.util.Crypto;
import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * *
 *
 *
 * This is a sample application main class file.
 *
 *
 * A single ant task (name: build_obfuscate_exe) is defined in this Netbeans
 * project build.xml file. When ant task run, it clean/build this sample
 * application then obfuscate both application and License4J Runtime Library and
 * create a single jar file named as "LICENSE4J-Obfuscation-Example.jar"; and
 * also creates an exe file with launch4j. build.xml file includes descriptions
 * in comment blocks. When you decompile final jar file and open the
 * MainJFrame.class, you will see that license validation routines in source is
 * completely obfuscated.
 *
 *
 * build.xml file, Proguard configuration file (in proguard folder), and
 * launch4j configuration file (in launch4j folder) can be used as a reference
 * for your software.
 *
 *
 */
public class MainJFrame extends javax.swing.JFrame {

    public MainJFrame() {
        initComponents();

        this.setLocationRelativeTo(null);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("License4J Example3");

        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        jLabel1.setText("Example Application 3");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(132, 132, 132)
                .addComponent(jLabel1)
                .addContainerGap(318, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addComponent(jLabel1)
                .addContainerGap(150, Short.MAX_VALUE))
        );

        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
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
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                /**
                 * License validation start
                 *
                 * The sample license below is valid, so you can run this
                 * project and see the output.
                 */

                /**
                 * New validateFloatingLicenseText with less arguments and new
                 * handler.
                 */
                String licenseString = "f22bb85d7950a8e98ed671c752aa02ab7a566705a0cf11adfdfb33d47d4ff9f72d8e279577e1b1a4f01f39b46d5744ba76e372436ad4c7375d650e1eef9dc35eea38938225ed269f873d2b138aa2e3aab0fd256fcc70496f8222bfb92d6a1820f52ba734dfaa4f3d01029c2d0e87c5df535b2bd301309699eb0c2b8c734cfb156dae59347662195706dc8314850e0f43e574dfc21eb2c4567dea04e04598249109a80bc7540203acde3907e22b9a301302537009aea490473c80b9abaf5b4127a0e4f6345b3ece66eb42658d9880080790f33c41ce3160fafa1e5a58600c0d15c24153bb014bcac6a74550137fa52b66a9d4a630709c7eb3fbdfd2cc6a2ddd5b471ebfd26436887a9e8911eab9e85532c1d16fe6c2bd453b0e0779962702ec6408e3270a03d0568d6e7665ed8e2331268464868395ec122823d0909d4b391aee8a7c26259eb4c3892444b143e4d6c363a01b4fe8332b5336dfb033f025bd4c1c00b71f2e492c8f59ea5bd3189d5c0a1f77d3ebc9fd07f8f92e7920a018298991137b291c6f91b0a60c961beda4bcaf49b45fb3116b80f0b56e6834b2729e9ef8bd45d47ce1c0bc08aaa436369ac89ba57c6f843286308861e436042e2c2396f21b5a07974426c7fa5f8eb204f15de1016a8e188e8eb065fe42a321e40c94e7313da830167ce2c75948ed5aad9b14821cedb840d7b157566bad443c0185b3d011bcd60681837d598a6f4b412ae48bbd4f971a6c11d9037309a9cb25d3857ef03eb1460e520739deeb1873a52f8c4ca6bad79c7eb65d3bd48ded27451b9aaadd65d46caec45191ec62da8f5b358346055a";
                String publickey = "30819f300d06092a864886f70d010101050003818d003081893032301006072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e00044ac0562877795266a1f0b38965af339c6196923eedb1e2f4f99377f5G02818100a9a58b21279c8b4048c78f89d27523140b3b3ebe7aa256fa89e9065573b2e095cbd135d25a1affd1e1d2d298cbd992a498fbef439a1d85393dbb5002d3b844d227aa7cac31970a5ed9a016a8267112f49b5f3dfb12823a0b279ea57fa5c5ab7603RSA4102413SHA512withRSA7a926e8d56c76925be4a8d1723f98700049ecc61520d25f7b24fa0d8dec914f10203010001";
                String productID = "example-id";
                String productEdition = "professional";

                License license = LicenseValidator.validate(
                        licenseString,
                        publickey,
                        productID,
                        productEdition,
                        null,
                        null,
                        null);

                /**
                 * Construct message to display in option pane.
                 */
                try {
                    StringBuilder buf = new StringBuilder();

                    /**
                     * Strings can be encrypted with com.util.Crypto.encrypt
                     * method, then decrypted with com.util.Crypto.decrypt
                     * method.
                     */
                    buf.append(Crypto.decrypt("14d28f593d41c182ad15f4539fd14703af6e7856433e5f57db658d7a603201fb"));
                    buf.append(license.getValidationStatus());
                    buf.append("\n");

                    if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
                        buf.append(Crypto.decrypt("08cd0dadfe04aeed991607361bf28f6800209fab287b3203847593b993954254"));
                        buf.append(license.getLicenseText().getUserFullName());
                        buf.append("\n");
                        buf.append(Crypto.decrypt("36ab17b1d874810dc84a73719b472e917dc6a87ac10deed87fad39c90102fede"));
                        buf.append(license.getLicenseText().getUserCompany());
                        buf.append("\n");
                        buf.append(Crypto.decrypt("336d254f28582b791c14386a8902e318a4903f1a6317289687ef82380d3db103"));
                        buf.append(license.getLicenseText().getCustomSignedFeature("custom-feature1"));
                        buf.append("\n");
                    }

                    /**
                     * Change option pane font.
                     */
                    UIManager.put("OptionPane.messageFont", new Font("Monospaced", Font.BOLD, 16));

                    /**
                     * Display license information in an option pane before
                     * starting application.
                     */
                    JOptionPane.showMessageDialog(null, buf.toString(), "License Information", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                /**
                 * License validation end
                 */
                new MainJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
