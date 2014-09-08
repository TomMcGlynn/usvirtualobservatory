/**
 * Copyright (C) Smithsonian Astrophysical Observatory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LoadSegmentDialog.java
 *
 * Created on May 14, 2011, 5:23:17 AM
 */

package cfa.vo.sed.gui;

import cfa.vo.sed.filters.FileFormatManager;
import cfa.vo.sed.filters.FilterException;
import cfa.vo.sed.filters.IFileFormat;
import cfa.vo.sed.filters.NativeFileFormat;
import cfa.vo.sed.importer.NEDImporter;
import cfa.vo.sed.importer.SegmentImporterException;
import cfa.vo.sedlib.Sed;
import cfa.vo.sedlib.Segment;
import cfa.vo.utils.NarrowOptionPane;
import cfa.vo.utils.SpaceTrimmer;
import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 *
 * @author olaurino
 */
public final class LoadSegmentDialog extends javax.swing.JDialog {

    private String nedEndpoint = NEDImporter.NED_DATA_DEFAULT_ENDPOINT;
    public static final String PROP_NEDENDPOINT = "nedEndpoint";

    /**
     * Get the value of nedEndpoint
     *
     * @return the value of nedEndpoint
     */
    public String getNedEndpoint() {
        return nedEndpoint;
    }

    /**
     * Set the value of nedEndpoint
     *
     * @param nedEndpoint new value of nedEndpoint
     */
    public void setNedEndpoint(String nedEndpoint) {
        String oldNedEndpoint = this.nedEndpoint;
        this.nedEndpoint = nedEndpoint;
        firePropertyChange(PROP_NEDENDPOINT, oldNedEndpoint, nedEndpoint);
    }


    private String urlText = "";
    public static final String PROP_URLTEXT = "urlText";

    /**
     * Get the value of urlText
     *
     * @return the value of urlText
     */
    public String getUrlText() {
        return urlText;
    }

    /**
     * Set the value of urlText
     *
     * @param urlText new value of urlText
     */
    public void setUrlText(String urlText) {
        urlText = SpaceTrimmer.sideTrim(urlText);
        String oldUrlText = this.urlText;
        this.urlText = urlText;
        firePropertyChange(PROP_URLTEXT, oldUrlText, urlText);
        setUrlS(urlText);
    }


    private boolean urlSelected = false;
    public static final String PROP_URLSELECTED = "urlSelected";

    /**
     * Get the value of urlSelected
     *
     * @return the value of urlSelected
     */
    public boolean isUrlSelected() {
        return urlSelected;
    }

    /**
     * Set the value of urlSelected
     *
     * @param urlSelected new value of urlSelected
     */
    public void setUrlSelected(boolean urlSelected) {
        boolean oldUrlSelected = this.urlSelected;
        this.urlSelected = urlSelected;
        firePropertyChange(PROP_URLSELECTED, oldUrlSelected, urlSelected);
        setUrlS(urlText);
    }


    private boolean localSelected = true;
    public static final String PROP_LOCALSELECTED = "localSelected";

    /**
     * Get the value of localSelected
     *
     * @return the value of localSelected
     */
    public boolean isLocalSelected() {
        return localSelected;
    }

    /**
     * Set the value of localSelected
     *
     * @param localSelected new value of localSelected
     */
    public void setLocalSelected(boolean localSelected) {
        boolean oldLocalSelected = this.localSelected;
        this.localSelected = localSelected;
        firePropertyChange(PROP_LOCALSELECTED, oldLocalSelected, localSelected);
        if(localSelected)
            setUrlS("file://"+new File(diskLocation).getAbsolutePath());
    }


    private boolean nedVisible = false;
    public static final String PROP_NEDVISIBLE = "nedVisible";

    /**
     * Get the value of nedVisible
     *
     * @return the value of nedVisible
     */
    public boolean isNedVisible() {
        return nedVisible;
    }

    /**
     * Set the value of nedVisible
     *
     * @param nedVisible new value of nedVisible
     */
    public void setNedVisible(boolean nedVisible) {
        boolean oldNedVisible = this.nedVisible;
        this.nedVisible = nedVisible;
        firePropertyChange(PROP_NEDVISIBLE, oldNedVisible, nedVisible);
        setIsLoadable(!nedVisible);
    }

