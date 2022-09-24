/*
 *  Copyright 2011 Justin Stoecker
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

package jsgl.jogl.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Records a camera's movement and orientation over time. While the state is set
 * to recording, the camera position and orientation is periodically saved. When
 * the state is set to playing, the list of recorded camera positions is
 * traversed in order. The position and rotation of the camera is interpolated
 * between key frames, and the most current values are exposed to other classes.
 *
 * @author Justin Stoecker
 */
public class StateRecorder
{
	/**
	 * Interface for a set of a values that are recorded and interpolated. Make
	 * sure any class that implements this has a default constructor. upon
	 * playback
	 */
	public interface FrameData
	{
		/** Interpolation between current frame data and next frame data */
		FrameData interpolate(FrameData next, float s);

		/** Parse values from a line */
		void parseValues(String[] vals);

		/** Returns all values in a space-separated string */
		String getValues();
	}

	/** A snapshot of data */
	public static class KeyFrame
	{
		public FrameData data;
		public float transitionTime; // time to transition to frame (in ms)

		public KeyFrame(FrameData data, float transitionTime)
		{
			this.data = data;
			this.transitionTime = transitionTime;
		}
	}

	/** Possible states of the camcorder */
	public enum State
	{
		Playing,
		Recording,
		Idle,
		FinishedPlaying
	}
	;

	private final int MAX_FRAMES;
	private ArrayList<KeyFrame> keyFrames;
	private float msPerFrame;
	private FrameData curData;
	private KeyFrame current;
	private KeyFrame next;
	private int curFrame = 0;
	private float playSpeed = 1.0f;
	private float timer = 0;
	private State state = State.Idle;

	public ArrayList<KeyFrame> getFrames()
	{
		return keyFrames;
	}

	public FrameData getCurrentData()
	{
		return curData;
	}

	public State getState()
	{
		return state;
	}

	public int getCurFrame()
	{
		return curFrame;
	}

	public int getFrameCount()
	{
		return keyFrames.size();
	}

	public void setPlaySpeed(float s)
	{
		playSpeed = s;
	}

	/**
	 * Creates a new camcorder that can record a camera's movement
	 *
	 * @param camera
	 *           - the camera used for recording
	 * @param msPerFrame
	 *           - interval at which keyframes are recorded
	 * @param maxFrames
	 *           - maximum number of keyframes the recorder will capture
	 */
	public StateRecorder(float msPerFrame, int maxFrames)
	{
		this.msPerFrame = msPerFrame;
		this.MAX_FRAMES = maxFrames;
		keyFrames = new ArrayList<>(MAX_FRAMES / 4);
	}

	/** Starts playing the recorded path */
	public void startPlaying()
	{
		if (keyFrames == null || keyFrames.size() == 0)
			return;

		state = State.Playing;
		curFrame = -1;
		timer = 0;
		nextFrame();
	}

	/** Stops playing or recording */
	public void stop()
	{
		state = State.Idle;
	}

	/** Starts path recording */
	public void startRecording()
	{
		keyFrames.clear();
		state = State.Recording;
		timer = 0;
	}

	/** Updates the camcorder state */
	public void update(double elapsedMS)
	{
		switch (state) {
		case Playing:
			play(elapsedMS);
			break;
		default:
			break;
		}
	}

	/** Continue playing the recorded path */
	private void play(double elapsedMS)
	{
		float s = timer / next.transitionTime;
		if (s >= 1.0f) {
			timer -= next.transitionTime;
			if (curFrame == keyFrames.size() - 2) {
				state = State.FinishedPlaying;
				return;
			}
			nextFrame();
		} else
			curData = current.data.interpolate(next.data, s);

		timer += elapsedMS * playSpeed;
	}

	/** Sets current frame to the next frame */
	private void nextFrame()
	{
		curFrame++;
		current = keyFrames.get(curFrame);
		next = keyFrames.get(curFrame + 1);
		curData = current.data;
	}

	public void addFrame(FrameData data)
	{
		keyFrames.add(new KeyFrame(data, msPerFrame));
	}

	/**
	 * Loads a recording from a specified file with a given FrameData class type
	 */
	public void load(File f, Class<?> frameDataClass) throws IOException, InstantiationException, IllegalAccessException
	{
		keyFrames.clear();
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line;
		while ((line = br.readLine()) != null) {
			FrameData data = (FrameData) frameDataClass.newInstance();
			String[] vals = line.split(" ");
			data.parseValues(vals);
			float transitionTime = Float.parseFloat(vals[vals.length - 1]);
			keyFrames.add(new KeyFrame(data, transitionTime));
		}
		br.close();
	}

	/**
	 * Saves the recording to a specified file
	 */
	public void save(File f) throws IOException
	{
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 0; i < keyFrames.size(); i++) {
			bw.write(keyFrames.get(i).data.getValues());
			bw.write(" " + keyFrames.get(i).transitionTime);
			if (i != keyFrames.size() - 1)
				bw.write('\n');
		}
		bw.close();
	}
}
