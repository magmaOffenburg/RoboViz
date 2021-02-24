package rv.util.swing;

import java.awt.Component;
import java.awt.HeadlessException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

public class FileChooser extends JFileChooser
{
	public FileChooser()
	{
		super();
	}

	public FileChooser(String currentDirectoryPath)
	{
		super(currentDirectoryPath);
	}

	@Override
	protected JDialog createDialog(Component parent) throws HeadlessException
	{
		JDialog dialog = super.createDialog(parent);
		SwingUtil.setLocationRelativeTo(dialog, parent);
		return dialog;
	}
}
