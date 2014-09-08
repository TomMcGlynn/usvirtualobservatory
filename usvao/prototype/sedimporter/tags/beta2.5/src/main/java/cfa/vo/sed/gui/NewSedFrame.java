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
 * NewSedFrame.java
 *
 * Created on May 6, 2011, 4:14:57 PM
 */

package cfa.vo.sed.gui;

import cfa.vo.sed.filters.IFileFormat;
import cfa.vo.sed.importer.ISegmentMetadata;
import cfa.vo.sed.importer.NEDImporter;
import cfa.vo.sedlib.DoubleParam;
import cfa.vo.sedlib.Segment;
import cfa.vo.utils.NarrowOptionPane;
import cfa.vo.utils.SkyCoordinates;
import java.awt.Component;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author olaurino
 */
public final class NewSedFrame extends ConfirmJInternalFrame {

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
        ra = SkyCoordinates.getRaDegString(ra);
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
        dec = SkyCoordinates.getDecDegString(dec);
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

    private Segment selectedSegment;
    public static final String PROP_SELECTEDSEGMENT = "selectedSegment";

    /**
     * Get the value of selectedSegment
     *
     * @return the value of selectedSegment
     */
    public Segment getSelectedSegment() {
        return selectedSegment;
    }

    /**
     * Set the value of selectedSegment
     *
     * @param selectedSegment new value of selectedSegment
     */
    public void setSelectedSegment(Segment selectedSegment) {
        Segment oldSelectedSegment = this.selectedSegment;
        this.selectedSegment = selectedSegment;
        firePropertyChange(PROP_SELECTEDSEGMENT, oldSelectedSegment, selectedSegment);
        List<Segment> l = new ArrayList();
        l.add(selectedSegment);
        setSelectedSegments(l);
    }


    private List<Segment> selectedSegments;
    public static final String PROP_SELECTEDSEGMENTS = "selectedSegments";

    /**
     * Get the value of selectedSegments
     *
     * @return the value of selectedSegments
     */
    public List<Segment> getSelectedSegments() {
        return selectedSegments;
    }

    /**
     * Set the value of selectedSegments
     *
     * @param selectedSegments new value of selectedSegments
     */
    public void setSelectedSegments(List<Segment> selectedSegments) {
        List<Segment> oldSelectedSegments = this.selectedSegments;
        this.selectedSegments = selectedSegments;
        setIsSegmentSelected(!selectedSegments.isEmpty());
        firePropertyChange(PROP_SELECTEDSEGMENTS, oldSelectedSegments, selectedSegments);
    }


    private List<Segment> segmentList = new ArrayList();
    public static final String PROP_SEGMENTLIST = "segmentList";

    /**
     * Get the value of segmentList
     *
     * @return the value of segmentList
     */
    public List<Segment> getSegmentList() {
        return segmentList;
    }

