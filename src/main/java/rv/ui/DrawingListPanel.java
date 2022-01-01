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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
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
public class DrawingListPanel extends FramePanelBase implements ShapeListListener
{
	static class CheckListRenderer extends JCheckBox implements ListCellRenderer<CheckListItem>
	{
		@Override
		public Component getListCellRendererComponent(JList<? extends CheckListItem> list, CheckListItem value,
				int index, boolean isSelected, boolean cellHasFocus)
		{
			setEnabled(list.isEnabled());
			setSelected(value.isSelected());
			setFont(list.getFont());
			setBackground(list.getBackground());
			setForeground(list.getForeground());
			setText(value.toString());
			return this;
		}
	}

	static class CheckListItem
	{
		private final VisibleNamedObject item;
		private final String label;
		private boolean isSelected = false;

		public CheckListItem(VisibleNamedObject item)
		{
			this.item = item;
			this.label = item.getName();
		}

		public boolean isSelected()
		{
			return isSelected;
		}

		public void setSelected(boolean isSelected)
		{
			this.isSelected = isSelected;
			item.setVisible(isSelected);
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	private static final Pattern DOTALL_PATTERN = Pattern.compile(".*");

	private Drawings drawings;
	private final JTextField regexField;
	private final JList<CheckListItem> list;
	final DefaultListModel<CheckListItem> model = new DefaultListModel<>();

	public DrawingListPanel(Drawings drawings, String drawingFilter)
	{
		super("Drawings");
		addCloseHotkey();
		frame.setAlwaysOnTop(true);
		list = new JList<>(model);
		frame.setSize(300, 600);
		frame.setMinimumSize(new Dimension(300, 200));

		list.setCellRenderer(new CheckListRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event)
			{
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
		regexField = new JTextField(drawingFilter);

		p.add(regexField);
		JButton regexSearch = new JButton("Regex");
		regexSearch.addActionListener(e -> regexList(getRegex()));
		p.add(regexSearch);
		regexField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e)
			{
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					regexList(getRegex());
			}
		});
		frame.add(p, BorderLayout.SOUTH);

		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(arg0 -> DrawingListPanel.this.drawings.clearAllShapeSets());
		p.add(clearButton);

		this.drawings = drawings;
		drawings.addShapeSetListener(this);

		frame.pack();
		frame.setSize(300, 600);
		// TODO: shouldn't do this, just grab pools on init
		drawings.clearAllShapeSets();
	}

	private void regexList(Pattern p)
	{
		for (int i = 0; i < model.getSize(); i++) {
			CheckListItem cli = (model.getElementAt(i));
			String name = cli.item.getName();
			cli.setSelected(p.matcher(name).matches());
		}
		list.repaint();
	}

	@Override
	public void setListChanged(SetListChangeEvent evt)
	{
		Pattern regex = getRegex();

		model.clear();
		List<BufferedSet<Shape>> shapeSets = evt.getShapeSets();
		for (BufferedSet<Shape> shapeSet : shapeSets) {
			if (shapeSet != null) {
				CheckListItem item = new CheckListItem(shapeSet);
				boolean visible = shapeSet.isVisible();
				boolean matchRegex = regex.matcher(shapeSet.getName()).matches();
				item.setSelected(visible && matchRegex);
				model.addElement(item);
			}
		}

		List<BufferedSet<Annotation>> annotationSets = evt.getAnnotationSets();
		for (BufferedSet<Annotation> annotationSet : annotationSets) {
			if (annotationSet != null) {
				CheckListItem item = new CheckListItem(annotationSet);
				boolean visible = annotationSet.isVisible();
				boolean matchRegex = regex.matcher(annotationSet.getName()).matches();
				item.setSelected(visible && matchRegex);
				model.addElement(item);
			}
		}
	}

	private Pattern getRegex()
	{
		String s = regexField.getText();
		if (s == null) {
			// Match everything
			return DOTALL_PATTERN;
		}

		Pattern pattern;
		try {
			pattern = Pattern.compile(s);
		} catch (PatternSyntaxException e) {
			// Invalid regex
			// Match everything instead
			pattern = DOTALL_PATTERN;
		}
		return pattern;
	}
}
