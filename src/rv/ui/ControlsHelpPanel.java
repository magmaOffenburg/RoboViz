package rv.ui;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

public class ControlsHelpPanel extends FramePanelBase {

    public ControlsHelpPanel() {
        super("Help");
        frame.setSize(400, 500);
        frame.setMinimumSize(new Dimension(400, 500));

        String file = "Could not load help page.";
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get("resources/help/controls.html"),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
