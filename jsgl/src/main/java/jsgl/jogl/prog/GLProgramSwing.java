package jsgl.jogl.prog;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;
import java.awt.BorderLayout;
import javax.swing.JFrame;

public abstract class GLProgramSwing extends GLProgram
{
	private final GLCanvas canvas;
	private final JFrame frame;

	public JFrame getFrame()
	{
		return frame;
	}

	public GLProgramSwing(String name, int w, int h, GLCapabilities caps)
	{
		super(w, h);

		canvas = new GLCanvas(caps);
		frame = new JFrame(name);
		frame.setLayout(new BorderLayout());
		frame.add(canvas, BorderLayout.CENTER);
		frame.setSize(w, h);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent windowevent)
			{
				frame.dispose();
				System.exit(0);
			}
		});

		frame.setVisible(true);

		attachDrawableAndStart(canvas);
	}

	@Override
	public void addKeyListener(KeyListener l)
	{
		new AWTKeyAdapter(l, canvas).addTo(canvas);
	}

	@Override
	public void addMouseListener(MouseListener l)
	{
		new AWTMouseAdapter(l, canvas).addTo(canvas);
	}
}
