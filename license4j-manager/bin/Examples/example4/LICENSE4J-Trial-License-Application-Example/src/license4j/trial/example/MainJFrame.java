/**
 * Main class.
 *
 */
package license4j.trial.example;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import com.license4j.License;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import license4j.complete.application.example.ProgressDialog;

public class MainJFrame extends javax.swing.JFrame {

    // MyProductLicense class
    private static MyProductLicense myProductLicense;
    // License object
    private static License license;

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

    /**
     * This method performs license validation tasks.
     */
    private void makeLicenseValidation() {
        final ProgressDialog progress = new ProgressDialog(null, true);

        /**
         * License validation method runs in SwingWorker, you can use threads in
         * a better way.
         */
        SwingWorker worker = new SwingWorker() {
            License license = null;

            @Override
            protected void done() {
                progress.setVisible(false);
            }

            @Override
            protected License doInBackground() {
                /**
                 * Initialize MyProductLicense. It loads license file in its
                 * constructor
                 */
                myProductLicense = new MyProductLicense();

                /**
                 * First, try to validate activateLicenseText
                 */
                license = myProductLicense.validateActivatedLicenseText();

                /**
                 * If license is null, it means there is no activateLicenseText
                 * in file then try to validate license key.
                 */
                if (license == null) {
                    license = myProductLicense.validateLicenseKey();
                }
                /**
                 * If there is no license key in file, license object will be
                 * null.
                 */

                return license;
            }
        };

        worker.execute();
        progress.setVisible(true);

        try {
            license = (License) worker.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(MyProductLicense.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(MyProductLicense.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (license == null) {
            /**
             * Either there is no license file or there is no license key or
             * activated license text in file, so display license dialog.
             */

            new MyProductLicenseJDialog(this, true, myProductLicense, license).setVisible(true);
            /**
             * License may be modified in MyProductLicenseJDialog after above
             * statement, so it is better to recall makeLicenseValidation method
             * in this main frame or force user to close and re-open
             * application.
             */
        } else if (license.getLicenseText() != null) {
            /**
             * If license is a License Text, then we know that there is an
             * activated license text.
             */
            switch (license.getValidationStatus()) {
                case LICENSE_VALID:
                    /**
                     * Continue running software, since license is activated and
                     * valid.
                     *
                     * There is no need to check for activation status here,
                     * because there can be only activated license text as we
                     * defined in generating license.
                     *
                     */

                    if (license.getLicenseText().getCustomSignedFeature("trial") != null
                            && license.getLicenseText().getCustomSignedFeature("trial").compareTo("yes") == 0) {
                        /**
                         * We set a custom signed feature named as "trial" and
                         * set its value to "yes" while generating license. If
                         * we find it, we know that this is a trial license;
                         * and we may display a dialog like below.
                         */
                        JOptionPane.showMessageDialog(this, license.getLicenseText().getLicenseExpireDaysRemaining(null) + " days left for trial period.",
                                    "Activation Period",
                                    JOptionPane.INFORMATION_MESSAGE);
                    }

                    break;
                default:
                    /**
                     * If another ValidationStatus value is obtained, then there
                     * is a problem with license, so display license dialog to
                     * give license details to user.
                     */
                    new MyProductLicenseJDialog(this, true, myProductLicense, license).setVisible(true);
                    /**
                     * License may be modified in MyProductLicenseJDialog after
                     * above statement, so it is better to recall
                     * makeLicenseValidation method in this main frame or force
                     * user to close and re-open application.
                     */

                    break;
            }
        } else if (license.getLicenseKey() != null) {
            /**
             * If license is a License Key, then we know that there is no
             * activated license text but only a license key, and it must be
             * activated.
             */
            switch (license.getValidationStatus()) {
                case LICENSE_VALID:
                    /**
                     * There is a license key in file and it is valid. Since we
                     * generate all license keys with activation feature, it
                     * must be activated, but lets check for activation status.
                     */

                    if (license.isActivationRequired()) {
                        /**
                         * This method returns true if only license has
                         * activation feature enabled and it is not activated.
                         *
                         * Then check for activation period left, and if there
                         * is 0 days left for activation, display license dialog
                         * to customer to make activation.
                         *
                         * Also it can be auto activated without requiring user
                         * to click on a button.
                         */
                        System.err.println();
                        if (license.getLicenseActivationDaysRemaining(null) < 1) {
                            new MyProductLicenseJDialog(this, true, myProductLicense, license).setVisible(true);
                            /**
                             * License may be modified in
                             * MyProductLicenseJDialog after above statement, so
                             * it is better to recall makeLicenseValidation
                             * method in this main frame or force user to close
                             * and re-open application.
                             */
                        } else {
                            /**
                             * If activation period is not passed, an
                             * information can be displayed to user or not.
                             */
                            JOptionPane.showMessageDialog(this, license.getLicenseActivationDaysRemaining(new Date()) + " days left for activation.",
                                    "Activation Period",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    break;
            }
        }
    }

    /*
     * Look and feel code
     */
    private static void setLookAndFeel() {
        if (System.getProperty("os.name").toLowerCase(Locale.US).startsWith("windows")) {
            try {
                UIManager.setLookAndFeel(new WindowsLookAndFeel());
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
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
         * Display license dialog
         */
        new MyProductLicenseJDialog(this, true, myProductLicense, license).setVisible(true);
        /**
         * License may be modified in MyProductLicenseJDialog after above
         * statement, so it is better to recall makeLicenseValidation method in
         * this main frame or force user to close and re-open application.
         */
    }//GEN-LAST:event_licenseMenuItemActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        /**
         * We put license loading and validation method here, because we do not
         * want to make software startup slow. But it can be check in main
         * method also.
         */
        makeLicenseValidation();
    }//GEN-LAST:event_formWindowOpened

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set look and feel */
        setLookAndFeel();

        /**
         * We put license loading and validation method in formWindowOpened,
         * because we do not want to make software startup slow. If you like it
         * can be check just before main software window.
         */
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainJFrame().setVisible(true);
            }
        });
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
