/**
 * Copyright (C) 2011 Smithsonian Astrophysical Observatory
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
 * NewSedFrame.java
 *
 * Created on May 6, 2011, 4:14:57 PM
 */

package cfa.vo.sed.gui;

import cfa.vo.sed.filters.IFileFormat;
import cfa.vo.sed.filters.NativeFileFormat;
import cfa.vo.sed.importer.ISegmentMetadata;
import cfa.vo.sed.importer.SedImporterApp;
import cfa.vo.sedlib.DoubleParam;
import cfa.vo.sedlib.Sed;
import cfa.vo.sedlib.Segment;
import cfa.vo.sedlib.common.ValidationError;
import cfa.vo.sedlib.common.ValidationErrorEnum;
import cfa.vo.sedlib.io.SedFormat;
import cfa.vo.utils.NarrowOptionPane;
import cfa.vo.utils.SkyCoordinates;
import java.awt.Component;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.application.Action;

/**
 *
 * @author olaurino
 */
public final class NewSedFrame extends JInternalFrame {

    private boolean isSegmentSelected = false;
    public static final String PROP_ISSEGMENTSELECTED = "isSegmentSelected";

    /**
     * Get the value of isSegmentSelected
     *
     * @return the value of isSegmentSelected
     */
    public boolean isIsSegmentSelected() {
        return isSegmentSelected;
    }

    /**
     * Set the value of isSegmentSelected
     *
     * @param isSegmentSelected new value of isSegmentSelected
     */
    public void setIsSegmentSelected(boolean isSegmentSelected) {
        boolean oldIsSegmentSelected = this.isSegmentSelected;
        this.isSegmentSelected = isSegmentSelected;
        firePropertyChange(PROP_ISSEGMENTSELECTED, oldIsSegmentSelected, isSegmentSelected);
    }


    private boolean isSedSaveable = false;
    public static final String PROP_ISSEDSAVEABLE = "isSedSaveable";

    /**
     * Get the value of isSedSaveable
     *
     * @return the value of isSedSaveable
     */
    public boolean isIsSedSaveable() {
        return isSedSaveable;
    }

    /**
     * Set the value of isSedSaveable
     *
     * @param isSedSaveable new value of isSedSaveable
     */
    public void setIsSedSaveable(boolean isSedSaveable) {
        boolean oldIsSedSaveable = this.isSedSaveable;
        this.isSedSaveable = isSedSaveable;
        firePropertyChange(PROP_ISSEDSAVEABLE, oldIsSedSaveable, isSedSaveable);
    }


    private String ra;
    public static final String PROP_RA = "ra";

    /**
     * Get the value of ra
     *
     * @return the value of ra
     */
    public String getRa() {
        return ra;
    }

    /**
     * Set the value of ra
     *
     * @param ra new value of ra
     */
    public void setRa(String ra) {
        if(ra!=null) {
            ra = SkyCoordinates.getRaDegString(ra);
        }
            String oldRa = this.ra;
            this.ra = ra;
            firePropertyChange(PROP_RA, oldRa, ra);

    }

    private String dec;
    public static final String PROP_DEC = "dec";

    /**
     * Get the value of dec
     *
     * @return the value of dec
     */
    public String getDec() {
        return dec;
    }

