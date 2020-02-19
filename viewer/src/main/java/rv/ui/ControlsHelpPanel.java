package rv.ui;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

public class ControlsHelpPanel extends FramePanelBase
{
	public ControlsHelpPanel()
	{
		super("Help");
		addCloseHotkey();
		frame.setSize(600, 800);
		frame.setMinimumSize(new Dimension(400, 500));

		String file = "Could not load help page.";
		List<String> lines = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("/help/controls.html"), StandardCharsets.UTF_8))
									 .lines()
									 .collect(Collectors.toList());

		if (lines != null) {
			StringBuilder builder = new StringBuilder();
			for (String line : lines) {
				builder.append(line);
			}
			file = builder.toString();
		}

		JEditorPane textArea = new JEditorPane();
		textArea.setContentType("text/html");
		textArea.setEditable(false);
		textArea.setFocusable(false);
		textArea.setText(file);
		frame.add(new JScrollPane(textArea));
	}
}
