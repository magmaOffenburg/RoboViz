package config;

import config.RVConfigure.SaveListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import rv.Configuration;
import rv.util.swing.FileChooser;
import rv.util.swing.SwingUtil;

public class GeneralPanel extends JPanel implements SaveListener
{
	final RVConfigure configProg;
	final Configuration.General config;

	JCheckBox recordLogsCB;
	JTextField logDirectoryTF;
	JButton openDirectoryButton;

	private Runnable onChange;

	public GeneralPanel(RVConfigure configProg)
	{
		super();
		this.configProg = configProg;
		this.config = configProg.config.general;
		configProg.listeners.add(this);
		onChange = configProg.updateSaveButtonState;
		initGUI();
	}

	void initGUI()
	{
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;

		add(initLogfilesPanel(), c);
	}

	JPanel initLogfilesPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Logfiles"));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 10;

		recordLogsCB = new JCheckBox("Record Logfiles", config.recordLogs);
		recordLogsCB.addChangeListener(e -> {
			config.recordLogs = recordLogsCB.isSelected();
			onChange.run();
		});
		logDirectoryTF = new JTextField(config.logfileDirectory);
		logDirectoryTF.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				config.logfileDirectory = logDirectoryTF.getText();
				onChange.run();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				config.logfileDirectory = logDirectoryTF.getText();
				onChange.run();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				config.logfileDirectory = logDirectoryTF.getText();
				onChange.run();
			}
		});
		SwingUtil.setPreferredWidth(logDirectoryTF, 150);
		openDirectoryButton = new JButton("...");
		openDirectoryButton.addActionListener(e -> {
			JFileChooser fileChooser = new FileChooser(logDirectoryTF.getText());
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setAcceptAllFileFilterUsed(false);
			if (fileChooser.showOpenDialog(configProg) != JFileChooser.CANCEL_OPTION)
				logDirectoryTF.setText(fileChooser.getSelectedFile().getAbsolutePath());
		});

		panel.add(recordLogsCB, c);

		c.gridy = 1;
		panel.add(new JLabel("Logfiles Directory: "), c);
		panel.add(logDirectoryTF, c);
		panel.add(openDirectoryButton, c);

		return panel;
	}

	@Override
	public void configSaved(RVConfigure configProg)
	{
		config.recordLogs = recordLogsCB.isSelected();
		config.logfileDirectory = logDirectoryTF.getText();
	}
}
