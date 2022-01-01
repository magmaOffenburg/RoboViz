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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;
import javax.swing.Timer;

/**
 * Program for testing network drawing on RoboViz with all shapes both animated
 * and static
 *
 * @author Justin Stoecker
 */
public class RVTester
{
	private static final int TEST_DURATION = 10000;
	private static final int ROBOVIZ_PORT = 32769;

	private DatagramSocket socket;
	private InetAddress address;
	private Color lightGreen = new Color(0.6f, 0.9f, 0.6f);
	private Timer animationTimer;
	private float[] a = {0, 0, 0};
	private float[] b = {0, 0, 1};
	private double angle = 0;

	public RVTester() throws SocketException, UnknownHostException
	{
		socket = new DatagramSocket();
		address = InetAddress.getLoopbackAddress();

		animationTimer = new Timer(16, arg0 -> {
			try {
				renderAnimatedShapes();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		animationTimer.setRepeats(true);
	}

	/** Method for all animated drawings */
	private void renderAnimatedShapes() throws IOException
	{
		angle += 0.05;

		// draw points wave
		for (int i = 0; i < 30; i++) {
			float p = i / 30.0f;
			float height = Math.max(0, (float) (Math.sin(angle + p * 18)));
			float[] pos = new float[] {-9 + 18 * p, p * 12 - 6, height};
			drawPoint(pos, 5, Color.BLACK, "animated.points");
		}

		// draw spinning triangle
		b[0] = (float) Math.cos(angle) * 2;
		b[1] = (float) Math.sin(angle) * 2;
		b[2] = (float) Math.cos(angle) + 1.5f;
		float[] c = {b[0], b[1], 0};
		drawLine(a, b, 5.0f, Color.YELLOW, "animated.spinner");
		drawLine(b, c, 5.0f, Color.YELLOW, "animated.spinner");
		drawLine(a, c, 5.0f, Color.YELLOW, "animated.spinner");

		drawAnnotation(String.format(Locale.US, "%.1f", b[2]), b, Color.GREEN, "animated.annotation");

		drawAgentAnnotation(String.format(Locale.US, "%.2f", b[0]), true, 1, Color.CYAN);

		// swap all sets starting with "animated"
		swapBuffers("animated");
	}

	/** Method for all static drawings */
	private void renderStaticShapes() throws IOException
	{
		// draw 3D coordinate axes
		drawLine(new float[] {0, 0, 0}, new float[] {3, 0, 0}, 3.0f, Color.RED, "static.axes");
		drawLine(new float[] {0, 0, 0}, new float[] {0, 3, 0}, 3.0f, Color.GREEN, "static.axes");
		drawLine(new float[] {0, 0, 0}, new float[] {0, 0, 3}, 3.0f, Color.BLUE, "static.axes");

		// draw 1 meter lines on field
		drawLine(new float[] {-9, -6, 0}, new float[] {9, -6, 0}, 1.0f, lightGreen, "static.lines.field");
		drawLine(new float[] {-9, 6, 0}, new float[] {9, 6, 0}, 1.0f, lightGreen, "static.lines.field");
		for (int i = 0; i <= 18; i++)
			drawLine(new float[] {-9 + i, -6, 0}, new float[] {-9 + i, 6, 0}, 1.0f, lightGreen, "static.lines.field");

		// draw some circles
		drawCircle(new float[] {-5, 0}, 3, 2, Color.BLUE, "static.circles");
		drawCircle(new float[] {5, 0}, 3, 2, Color.BLUE, "static.circles");

		// draw some spheres
		drawSphere(new float[] {-5, 0, 2}, 0.5f, Color.PINK, "static.spheres");
		drawSphere(new float[] {5, 0, 2}, 0.5f, Color.PINK, "static.spheres");

		drawAnnotation("hello\nworld", new float[] {0, 0, 2}, Color.GREEN, "static.annotations");

		// draw a polygon
		float[][] v = {
				{0, 0, 0},
				{1, 0, 0},
				{1, 1, 0},
				{0, 3, 0},
				{-2, -2, 0},
		};
		drawPolygon(v, new Color(1.0f, 1.0f, 1.0f, 0.5f), "static.polygons");

		drawAgentAnnotation("testing", true, 1, Color.red);
		drawAgentAnnotation("I'm agent #2", true, 2, Color.yellow);

		swapBuffers("static");
	}

	/** Method for selecting all agents */
	private void selectAgents() throws Exception
	{
		// Select left team agents
		for (int i = 1; i <= 11; i++) {
			selectAgent(true, i);
			Thread.sleep(500);
		}

		// Select right team agents
		for (int i = 1; i <= 11; i++) {
			selectAgent(false, i);
			Thread.sleep(500);
		}
	}

	public void runTest() throws IOException
	{
		animationTimer.start();
		renderStaticShapes();
	}

	private void swapBuffers(String group) throws IOException
	{
		byte[] buf = RVDraw.newBufferSwap(group);
		socket.send(new DatagramPacket(buf, buf.length, address, ROBOVIZ_PORT));
	}

	public void drawCircle(float[] center, float radius, float thickness, Color color, String group) throws IOException
	{
		byte[] buf = RVDraw.newCircle(center, radius, thickness, color, group);
		socket.send(new DatagramPacket(buf, buf.length, address, ROBOVIZ_PORT));
	}

	public void drawLine(float[] a, float[] b, float thickness, Color color, String group) throws IOException
	{
		byte[] buf = RVDraw.newLine(a, b, thickness, color, group);
		socket.send(new DatagramPacket(buf, buf.length, address, ROBOVIZ_PORT));
	}

	public void drawPoint(float[] p, float size, Color color, String group) throws IOException
	{
		byte[] buf = RVDraw.newPoint(p, size, color, group);
		socket.send(new DatagramPacket(buf, buf.length, address, ROBOVIZ_PORT));
	}

	public void drawSphere(float[] p, float radius, Color color, String group) throws IOException
	{
		byte[] buf = RVDraw.newSphere(p, radius, color, group);
		socket.send(new DatagramPacket(buf, buf.length, address, ROBOVIZ_PORT));
	}

	public void drawPolygon(float[][] v, Color color, String set) throws IOException
	{
		byte[] buf = RVDraw.newPolygon(v, color, set);
		socket.send(new DatagramPacket(buf, buf.length, address, ROBOVIZ_PORT));
	}

	public void drawAnnotation(String text, float[] pos, Color color, String set) throws IOException
	{
		byte[] buf = RVDraw.newAnnotation(text, pos, color, set);
		socket.send(new DatagramPacket(buf, buf.length, address, ROBOVIZ_PORT));
	}

	public void drawAgentAnnotation(String text, boolean leftTeam, int agentNum, Color color) throws IOException
	{
		byte[] buf = RVDraw.newAgentAnnotation(text, leftTeam, agentNum, color);
		socket.send(new DatagramPacket(buf, buf.length, address, ROBOVIZ_PORT));
	}

	public void selectAgent(boolean leftTeam, int agentNum) throws IOException
	{
		byte[] buf = RVDraw.newSelectAgent(leftTeam, agentNum);
		socket.send(new DatagramPacket(buf, buf.length, address, ROBOVIZ_PORT));
	}

	public static void main(String[] args) throws Exception
	{
		RVTester tester = new RVTester();
		tester.runTest();
		Thread.sleep(TEST_DURATION);
		tester.drawAgentAnnotation(null, true, 1, Color.CYAN);
		tester.animationTimer.stop();
		tester.selectAgents();
	}
}