    /**
     * Set the value of dec
     *
     * @param dec new value of dec
     */
    public void setDec(String dec) {
        if(dec!=null) {
            dec = SkyCoordinates.getDecDegString(dec);
        }
        String oldDec = this.dec;
        this.dec = dec;
        firePropertyChange(PROP_DEC, oldDec, dec);
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

    private String sedID;
    public static final String PROP_SEDID = "sedID";

    private SegmentWrapper selectedSegment;
    public static final String PROP_SELECTEDSEGMENT = "selectedSegment";

    /**
     * Get the value of selectedSegment
     *
     * @return the value of selectedSegment
     */
    public SegmentWrapper getSelectedSegment() {
        return selectedSegment;
    }

    /**
     * Set the value of selectedSegment
     *
     * @param selectedSegment new value of selectedSegment
     */
    public void setSelectedSegment(SegmentWrapper selectedSegment) {
        SegmentWrapper oldSelectedSegment = this.selectedSegment;
        this.selectedSegment = selectedSegment;
        firePropertyChange(PROP_SELECTEDSEGMENT, oldSelectedSegment, selectedSegment);
        List<SegmentWrapper> l = new ArrayList();
        l.add(selectedSegment);
        setSelectedSegments(l);
    }


    private List<SegmentWrapper> selectedSegments;
    public static final String PROP_SELECTEDSEGMENTS = "selectedSegments";

    /**
     * Get the value of selectedSegments
     *
     * @return the value of selectedSegments
     */
    public List<SegmentWrapper> getSelectedSegments() {
        return selectedSegments;
    }

    /**
     * Set the value of selectedSegments
     *
     * @param selectedSegments new value of selectedSegments
     */
    public void setSelectedSegments(List<SegmentWrapper> selectedSegments) {
        List<SegmentWrapper> oldSelectedSegments = this.selectedSegments;
        this.selectedSegments = selectedSegments;
        setIsSegmentSelected(!selectedSegments.isEmpty());
        firePropertyChange(PROP_SELECTEDSEGMENTS, oldSelectedSegments, selectedSegments);
    }


    private SedWrapper sedWrapper = new SedWrapper();
    public static final String PROP_SEDWRAPPER = "sedWrapper";

    /**
     * Get the value of segmentList
     *
     * @return the value of segmentList
     */
    public SedWrapper getSedWrapper() {
        return sedWrapper;
    }

    /**
     * Set the value of segmentList
     *
     * @param segmentList new value of segmentList
     */
    public void setSedWrapper(SedWrapper sedWrapper) {
        SedWrapper oldSedWrapper = this.sedWrapper;
        this.sedWrapper = sedWrapper;
        firePropertyChange(PROP_SEDWRAPPER, oldSedWrapper, sedWrapper);
        setSegmentCount(sedWrapper.getSize());
    }

    private Integer segmentCount = 0;
    public static final String PROP_SEGMENTCOUNT = "segmentCount";

    /**
     * Get the value of segmentCount
     *
     * @return the value of segmentCount
     */
    public Integer getSegmentCount() {
        return segmentCount;
    }

    /**
     * Set the value of segmentCount
     *
     * @param segmentCount new value of segmentCount
     */
    public void setSegmentCount(Integer segmentCount) {
        Integer oldSegmentCount = this.segmentCount;
        this.segmentCount = segmentCount;
        firePropertyChange(PROP_SEGMENTCOUNT, oldSegmentCount, segmentCount);
    }


    JDesktopPane desktopPane = MainView.getInstance().getDesktopPane();

    /**
     * Get the value of sedID
     *
     * @return the value of sedID
     */
    public String getSedID() {
        return sedID;
    }

    /**
     * Set the value of sedID
     *
     * @param sedID new value of sedID
     */
    public void setSedID(String sedID) {
        String oldSedID = this.sedID;
        this.sedID = sedID;
        firePropertyChange(PROP_SEDID, oldSedID, sedID);
    }


    /** Creates new form NewSedFrame */
    public NewSedFrame(String name) {
        super(name);
        initComponents();
        setSedID(name);
        jButton6.setVisible(SedImporterApp.isSampEnabled());
        jButton3.setVisible(SedImporterApp.isSampEnabled());
        jCheckBox1.setEnabled(SedImporterApp.isSampEnabled());
        jCheckBox1.setSelected(SedImporterApp.isSampEnabled());

//        addInternalFrameListener(new InternalFrameAdapter() {
//            @Override
//            public void internalFrameClosed(InternalFrameEvent e) {
//                desktopPane.remove(NewSedFrame.this);
//                NewSedFrame.this.dispose();
//            }
//        });

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

        jPanel8 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconifiable(true);
        setResizable(true);
        setAutoscrolls(true);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${sedID}"), this, org.jdesktop.beansbinding.BeanProperty.create("title"));
        bindingGroup.addBinding(binding);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Target Info"));

        jTextField2.setName("targetName"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${targetName}"), jTextField2, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jLabel4.setText("DEC:");

        jTextField4.setName("targetDec"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${dec}"), jTextField4, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jLabel2.setText("Name:");

        jTextField3.setName("targetRa"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${ra}"), jTextField3, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jButton5.setText("Resolve");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolve(evt);
            }
        });

        jLabel3.setText("RA:");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel3)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jTextField2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton5))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jTextField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextField4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(9, 9, 9))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton5)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4)
                    .add(jTextField4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Interoperability"));

        jCheckBox1.setText("Accept tables from SAMP");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${acceptingSAMP}"), jCheckBox1, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(120, Short.MAX_VALUE)
                .add(jCheckBox1))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jCheckBox1)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "SED"));

        jLabel5.setText("Segments #:");

        jTextField5.setEditable(false);
        jTextField5.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${segmentCount}"), jTextField5, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jLabel1.setText("ID:");

        jTextField1.setEditable(false);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${sedID}"), jTextField1, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(NewSedFrame.class, this);
        jButton6.setAction(actionMap.get("broadcastSed")); // NOI18N
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${segmentCount}"), jButton6, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jButton7.setText("Save SED");
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${segmentCount}"), jButton7, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSed(evt);
            }
        });

        jButton8.setText("Reset SED");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reset(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
            .add(jButton7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
            .add(jButton8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .add(jButton6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jButton7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton8)
                .add(14, 14, 14))
        );

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextField5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5)))
            .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 320, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, 0, 154, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel7Layout.createSequentialGroup()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(6, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Segments Operations"));

        jButton4.setText("Save Segment(s)");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${isSegmentSelected}"), jButton4, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSegmentList(evt);
            }
        });

        jButton3.setAction(actionMap.get("broadcastSegments")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${isSegmentSelected}"), jButton3, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jButton1.setText("New Segment(s)");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSegment(evt);
            }
        });

        jButton2.setText("Remove Segment(s)");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${isSegmentSelected}"), jButton2, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedSegments(evt);
            }
        });

        jButton9.setAction(actionMap.get("editSegments")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${isSegmentSelected}"), jButton9, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
            .add(jButton3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
            .add(jButton9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
            .add(jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jButton1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton9)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton4))
        );

        jTable1.setBounds(new java.awt.Rectangle(50, 50, 1000, 700));
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${sedWrapper.segmentList}");
        org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, jTable1);
        org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${target.pos.value}"));
        columnBinding.setColumnName("Coords");
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${curation.publisher.value}"));
        columnBinding.setColumnName("Publisher");
        columnBinding.setColumnClass(String.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${data.length}"));
        columnBinding.setColumnName("# Points");
        columnBinding.setColumnClass(Integer.class);
        columnBinding.setEditable(false);
        bindingGroup.addBinding(jTableBinding);
        jTableBinding.bind();binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${selectedSegment}"), jTable1, org.jdesktop.beansbinding.BeanProperty.create("selectedElement"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${selectedSegments}"), jTable1, org.jdesktop.beansbinding.BeanProperty.create("selectedElements"));
        bindingGroup.addBinding(binding);

        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(55);
        jTable1.getColumnModel().getColumn(0).setCellRenderer(new PosRenderer());
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(215);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(20);

        org.jdesktop.layout.GroupLayout jPanel9Layout = new org.jdesktop.layout.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 436, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel8Layout.createSequentialGroup()
                .add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addSegment(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSegment
        LoadSegmentDialog dia = MainView.getLoadSegmentDialog();
        if(!targetName.isEmpty())
            dia.setTargetName(targetName);
        dia.reset();
        dia.setVisible(true);
        addSegment(dia);
    }//GEN-LAST:event_addSegment

    private void addSegment(LoadSegmentDialog dialog) {
        if(dialog.isChosen()) {

            if(dialog.isSetURL()) {//either there is a url

                boolean compliant = false;

                if(SedImporterApp.READ_COMPLIANT_FILES) 
                    if(dialog.getFormat().equals(NativeFileFormat.FITS)||dialog.getFormat().equals(NativeFileFormat.VOTABLE)) {
                        SedFormat format = dialog.getFormat().equals(NativeFileFormat.FITS) ? SedFormat.FITS : SedFormat.VOT;

                        try {

                            Sed sed = Sed.read(dialog.getURL().openStream(), format);
                            List<Segment> segList = new ArrayList();
                            compliant = true;
                            
                            for (int i=0; i<sed.getNumberOfSegments(); i++) {
                                Segment segment = sed.getSegment(i);
                                List<ValidationError> errList = new ArrayList();
                                segment.validate(errList);

                                if(segment.createTarget().getPos()==null)
                                    if(segment.createChar().createSpatialAxis().createCoverage().getLocation()!=null)
                                        segment.createTarget().createPos().setValue(segment.getChar().getSpatialAxis().getCoverage().getLocation().getValue());
                                    else
                                        segment.createTarget().createPos().setValue(new DoubleParam[]{new DoubleParam(Double.NaN), new DoubleParam(Double.NaN)});
                                    

                                if(errList.isEmpty())
                                    segList.add(segment);
                                else
                                    for(ValidationError err : errList) {
                                        ValidationErrorEnum en = err.getError();
                                        if(!en.equals(ValidationErrorEnum.MISSING_DATA_FLUXAXIS_VALUE) &&
                                           !en.equals(ValidationErrorEnum.MISSING_DATA_SPECTRALAXIS_VALUE))
                                            segList.add(segment);
                                        else
                                            compliant = false;
                                    }
                                    
                                
                            }
                            
                            if(compliant) {
                                addSegments(segList);
                            }
                            
                        } catch (Exception ex) {
                            compliant=false;
                        }
                    }
                

                
                if(!compliant)
                    try {
                        List<ISegmentMetadata> mdList = dialog.getFormat().getFilter(dialog.getURL()).getMetadata();
                        for(int i = mdList.size()-1; i>=0; i--) {
                            ISegmentMetadata md = mdList.get(i);
                            SetupFrame cf = new SetupFrame("New Segment", md, this, dialog.getURL().toString(), dialog.getFormat(), i);
                            desktopPane.add(cf);
                            cf.setVisible(true);
                        }
                    } catch (Exception ex) {
                        String message;
                        if(!(ex.getMessage()==null))
                            message = ex.getMessage();
                        else
                            message = "Unable to import. Please check the file.";
                        NarrowOptionPane.showMessageDialog(MainView.getInstance(),
                                message,
                                "Import Error",
                                NarrowOptionPane.ERROR_MESSAGE);
                        dialog.setVisible(true);
                        this.addSegment(dialog);
                    }

            } else if(dialog.hasSegments()) {//or there is a set of segments from NED
                List<Segment> segList = dialog.getSegments();
                for(Segment segment : segList) {
                        segment.createTarget().createPos().setValue(segment.getChar().getSpatialAxis().getCoverage().getLocation().getValue());
                    
                }
                addSegments(segList);
            }

        }
    }

