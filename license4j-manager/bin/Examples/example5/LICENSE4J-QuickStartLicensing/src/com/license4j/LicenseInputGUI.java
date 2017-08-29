/**
 * Main license input dialog, customer use this to install a license or start
 * trial period.
 */
package com.license4j;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;

public class LicenseInputGUI extends javax.swing.JDialog {

    // License object
    private License license;
    // ProductLicense class
    private ProductLicense productLicense;

    private final JFileChooser jFileChooser;

    /**
     * Creates new form MainGUI
     */
    public LicenseInputGUI(java.awt.Frame parent, boolean modal, ProductLicense productLicense) {
        super(parent, modal);

        setLookFeel();

        initComponents();

        this.productLicense = productLicense;

        this.setLocationRelativeTo(null);

        // Initialize file chooser.
        jFileChooser = new JFileChooser();
        jFileChooser.setMultiSelectionEnabled(false);
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Set document filter to uppercase all.
        ((AbstractDocument) licenseKeyjTextField.getDocument()).setDocumentFilter(new UpperCaseDocumentFilter());
    }

    public License display() {
        // Display dialog and return license object when closed.
        super.setVisible(true);
        return license;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        mainjPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        haveLicenseKeyjRadioButton = new javax.swing.JRadioButton();
        haveLicenseTextjRadioButton = new javax.swing.JRadioButton();
        licenseKeyjTextField = new javax.swing.JTextField();
        licenseFilejTextField = new javax.swing.JTextField();
        startTrialjRadioButton = new javax.swing.JRadioButton();
        validatejButton = new javax.swing.JButton();
        closejButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("License Validation");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Licensing"));

        buttonGroup1.add(haveLicenseKeyjRadioButton);
        haveLicenseKeyjRadioButton.setText("I have a license key:");
        haveLicenseKeyjRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                haveLicenseKeyjRadioButtonItemStateChanged(evt);
            }
        });

        buttonGroup1.add(haveLicenseTextjRadioButton);
        haveLicenseTextjRadioButton.setText("I have a license text:");
        haveLicenseTextjRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                haveLicenseTextjRadioButtonItemStateChanged(evt);
            }
        });

        licenseKeyjTextField.setEnabled(false);

        licenseFilejTextField.setEditable(false);
        licenseFilejTextField.setEnabled(false);
        licenseFilejTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                licenseFilejTextFieldMouseClicked(evt);
            }
        });

        buttonGroup1.add(startTrialjRadioButton);
        startTrialjRadioButton.setSelected(true);
        startTrialjRadioButton.setText("I want to use trial license");
        startTrialjRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                startTrialjRadioButtonItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startTrialjRadioButton)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(haveLicenseTextjRadioButton)
                            .addComponent(haveLicenseKeyjRadioButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(licenseKeyjTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                            .addComponent(licenseFilejTextField))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(startTrialjRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(haveLicenseKeyjRadioButton)
                    .addComponent(licenseKeyjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(haveLicenseTextjRadioButton)
                    .addComponent(licenseFilejTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {licenseFilejTextField, licenseKeyjTextField});

        validatejButton.setText("Validate");
        validatejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validatejButtonActionPerformed(evt);
            }
        });

        closejButton.setText("Close");
        closejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closejButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainjPanelLayout = new javax.swing.GroupLayout(mainjPanel);
        mainjPanel.setLayout(mainjPanelLayout);
        mainjPanelLayout.setHorizontalGroup(
            mainjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainjPanelLayout.createSequentialGroup()
                .addGroup(mainjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainjPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(validatejButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closejButton))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        mainjPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {closejButton, validatejButton});

        mainjPanelLayout.setVerticalGroup(
            mainjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainjPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(mainjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(validatejButton)
                    .addComponent(closejButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainjPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainjPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void validatejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validatejButtonActionPerformed
        if (startTrialjRadioButton.isSelected()) {
            /**
             * If customer selects trial license option, we check for license
             * type, and either license key or license text is set as trial.
             *
             * Then validate license.
             */
            if (productLicense.getTrialLicenseKey() != null) {
                productLicense.setLicenseKey(productLicense.getTrialLicenseKey());
                license = productLicense.validateLicenseKey();
            } else if (productLicense.getTrialLicenseText() != null) {
                productLicense.setLicenseText(productLicense.getTrialLicenseText());
                license = productLicense.validateLicenseKey();
            }
        } else if (haveLicenseKeyjRadioButton.isSelected()) {
            /**
             * Customer selected license key option, set the key in
             * productLicense class then validate.
             */
            productLicense.setLicenseKey(licenseKeyjTextField.getText().trim());
            license = productLicense.validateLicenseKey();
        } else if (haveLicenseTextjRadioButton.isSelected()) {
            /**
             * Customer selected license file option, display file chooser to
             * customer to select license file.
             */
            StringBuilder buf = new StringBuilder();
            BufferedReader br = null;
            try {
                String line;
                br = new BufferedReader(new FileReader(licenseFilejTextField.getText()));

                while ((line = br.readLine()) != null) {
                    buf.append(line);
                    buf.append("\n");
                }

                /**
                 * then set license text here, and validate.
                 */
                productLicense.setLicenseText(buf.toString());
                license = productLicense.validateLicenseText();
            } catch (IOException ex) {
                Logger.getLogger(LicenseInputGUI.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(LicenseInputGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        dispose();
    }//GEN-LAST:event_validatejButtonActionPerformed

    private void startTrialjRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_startTrialjRadioButtonItemStateChanged
        /**
         * Enable/Disable fields etc depending on selection.
         */
        if (startTrialjRadioButton.isSelected()) {
            licenseKeyjTextField.setEnabled(false);
            licenseFilejTextField.setEnabled(false);

            licenseKeyjTextField.setText("");
            licenseFilejTextField.setText("");
        }
    }//GEN-LAST:event_startTrialjRadioButtonItemStateChanged

    private void haveLicenseKeyjRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_haveLicenseKeyjRadioButtonItemStateChanged
        /**
         * Enable/Disable fields etc depending on selection.
         */
        if (haveLicenseKeyjRadioButton.isSelected()) {
            licenseKeyjTextField.setEnabled(true);
            licenseFilejTextField.setEnabled(false);

            licenseKeyjTextField.setText("");
            licenseFilejTextField.setText("");
        }
    }//GEN-LAST:event_haveLicenseKeyjRadioButtonItemStateChanged

    private void haveLicenseTextjRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_haveLicenseTextjRadioButtonItemStateChanged
        /**
         * Enable/Disable fields etc depending on selection.
         */
        if (haveLicenseTextjRadioButton.isSelected()) {
            licenseKeyjTextField.setEnabled(false);
            licenseFilejTextField.setEnabled(true);

            licenseKeyjTextField.setText("");
            licenseFilejTextField.setForeground(Color.DARK_GRAY);
            licenseFilejTextField.setText("double click here to select license file");
        }
    }//GEN-LAST:event_haveLicenseTextjRadioButtonItemStateChanged

    private void licenseFilejTextFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_licenseFilejTextFieldMouseClicked
        /**
         * File chooser is opened when double clicked.
         */
        if (haveLicenseTextjRadioButton.isSelected() && evt.getClickCount() == 2) {
            int s = jFileChooser.showOpenDialog(this);

            if (s == JFileChooser.APPROVE_OPTION) {
                licenseFilejTextField.setForeground(licenseKeyjTextField.getForeground());

                licenseFilejTextField.setText(jFileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }//GEN-LAST:event_licenseFilejTextFieldMouseClicked

    private void closejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closejButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closejButtonActionPerformed

    private void setLookFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LicenseInputGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(LicenseInputGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(LicenseInputGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(LicenseInputGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton closejButton;
    private javax.swing.JRadioButton haveLicenseKeyjRadioButton;
    private javax.swing.JRadioButton haveLicenseTextjRadioButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField licenseFilejTextField;
    private javax.swing.JTextField licenseKeyjTextField;
    private javax.swing.JPanel mainjPanel;
    private javax.swing.JRadioButton startTrialjRadioButton;
    private javax.swing.JButton validatejButton;
    // End of variables declaration//GEN-END:variables
}