    /**
     * Set the value of segmentList
     *
     * @param segmentList new value of segmentList
     */
    public void setSegmentList(List<Segment> segmentList) {
        List<Segment> oldSegmentList = this.segmentList;
        this.segmentList = segmentList;
        firePropertyChange(PROP_SEGMENTLIST, oldSegmentList, segmentList);
        setSegmentCount(segmentList.size());
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

        jPanel3 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setResizable(true);
        setAutoscrolls(true);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${sedID}"), this, org.jdesktop.beansbinding.BeanProperty.create("title"));
        bindingGroup.addBinding(binding);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Segments Operations"));

        jButton4.setText("Save Segment(s)");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${isSegmentSelected}"), jButton4, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSegmentList(evt);
            }
        });

        jButton3.setText("Broadcast Segment(s)");
        jButton3.setEnabled(false);

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

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(17, 17, 17)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButton3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButton4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jButton1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton4)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "SED"));

        jLabel5.setText("Segments #:");

        jTextField5.setEditable(false);
        jTextField5.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${segmentCount}"), jTextField5, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jLabel1.setText("ID:");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${sedID}"), jTextField1, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jButton6.setText("Broadcast SED");
        jButton6.setEnabled(false);
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

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
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTable1.setBounds(new java.awt.Rectangle(50, 50, 1000, 700));
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${segmentList}");
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
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(100);
        jTable1.getColumnModel().getColumn(0).setCellRenderer(new PosRenderer());
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(170);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(20);

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
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
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
                .addContainerGap(45, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 318, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 442, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3, 0, 212, Short.MAX_VALUE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jScrollPane1, 0, 0, Short.MAX_VALUE)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
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

                try {//TODO Multiple segments
                    ISegmentMetadata md = dialog.getFormat().getFilter().getMetadata(dialog.getURL()).get(0);
                    SetupFrame cf = new SetupFrame("New Segment", md, this, dialog.getURL().toString(), dialog.getFormat());
                    desktopPane.add(cf);
                    cf.setVisible(true);
                } catch (Exception ex) {
                    NarrowOptionPane.showMessageDialog(MainView.getInstance(),
                            ex.getMessage(),
                            "Import Error",
                            NarrowOptionPane.ERROR_MESSAGE);
                    dialog.setVisible(true);
                    this.addSegment(dialog);
                }

            } else if(dialog.hasSegments()) {//or there is a set of segments from NED
                List<Segment> segList = dialog.getSegments();
                for(Segment segment : segList) {
                    if(!segment.createTarget().createPos().isSetValue()) {
                        Double[] coords = new Double[]{0d, 0d};
                        
                        if(getRa()==null || getDec()==null) {
                                coords = NameResolver.resolve(segment.getTarget().getName().getValue());
                                if(coords==null)
                                    coords = new Double[] {Double.NaN, Double.NaN };
                        }

                        Double raD = getRa()!=null? Double.valueOf(getRa()) : coords[0];
                        Double decD = getDec()!=null? Double.valueOf(getDec()) : coords[1];
                        DoubleParam[] coordsParam = new DoubleParam[]{new DoubleParam(raD), new DoubleParam(decD)};
                        segment.getTarget().getPos().setValue(coordsParam);
                        segment = NEDImporter.fixNEDSegment(segment);//FIXME STOPGAP MEASURE
                    }
                    
                }
                addSegments(segList);
            }

        }
    }

    public void addSegment(URL url, IFileFormat format) {
        try {
            ISegmentMetadata md = format.getFilter().getMetadata(url).get(0);
            SetupFrame cf = new SetupFrame("New Segment", md, this, url.toString(), format);
            desktopPane.add(cf);
            cf.setVisible(true);
        } catch (Exception ex) {
            NarrowOptionPane.showMessageDialog(MainView.getInstance(),
                            ex.getMessage(),
                            "Import Error",
                            NarrowOptionPane.ERROR_MESSAGE);
        }
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
        SaveSedDialog dia = new SaveSedDialog(MainView.getInstance(), segmentList, sedID);
        dia.setVisible(true);
    }//GEN-LAST:event_saveSed

    private void saveSegmentList(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSegmentList
        SaveSedDialog dia = new SaveSedDialog(MainView.getInstance(), selectedSegments, sedID);
        dia.setVisible(true);
    }//GEN-LAST:event_saveSegmentList

    private void removeSelectedSegments(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSelectedSegments
        int ans = NarrowOptionPane.showConfirmDialog(MainView.getInstance(),
                "Are you sure you want to remove the selected segments from the SED?",
                "Confirm removal",
                NarrowOptionPane.YES_NO_OPTION);
        if(ans==NarrowOptionPane.YES_OPTION) {
            List newSegmentList = new ArrayList();
            newSegmentList.addAll(segmentList);
            for(Segment s : selectedSegments)
                newSegmentList.remove(s);
            setSegmentList(newSegmentList);
            setSelectedSegments(new ArrayList());
        }
    }//GEN-LAST:event_removeSelectedSegments

    private void reset(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reset
        int ans = NarrowOptionPane.showConfirmDialog(MainView.getInstance(),
                "Are you sure you want to reset this SED?",
                "Confirm reset",
                NarrowOptionPane.YES_NO_OPTION);
        if(ans==NarrowOptionPane.YES_OPTION) {
            setSegmentList(new ArrayList());
            setSelectedSegments(new ArrayList());
            setTargetName("");
            setRa(null);
            setDec(null);
        }
    }//GEN-LAST:event_reset

    public void addSegment(Segment segment) {
        List newSegmentList = new ArrayList();
        newSegmentList.addAll(segmentList);
        newSegmentList.add(segment);
        setSegmentList(newSegmentList);
    }
    
    public void addSegments(List<Segment> segments) {
        List newSegmentList = new ArrayList();
        newSegmentList.addAll(segmentList);
        newSegmentList.addAll(segments);
        setSegmentList(newSegmentList);
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

        public Component getTableCellRendererComponent(JTable jtable, Object o, boolean isSelected, boolean hasFocus, int row, int col) {
            DoubleParam[] radec = ((DoubleParam[]) o);

            if(radec==null)
                radec = new DoubleParam[]{new DoubleParam(Double.NaN), new DoubleParam(Double.NaN)};

            String content = radec[0].getValue()+" "+radec[1].getValue();
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



    }

}
