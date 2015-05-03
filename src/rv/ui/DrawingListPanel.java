/*
 *  Copyright 2011 RoboViz
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package rv.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import rv.Globals;
import rv.comm.drawing.BufferedSet;
import rv.comm.drawing.Drawings;
import rv.comm.drawing.Drawings.SetListChangeEvent;
import rv.comm.drawing.Drawings.ShapeListListener;
import rv.comm.drawing.VisibleNamedObject;
import rv.comm.drawing.annotations.Annotation;
import rv.comm.drawing.shapes.Shape;

/**
 * TODO: lots of work on this class; should use a JTree instead of JList
 * 
 * @author justin
 * 
 */
public class DrawingListPanel extends FramePanelBase implements ShapeListListener {

    static class CheckListRenderer extends JCheckBox implements ListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean hasFocus) {
            setEnabled(list.isEnabled());
            setSelected(((CheckListItem) value).isSelected());
            setFont(list.getFont());
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setText(value.toString());
            return this;
        }
    }

    static class CheckListItem {
        private VisibleNamedObject item;
        private String             label;
        private boolean            isSelected = false;

        public CheckListItem(VisibleNamedObject item) {
            this.item = item;
            this.label = item.getName();
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
            item.setVisible(isSelected);
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private class Cell extends JComponent {
        private String    setName;
        private JLabel    text;
        private JCheckBox checkbox;

        public Cell(String text, boolean state) {
            this.setName = text;
            this.text = new JLabel(text);
            this.checkbox = new JCheckBox();
            checkbox.setSelected(state);
            checkbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    BufferedSet<Shape> p = drawings.getShapeSet(setName);
                    if (p != null)
                        p.setVisible(!p.isVisible());

                    BufferedSet<Annotation> p1 = drawings.getAnnotationSet(setName);
                    if (p1 != null)
                        p1.setVisible(!p1.isVisible());
                }
            });

            this.setLayout(new GridLayout(0, 2));
            add(this.text);
            add(this.checkbox);
        }
    }

    private Drawings       drawings;
    private JTextField     regexField;
    private JList          list;
    final DefaultListModel model = new DefaultListModel();

    public DrawingListPanel(Drawings drawings) {

        super("Drawings");
        frame.setIconImage(Globals.getIcon());
        frame.setAlwaysOnTop(true);
        list = new JList(model);
        frame.setSize(300, 600);
        frame.setMinimumSize(new Dimension(300, 200));

        list.setCellRenderer(new CheckListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                // TODO: try/catch really needed?
                try {
                    JList list = (JList) event.getSource();
                    int index = list.locationToIndex(event.getPoint());
                    CheckListItem item = (CheckListItem) list.getModel().getElementAt(index);
                    item.setSelected(!item.isSelected());
                    list.repaint(list.getCellBounds(index, index));
                } catch (Exception e) {
                }
            }
        });

        frame.setLayout(new BorderLayout());

        frame.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1, 3));
        regexField = new JTextField(".*");

        p.add(regexField);
        JButton regexSearch = new JButton("Regex");
        regexSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                regexList(regexField.getText());
            }
        });
        p.add(regexSearch);
        regexField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    regexList(regexField.getText());
            }
        });
        frame.add(p, BorderLayout.SOUTH);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                DrawingListPanel.this.drawings.clearAllShapeSets();
            }
        });
        p.add(clearButton);

        this.drawings = drawings;
        drawings.addShapeSetListener(this);

        frame.pack();
        frame.setSize(300, 600);
        // TODO: shouldnt do this, just grab pools on init
        drawings.clearAllShapeSets();
    }

    private void regexList(String s) {
        for (int i = 0; i < model.getSize(); i++) {
            CheckListItem cli = ((CheckListItem) model.getElementAt(i));
            cli.setSelected(cli.item.getName().matches(s));
        }
        list.repaint();
    }

    @Override
    public void setListChanged(SetListChangeEvent evt) {
        String regex = regexField.getText();

        model.clear();
        ArrayList<BufferedSet<Shape>> shapeSets = evt.getShapeSets();
        int size = shapeSets.size();
        for (int i = 0; i < size; i++) {
            if (shapeSets.get(i) != null) {
                CheckListItem item = new CheckListItem(shapeSets.get(i));
                boolean visible = shapeSets.get(i).isVisible();
                boolean matchRegex = regex == null ? true : shapeSets.get(i).getName()
                        .matches(regex);
                item.setSelected(visible && matchRegex);
                model.addElement(item);
            }
        }

        ArrayList<BufferedSet<Annotation>> annotationSets = evt.getAnnotationSets();
        int size2 = annotationSets.size();
        for (int i = 0; i < size2; i++) {
            if (annotationSets.get(i) != null) {
                CheckListItem item = new CheckListItem(annotationSets.get(i));
                boolean visible = annotationSets.get(i).isVisible();
                boolean matchRegex = regex == null ? true : annotationSets.get(i).getName()
                        .matches(regex);
                item.setSelected(visible && matchRegex);
                model.addElement(item);
            }
        }
    }
}
