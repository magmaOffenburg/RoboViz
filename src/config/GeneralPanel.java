package config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import config.RVConfigure.SaveListener;
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

	public GeneralPanel(RVConfigure configProg)
	{
		super();
		this.configProg = configProg;
		this.config = configProg.config.general;
		configProg.listeners.add(this);
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
		logDirectoryTF = new JTextField(config.logfileDirectory);
		SwingUtil.setPreferredWidth(logDirectoryTF, 150);
		openDirectoryButton = new JButton("...");
		openDirectoryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fileChooser = new FileChooser(logDirectoryTF.getText());
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);
				if (fileChooser.showOpenDialog(configProg) != JFileChooser.CANCEL_OPTION)
					logDirectoryTF.setText(fileChooser.getSelectedFile().getAbsolutePath());
			}
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