//    public void addSegment(URL url, IFileFormat format) {
//        try {
//            List<ISegmentMetadata> mdList = format.getFilter(url).getMetadata();
//            for(int i=0; i<mdList.size(); i++) {
//                ISegmentMetadata md = mdList.get(i);
//                SetupFrame cf = new SetupFrame("New Segment", md, this, url.toString(), format, i);
//                desktopPane.add(cf);
//                cf.setVisible(true);
//            }
//        } catch (Exception ex) {
//            NarrowOptionPane.showMessageDialog(MainView.getInstance(),
//                            ex.getMessage(),
//                            "Import Error",
//                            NarrowOptionPane.ERROR_MESSAGE);
//        }
//    }

    public static SetupFrame addSegment(URL url, IFileFormat format, List<NewSedFrame> frameList) throws Exception {
        ISegmentMetadata md = format.getFilter(url).getMetadata().get(0);//This call is used by SAMP handlers and can refer only to a single table
        SetupFrame cf = new SetupFrame("New Segment", md, frameList, url.toString(), format, 0);
        return cf;
    }

    private void resolve(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolve
        try {
            if(!targetName.equals("")) {
                Double[] radec = NameResolver.resolve(targetName);
                setRa(radec[0].toString());
                setDec(radec[1].toString());
            }
        } catch (Exception ex) {
            NarrowOptionPane.showMessageDialog(MainView.getInstance(),
                        "An error occurred while trying to resolve the name "+targetName+".",
                        "Name resolver error",
                        NarrowOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_resolve

    private void saveSed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSed
        SaveSedDialog dia = new SaveSedDialog(MainView.getInstance(), sedWrapper, sedID);
        dia.setVisible(true);
    }//GEN-LAST:event_saveSed

    private void saveSegmentList(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSegmentList
        SaveSedDialog dia = new SaveSedDialog(MainView.getInstance(), new SedWrapper(selectedSegments), sedID);
        dia.setVisible(true);
    }//GEN-LAST:event_saveSegmentList

    private void removeSelectedSegments(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSelectedSegments
        int ans = NarrowOptionPane.showConfirmDialog(MainView.getInstance(),
                "Are you sure you want to remove the selected segments from the SED?",
                "Confirm removal",
                NarrowOptionPane.YES_NO_OPTION);
        if(ans==NarrowOptionPane.YES_OPTION) {
            List newSegmentList = new ArrayList();
            newSegmentList.addAll(sedWrapper.getSegmentList());
            for(SegmentWrapper s : selectedSegments) {
                newSegmentList.remove(s);
                if(s.getSetupFrame() != null)
                    s.getSetupFrame().getSedFrameList().remove(this);
            }
            setSedWrapper(new SedWrapper(newSegmentList));
            setSelectedSegments(new ArrayList());
        }
    }//GEN-LAST:event_removeSelectedSegments

    private void reset(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reset
        int ans = NarrowOptionPane.showConfirmDialog(MainView.getInstance(),
                "Are you sure you want to reset this SED?",
                "Confirm reset",
                NarrowOptionPane.YES_NO_OPTION);
        if(ans==NarrowOptionPane.YES_OPTION) {
            setSedWrapper(new SedWrapper());
            setSelectedSegments(new ArrayList());
            setTargetName("");
            setRa(null);
            setDec(null);
        }
    }//GEN-LAST:event_reset

    public void addSegment(SegmentWrapper segment) {
        List newSegmentList = new ArrayList();
        newSegmentList.addAll(sedWrapper.getSegmentList());
        newSegmentList.add(segment);
        setSedWrapper(new SedWrapper(newSegmentList));
    }
    
    public void addSegments(List<Segment> segments) {
        List<SegmentWrapper> newSegmentList = new ArrayList();
        newSegmentList.addAll(sedWrapper.getSegmentList());
        for(Segment segment : segments)
            newSegmentList.add(new SegmentWrapper(segment, null));
        setSedWrapper(new SedWrapper(newSegmentList));
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    private class PosRenderer extends JLabel implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable jtable, Object o, boolean isSelected, boolean hasFocus, int row, int col) {
            DoubleParam[] radec = ((DoubleParam[]) o);

            String raS, decS;
            if(radec[0]==null)
                raS = "-";
            else
                raS = Double.valueOf(radec[0].getValue()).isNaN() ? "-" : roundToSignificantFigures(Double.valueOf(radec[0].getValue()), 5).toString();
            if(radec[1]==null)
                decS = "-";
            else
                decS = Double.valueOf(radec[1].getValue()).isNaN() ? "-" : roundToSignificantFigures(Double.valueOf(radec[1].getValue()), 5).toString();
            String content = raS+", "+decS;
            setText(content);
            
            if(isSelected) {
                this.setBackground(UIManager.getDefaults().getColor("Table.selectionBackground"));
                this.setForeground(UIManager.getDefaults().getColor("Table.selectionForeground"));
            }
            else {
                this.setBackground(UIManager.getDefaults().getColor("Table.Background"));
                this.setForeground(UIManager.getDefaults().getColor("Table.Foreground"));
            }
            this.setOpaque(true);
            return this;
        }

        private Double roundToSignificantFigures(double num, int n) {
            if(num == 0) {
                return 0d;
            }

            final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
            final int power = n - (int) d;

            final double magnitude = Math.pow(10, power);
            final long shifted = Math.round(num*magnitude);
            return shifted/magnitude;
        }



    }

    private boolean acceptingSAMP = true;
    public static final String PROP_ACCEPTINGSAMP = "acceptingSAMP";

    /**
     * Get the value of acceptingSAMP
     *
     * @return the value of acceptingSAMP
     */
    public boolean isAcceptingSAMP() {
        return acceptingSAMP;
    }

    /**
     * Set the value of acceptingSAMP
     *
     * @param acceptSAMP new value of acceptingSAMP
     */
    public void setAcceptingSAMP(boolean acceptingSAMP) {
        boolean oldAcceptingSAMP = this.acceptingSAMP;
        this.acceptingSAMP = acceptingSAMP;
        firePropertyChange(PROP_ACCEPTINGSAMP, oldAcceptingSAMP, acceptingSAMP);
    }


    @Action
    public void broadcastSed() {
        try {
                Sed sed = sedWrapper.makeSed();
                SedImporterApp.sendSedMessage(sed, sedID);
            } catch (Exception ex) {
                NarrowOptionPane.showMessageDialog(MainView.getInstance(),
                        "Error while broadcasting the SED",
                        "SAMP Error",
                        NarrowOptionPane.ERROR_MESSAGE);
            }
    }

    @Action
    public void editSegments() {
        for(Iterator<SegmentWrapper> i = selectedSegments.iterator(); i.hasNext(); ) {
            SegmentWrapper sw = i.next();
            if(sw.getSetupFrame()!=null) {
                for(NewSedFrame fr : sw.getSetupFrame().getSedFrameList()) {
                    fr.getSedWrapper().removeSegment(sw);
                }
                i.remove();
                sw.getSetupFrame().setClosable(false);
                sw.getSetupFrame().setImportButtonLabel("Edit");
                sw.getSetupFrame().refresh();
                sw.getSetupFrame().setVisible(true);
            } else {
                NarrowOptionPane.showMessageDialog(MainView.getInstance(),
                        "The segment was imported 'as is', for example from NED or from file, so it can't be edited.",
                        "Editing not available for this segment",
                        NarrowOptionPane.WARNING_MESSAGE);
            }
        }
    }

    @Action
    public void broadcastSegments() {
        try {
            Sed sed = new Sed();
            for(SegmentWrapper sw : selectedSegments) {
                sed.addSegment(sw.getSegment());
            }
            SedImporterApp.sendSedMessage(sed, sedID+"_selection");
        } catch (Exception ex) {
            NarrowOptionPane.showMessageDialog(MainView.getInstance(),
                        ex.getMessage(),
                        "SAMP Error",
                        NarrowOptionPane.ERROR_MESSAGE);
        }

    }

    


}