    private boolean chosen = false;

    /**
     * Get the value of chosen
     *
     * @return the value of chosen
     */
    public boolean isChosen() {
        return chosen;
    }

    private List<Segment> segList = new ArrayList();

    public boolean hasSegments() {
        return !segList.isEmpty();
    }

    public List<Segment> getSegments() {
        return segList;
    }

    private String targetName = "";
    public static final String PROP_TARGETNAME = "targetName";

    /**
     * Get the value of targetName
     *
     * @return the value of targetName
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * Set the value of targetName
     *
     * @param targetName new value of targetName
     */
    public void setTargetName(String targetName) {
        String oldTargetName = this.targetName;
        this.targetName = targetName;
        firePropertyChange(PROP_TARGETNAME, oldTargetName, targetName);
    }


    private boolean isLoadable = false;
    public static final String PROP_ISLOADABLE = "isLoadable";

    /**
     * Get the value of isLoadable
     *
     * @return the value of isLoadable
     */
    public boolean isIsLoadable() {
        return isLoadable;
    }

    /**
     * Set the value of isLoadable
     *
     * @param isLoadable new value of isLoadable
     */
    public void setIsLoadable(boolean isLoadable) {
        boolean oldIsLoadable = this.isLoadable;
        this.isLoadable = isLoadable;
        firePropertyChange(PROP_ISLOADABLE, oldIsLoadable, isLoadable);
    }


    public static final String PROP_FORMATS = "formats";

    private String diskLocation = "";
    public static final String PROP_DISKLOCATION = "diskLocation";

    /**
     * Get the value of location
     *
     * @return the value of location
     */
    public String getDiskLocation() {
        return diskLocation;
    }

    /**
     * Set the value of location
     *
     * @param location new value of location
     */
    public void setDiskLocation(String location) {
        location = SpaceTrimmer.sideTrim(location);
        String oldLocation = this.diskLocation;
        this.diskLocation = location;
        firePropertyChange(PROP_DISKLOCATION, oldLocation, location);
        setUrlS("file://"+new File(location).getAbsolutePath());
    }


    public static final String PROP_URLS = "urlS";

    private String urlS;

    /**
     * Get the value of urlS
     *
     * @return the value of urlS
     */
    public String getUrlS() {
        return urlS;
    }

    /**
     * Set the value of urlS
     *
     * @param urlS new value of urlS
     */
    public void setUrlS(String urlS) {
        String oldUrlS = this.urlS;
        this.urlS = urlS;
        firePropertyChange(PROP_URLS, oldUrlS, urlS);
        setIsLoadable(isSetURL());
    }


    public URL getURL() throws MalformedURLException {
        return new URL(urlS);
    }

    public boolean isSetURL() {
        if(localSelected)
            return new File(diskLocation).isFile();
        if(urlSelected)
            try {
                return !getURL().getHost().isEmpty() && !getURL().getPath().isEmpty() && getURL().getProtocol().matches("http|ftp");
            } catch (MalformedURLException ex) {
                return false;
            }
        return false;

    }

    private IFileFormat format = NativeFileFormat.VOTABLE;
    public static final String PROP_FORMAT = "format";

    /**
     * Get the value of format
     *
     * @return the value of format
     */
    public IFileFormat getFormat() {
        return format;
    }

    /**
     * Set the value of format
     *
     * @param format new value of format
     */
    public void setFormat(IFileFormat format) {
        IFileFormat oldFormat = this.format;
        this.format = format;
        firePropertyChange(PROP_FORMAT, oldFormat, format);
    }

    /** Creates new form LoadSegmentDialog */
    public LoadSegmentDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocationRelativeTo(parent);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTextField2 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jButton1 = new javax.swing.JButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jButton2 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jTextField3 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jRadioButton3 = new javax.swing.JRadioButton();

