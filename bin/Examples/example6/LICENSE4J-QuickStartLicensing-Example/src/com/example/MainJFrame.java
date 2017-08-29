/**
 * Main class.
 *
 */
package com.example;

import com.license4j.LicenseInformationGUI;
import com.license4j.LicenseInputGUI;
import com.license4j.ProductLicense;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class MainJFrame extends javax.swing.JFrame {

    /**
     * ProductLicense class to keep all license information
     */
    private static ProductLicense productLicense;

    /**
     * Creates new form MainJFrame
     */
    public MainJFrame() {
        initComponents();

        this.setSize(800, 500);
        this.setLocationRelativeTo(null);

        // Read contents of html file and display on editor pane.
        InputStream in;
        BufferedReader br;
        try {
            in = this.getClass().getResourceAsStream("README.html");
            br = new BufferedReader(new InputStreamReader(in));
            String read;
            StringBuilder text = new StringBuilder();
            while ((read = br.readLine()) != null) {
                text.append(read);
            }

            jEditorPane1.setText(text.toString());
            jEditorPane1.setCaretPosition(0);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * Look and feel code
     */
    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LicenseInputGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LicenseInputGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LicenseInputGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LicenseInputGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        licenseMenuItem = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Main Product Window");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jEditorPane1.setEditable(false);
        jEditorPane1.setContentType("text/html"); // NOI18N
        jScrollPane1.setViewportView(jEditorPane1);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jMenu1.setText("File");

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(exitMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenuItem2.setText("Cut");
        jMenu2.add(jMenuItem2);

        jMenuItem4.setText("Copy");
        jMenu2.add(jMenuItem4);

        jMenuItem5.setText("Paste");
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Help");

        jMenuItem1.setText("Help Contents");
        jMenu3.add(jMenuItem1);

        licenseMenuItem.setText("License");
        licenseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                licenseMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(licenseMenuItem);

        jMenuItem3.setText("About");
        jMenu3.add(jMenuItem3);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void licenseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_licenseMenuItemActionPerformed
        /**
         * Display license dialog when user clicked on License menu item.
         */
        new LicenseInformationGUI(this, true, productLicense).display();
    }//GEN-LAST:event_licenseMenuItemActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        /**
         * You can put license loading and validation methods here, so license
         * check will not slow down your software startup.
         */
    }//GEN-LAST:event_formWindowOpened

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set look and feel */
        setLookAndFeel();

        /**
         * --------------------- LICENSING CODE START ------------------
         */
        productLicense = new ProductLicense(
                // the file to save license
                System.getProperty("user.home") + File.separator + "sample-app-license-file.lic",
                // public key
                "30819f300d06092a864886f70d010101050003818d003081893032301006"
                + "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0"
                + "0044f19c44ee47bc7a7f61af76c1b060a750b9bee5c9907452c8f34d42fG"
                + "02818100a1f9ed4070844ab588914b06f68f49f36ba581ee589901bce5a8"
                + "66c3753f32b320313b1f7be69753d97993848feb81351bc9f9df23fb0c93"
                + "a06a56c964a2d37d216e8fb557fc1d8b5dd9c740052f66afde48b0d515fe"
                + "ce608c04ced6e11475f003RSA4102413SHA512withRSA9103c41df57a5d4"
                + "8969e961326cf1e7233f4cd1c0d7121204a6da690e21a17ab0203010001",
                // a trial license key
                "7IDZ9-2VY4C-SIDNK-JPHVE-KL7VA",
                // since trial license key is given, this must be null
                null,
                // this is internal string for license keys
                "some-internal-string",
                // this the product id
                "examples",
                // activation server is null, so we use Online.License4J
                null);

        /**
         * Initialize LicenseInformationGUI, and run check method. GUI will not
         * be displayed if there is a valid license.
         */
        LicenseInformationGUI licenseInformationGUI = new LicenseInformationGUI(null, true, productLicense);
        boolean ok = licenseInformationGUI.check();
        /**
         * --------------------- LICENSING CODE END ------------------
         */

        /**
         * If gui.check() method returns true, it means there is a valid
         * license, so start your main frame.
         */
        if (ok) {
            /* Create and display the form */
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new MainJFrame().setVisible(true);
                }
            });
        } else {
            JOptionPane.showMessageDialog(null, "Either license is invalid or activation period expired.\n\n"
                    + "Software will exit now.", "License Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem licenseMenuItem;
    // End of variables declaration//GEN-END:variables
}
