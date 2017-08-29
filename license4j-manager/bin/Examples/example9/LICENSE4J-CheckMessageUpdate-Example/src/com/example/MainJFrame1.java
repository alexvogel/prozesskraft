package com.example;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import com.license4j.License;
import com.license4j.LicenseValidator;
import com.license4j.ValidationStatus;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Simple JFrame to demonstrate product update check.
 *
 * License validation then product update check is performed in formWindowOpened
 * method after main window displayed.
 *
 */
public class MainJFrame1 extends javax.swing.JFrame {

    /**
     * Creates new form MainJFrame
     */
    public MainJFrame1() {
        initComponents();

        this.setLocationRelativeTo(null);

        jEditorPane1.setText("<span style=\"padding:25px;\">"
                + "<H2>Example Application to Demonstrate Product Update Notification</H2>"
                + "<P>This example has a sample license in source code, and will check for update then display an update notification window after window opened.</P>"
                + "</span>");
    }

    /*
     * Look and feel code
     */
    private static void setLookAndFeel() {
        if (System.getProperty("os.name").toLowerCase(Locale.US).startsWith("windows")) {
            try {
                UIManager.setLookAndFeel(new WindowsLookAndFeel());
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(MainJFrame1.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(MainJFrame1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jEditorPane1.setContentType("text/html"); // NOI18N
        jScrollPane1.setViewportView(jEditorPane1);

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 692, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        /**
         *
         * Here is our license validation code, it runs after window opened.
         *
         */
        String key = "GSLRZ-C5DQE-BVRBW-IXNCR-UT72V";

        String publickey = "30819f300d06092a864886f70d010101050003818d003081893032301006072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e00044f19c44ee47bc7a7f61af76c1b060a750b9bee5c9907452c8f34d42fG02818100a1f9ed4070844ab588914b06f68f49f36ba581ee589901bce5a866c3753f32b320313b1f7be69753d97993848feb81351bc9f9df23fb0c93a06a56c964a2d37d216e8fb557fc1d8b5dd9c740052f66afde48b0d515fece608c04ced6e11475f003RSA4102413SHA512withRSA9103c41df57a5d48969e961326cf1e7233f4cd1c0d7121204a6da690e21a17ab0203010001";

        String internalString = "example";
        String nameforValidation = null;
        String companyforValidation = null;
        int hardwareIDMethod = 0;

       final License license = LicenseValidator.validate(
                key,
                publickey,
                internalString,
                nameforValidation,
                companyforValidation,
                hardwareIDMethod);

        /**
         * If license is valid, we will check for update to this product.
         */
        if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
            /**
             * Product edition is null.
             *
             * As an example we define current software version as 1.0. If there
             * is an update defined on server bigger than 1.0, it will be
             * returned, otherwise null returns.
             *
             * It runs in thread to not block application main window.
             */
            Thread t = new Thread() {
                @Override
                public void run() {
                    String updatedVersionNumber = license.checkForUpdate(null, "1.0");

                    if (updatedVersionNumber != null) {
                        /**
                         * If an update message is defined, you can get it from
                         * server with
                         */
                        String updateMessage = license.checkForUpdateMessage(null, updatedVersionNumber);

                        JOptionPane.showMessageDialog(null, updateMessage, "Update Found", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            };
            t.start();
        }

    }//GEN-LAST:event_formWindowOpened

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set look and feel */
        setLookAndFeel();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainJFrame1().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