        setTitle("Load an input File");
        setResizable(false);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${diskLocation}"), jTextField2, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jRadioButton2, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jTextField2, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jComboBox1.setModel(new DefaultComboBoxModel(FileFormatManager.getInstance().getFormatsArray()));
        jComboBox1.setRenderer(new FormatRenderer());

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${format}"), jComboBox1, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        jLabel1.setText("File Format:");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("URL:");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${urlSelected}"), jRadioButton1, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        jButton1.setText("Browse...");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jRadioButton2, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jButton1, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Location on Disk:");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${localSelected}"), jRadioButton2, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        jButton2.setText("Load");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${isLoadable}"), jButton2, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                load(evt);
            }
        });

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${urlText}"), jTextField1, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jRadioButton1, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jTextField1, org.jdesktop.beansbinding.BeanProperty.create("editable"));
        bindingGroup.addBinding(binding);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "NED Service"));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${targetName}"), jTextField3, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${nedVisible}"), jTextField3, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel2.setText("Target Name:");

        jButton3.setText("Import NED SED");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${nedVisible}"), jButton3, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importNedSed(evt);
            }
        });

        jCheckBox1.setText("Change Endpoint");

        jLabel3.setText("Endpoint: ");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${nedEndpoint}"), jTextField4, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jCheckBox1, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jTextField4, org.jdesktop.beansbinding.BeanProperty.create("editable"));
        bindingGroup.addBinding(binding);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jButton3)
                        .add(94, 94, 94))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                                .add(jLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jTextField4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                                .add(jCheckBox1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 35, Short.MAX_VALUE)
                                .add(jTextField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 198, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(9, 9, 9))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .add(18, 18, 18)
                .add(jButton3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jTextField4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("Get an SED from the NED Service");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${nedVisible}"), jRadioButton3, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jRadioButton3)
                    .add(jButton1)
                    .add(layout.createSequentialGroup()
                        .add(jRadioButton2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextField2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jRadioButton1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 127, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton2))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jRadioButton2)
                    .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jRadioButton1))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton2)
                    .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .add(6, 6, 6)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jRadioButton3)
                .add(17, 17, 17)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 188, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void browse(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browse
        JFileChooser jfc = MainView.getFileChooser();
        File l = new File(diskLocation);
        if(l.isDirectory())
            jfc.setCurrentDirectory(l);
        jfc.setApproveButtonText("Select");
        int returnval = jfc.showOpenDialog(MainView.getInstance());
        if(returnval == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            setDiskLocation(f.getAbsolutePath());
        }
    }//GEN-LAST:event_browse

    private void load(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_load
        chosen=true;
        this.setVisible(false);
    }//GEN-LAST:event_load

    private void importNedSed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importNedSed
        if(targetName.isEmpty())
            NarrowOptionPane.showMessageDialog(MainView.getInstance(),
                    "The target name is empty",
                    "Warning",
                    NarrowOptionPane.WARNING_MESSAGE);
        else
            try {
                Sed sed = NEDImporter.getSedFromName(targetName, nedEndpoint);

                if(sed.getNumberOfSegments()==0)
                    NarrowOptionPane.showMessageDialog(MainView.getInstance(),
                        "No Data",
                        "Warning",
                        NarrowOptionPane.WARNING_MESSAGE);

                else {
                    for(int i=0; i<sed.getNumberOfSegments(); i++) {
                        Segment segment = sed.getSegment(i);
                        segList.add(segment);
                    }

                    chosen=true;
                    this.setVisible(false);
                }
        } catch (SegmentImporterException ex) {
            NarrowOptionPane.showMessageDialog(MainView.getInstance(),
                    ex.getMessage(),
                    "Error",
                    NarrowOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_importNedSed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                LoadSegmentDialog dialog = new LoadSegmentDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    public void reset() {
        chosen = false;
        segList = new ArrayList();
    }

    private class FormatRenderer extends BasicComboBoxRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {


            IFileFormat format = (IFileFormat) value;

            if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                    if (-1 < index) {
                        try {
                            list.setToolTipText(format.getFilter().getDescription());
                        } catch (FilterException ex) {
                            Logger.getLogger(SetupFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setFont(list.getFont());
            setText((value == null) ? "" : format.getName());

            return this;

        }
    }

}
