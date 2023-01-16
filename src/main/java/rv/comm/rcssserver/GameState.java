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

package rv.comm.rcssserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.magmaoffenburg.roboviz.configuration.Config;
import rv.comm.rcssserver.ServerComm.ServerChangeListener;
import rv.ui.screens.FoulListOverlay;
import rv.world.WorldModel;

/**
 * Contains soccer game state information collected from rcssserver: teams, scores, play mode, time,
 * field dimensions, etc.
 *
 * @author Justin Stoecker
 */
@SuppressWarnings("unused")
public class GameState implements ServerChangeListener
{
	public interface GameStateChangeListener
	{
		/** Called when measurements (field dimensions, etc.) or rules change */
		void gsMeasuresAndRulesChanged(GameState gs);

		/** Called when team names, scores, or play mode change */
		void gsPlayStateChanged(GameState gs);

		/** Called when the time or half changes */
		void gsTimeChanged(GameState gs);
	}

	public interface ServerMessageReceivedListener
	{
		/** Called when a valid message from the server is received */
		void gsServerMessageReceived(GameState gs);

		/** Called after a message from the server has been processed (parsed) */
		void gsServerMessageProcessed(GameState gs);
	}

	public enum FoulType
	{
		CROWDING(0, "crowding"),
		TOUCHING(1, "touching"),
		ILLEGAL_DEFENCE(2, "illegal defence"),
		ILLEGAL_ATTACK(3, "illegal attack"),
		INCAPABLE(4, "incapable"),
		KICKOFF(5, "illegal kickoff"),
		CHARGING(6, "charging"),
		SELF_COLLISION(7, "self collision"),
		BALL_HOLDING(8, "ball holding");

		private int index;
		private String name;

		FoulType(int index, String name)
		{
			this.index = index;
			this.name = name;
		}

		public String toString()
		{
			return name;
		}
	}

	public static class Foul
	{
		public float time;
		public int index;
		public FoulType type;
		public int team;
		public int agentID;
		public long receivedTime;
	}

	public static class HistoryItem
	{
		public final float time;
		public final String playMode;

		public HistoryItem(float time, String playMode)
		{
			this.time = time;
			this.playMode = playMode;
		}
	}

	// Measurements and Rules
	public static final String FIELD_LENGTH = "FieldLength";
	public static final String FIELD_WIDTH = "FieldWidth";
	public static final String FIELD_HEIGHT = "FieldHeight";
	public static final String GOAL_WIDTH = "GoalWidth";
	public static final String GOAL_DEPTH = "GoalDepth";
	public static final String GOAL_HEIGHT = "GoalHeight";
	public static final String FREE_KICK_DST = "FreeKickDistance";
	public static final String WAIT_BEFORE_KO = "WaitBeforeKickOff";
	public static final String AGENT_RADIUS = "AgentRadius";
	public static final String BALL_RADIUS = "BallRadius";
	public static final String BALL_MASS = "BallMass";
	public static final String RULE_GOAL_PAUSE_TIME = "RuleGoalPauseTime";
	public static final String RULE_KICK_PAUSE_TIME = "RuleKickInPauseTime";
	public static final String RULE_HALF_TIME = "RuleHalfTime";
	public static final String PASS_MODE_MIN_OPP_BALL_DIST = "PassModeMinOppBallDist";
	public static final String PASS_MODE_DURATION = "PassModeDuration";

	// Play State
	public static final String PLAY_MODES = "play_modes";
	public static final String TEAM_LEFT = "team_left";
	public static final String TEAM_RIGHT = "team_right";
	public static final String SCORE_LEFT = "score_left";
	public static final String SCORE_RIGHT = "score_right";
	public static final String PLAY_MODE = "play_mode";
	public static final String PASS_MODE_SCORE_WAIT_LEFT = "pass_mode_score_wait_left";
	public static final String PASS_MODE_SCORE_WAIT_RIGHT = "pass_mode_score_wait_right";

	// Play Modes
	public static final String BEFORE_KICK_OFF = "BeforeKickOff";
	public static final String KICK_OFF_LEFT = "KickOff_Left";
	public static final String KICK_OFF_RIGHT = "KickOff_Right";
	public static final String PLAY_ON = "PlayOn";
	public static final String KICK_IN_LEFT = "KickIn_Left";
	public static final String KICK_IN_RIGHT = "KickIn_Right";
	public static final String CORNER_KICK_LEFT = "corner_kick_left";
	public static final String CORNER_KICK_RIGHT = "corner_kick_right";
	public static final String GOAL_KICK_LEFT = "goal_kick_left";
	public static final String GOAL_KICK_RIGHT = "goal_kick_right";
	public static final String OFFSIDE_LEFT = "offside_left";
	public static final String OFFSIDE_RIGHT = "offside_right";
	public static final String GAME_OVER = "GameOver";
	public static final String GOAL_LEFT = "Goal_Left";
	public static final String GOAL_RIGHT = "Goal_Right";
	public static final String FREE_KICK_LEFT = "free_kick_left";
	public static final String FREE_KICK_RIGHT = "free_kick_right";
	public static final String DIRECT_FREE_KICK_LEFT = "direct_free_kick_left";
	public static final String DIRECT_FREE_KICK_RIGHT = "direct_free_kick_right";
	public static final String PASS_LEFT = "pass_left";
	public static final String PASS_RIGHT = "pass_right";

	// Time
	public static final String TIME = "time";
	public static final String HALF = "half";

	// Foul
	public static final String FOUL = "foul";

	private boolean initialized;
	private float fieldLength;
	private float fieldWidth;
	private float fieldHeight;
	private float goalWidth;
	private float goalDepth;
	private float goalHeight;
	private float freeKickDist;
	private float waitBeforeKickoff;
	private float agentRadius;
	private float ballRadius;
	private float ballMass;
	private float ruleGoalPauseTime;
	private float ruleKickPauseTime;
	private float ruleHalfTime;
	private float passModeMinOppBallDist;
	private float passModeDuration;
	private boolean passModeValuesReported;
	private float passModeScoreWaitLeft;
	private float passModeScoreWaitRight;
	private String[] playModes;
	private String teamLeft;
	private String teamRight;
	private int scoreLeft;
	private int scoreRight;
	private String playMode = "<Play Mode>";
	private boolean playModeJustChanged;
	private List<HistoryItem> playModeHistory = new CopyOnWriteArrayList<>();
	private float time;
	private int half;
	private List<Foul> fouls = new CopyOnWriteArrayList<>();

	private final List<GameStateChangeListener> listeners = new CopyOnWriteArrayList<>();

	private final List<ServerMessageReceivedListener> smListeners = new CopyOnWriteArrayList<>();

	public boolean isInitialized()
	{
		return initialized;
	}

	public float getFieldLength()
	{
		return fieldLength;
	}

	public float getFieldWidth()
	{
		return fieldWidth;
	}

	public float getFieldHeight()
	{
		return fieldHeight;
	}

	public float getGoalWidth()
	{
		return goalWidth;
	}

	public float getGoalDepth()
	{
		return goalDepth;
	}

	public float getGoalHeight()
	{
		return goalHeight;
	}

	public float getFreeKickDistance()
	{
		return freeKickDist;
	}

	public float getWaitBeforeKickOff()
	{
		return waitBeforeKickoff;
	}

	public float getAgentRadius()
	{
		return agentRadius;
	}

	public float getBallRadius()
	{
		return ballRadius;
	}

	public float getBallMass()
	{
		return ballMass;
	}

	public float getGoalPauseTime()
	{
		return ruleGoalPauseTime;
	}

	public float getKickPauseTime()
	{
		return ruleKickPauseTime;
	}

	public float getHalfTime()
	{
		return ruleHalfTime;
	}

	public float getPassModeMinOppBallDist()
	{
		return passModeMinOppBallDist;
	}

	public float getPassModeDuration()
	{
		return passModeDuration;
	}

	public boolean getPassModeValuesReported()
	{
		return passModeValuesReported;
	}

	public String[] getPlayModes()
	{
		return playModes;
	}

	public String getTeamLeft()
	{
		return teamLeft;
	}

	public String getUIStringTeamLeft()
	{
		if (teamLeft == null)
			return Config.defaultLeftTeamName;
		return teamLeft;
	}

	public String getTeamRight()
	{
		return teamRight;
	}

	public String getUIStringTeamRight()
	{
		if (teamRight == null)
			return Config.defaultRightTeamName;
		return teamRight;
	}

	public int getScoreLeft()
	{
		return scoreLeft;
	}

	public int getScoreRight()
	{
		return scoreRight;
	}

	public String getPlayMode()
	{
		return playMode;
	}

	public boolean hasPlayModeJustChanged()
	{
		return playModeJustChanged;
	}

	public List<HistoryItem> getPlayModeHistory()
	{
		return playModeHistory;
	}

	public float getTime()
	{
		return time;
	}

	public int getHalf()
	{
		return half;
	}

	public List<Foul> getFouls()
	{
		return fouls;
	}

	public float getPassModeScoreWaitLeft()
	{
		return passModeScoreWaitLeft;
	}

	public float getPassModeScoreWaitRight()
	{
		return passModeScoreWaitRight;
	}

	public void addListener(GameStateChangeListener l)
	{
		listeners.add(l);
	}

	public void removeListener(GameStateChangeListener l)
	{
		listeners.remove(l);
	}

	public void addListener(ServerMessageReceivedListener l)
	{
		smListeners.add(l);
	}

	public void removeListener(ServerMessageReceivedListener l)
	{
		smListeners.remove(l);
	}

	public void reset()
	{
		initialized = false;
		teamLeft = null;
		teamRight = null;
		scoreLeft = 0;
		scoreRight = 0;
		playMode = null;
		playModeJustChanged = false;
		playModeHistory = new ArrayList<>();
		time = 0;
		half = 0;
		fouls = new CopyOnWriteArrayList<>();

		// For support of rcssserver3d versions <= 0.7.2
		passModeMinOppBallDist = 1;
		passModeDuration = 4;
		passModeValuesReported = false;
	}

	private boolean isTimeStopped()
	{
		return BEFORE_KICK_OFF.equals(playMode) || GAME_OVER.equals(playMode);
	}

	private void removeExpiredFouls()
	{
		if (fouls.isEmpty()) {
			return;
		}

		// Remove fouls that are no longer to be displayed and out of date
		// so that they don't block other fouls from being added later.
		// This can be a bit tricky if we're moving backwards/forwards in
		// time in a log.
		ArrayList<Foul> foulsToRemove = new ArrayList<>();
		long currentTimeMillis = System.currentTimeMillis();
		for (Foul foul : fouls) {
			if (!FoulListOverlay.shouldDisplayFoul(foul, currentTimeMillis)) {
				if (Math.abs(time - foul.time) >= 1 || isTimeStopped()) {
					foulsToRemove.add(foul);
				}
			}
		}
		fouls.removeAll(foulsToRemove);
	}

	private void addFoul(Foul foul)
	{
		boolean alreadyHaveFoul = false;
		for (Foul f : fouls) {
			if (f.type == foul.type && f.team == foul.team && f.agentID == foul.agentID &&
					Math.abs(foul.time - f.time) < 1.0) {
				// We already have this foul so don't add it again
				alreadyHaveFoul = true;
				break;
			}
		}
		if (!alreadyHaveFoul) {
			fouls.add(foul);
		}
	}

	/**
	 * Parses expression and updates state
	 */
	public void parse(SExp exp, WorldModel world)
	{
		if (exp.getChildren() == null)
			return;

		for (ServerMessageReceivedListener l : smListeners) {
			l.gsServerMessageReceived(this);
		}

		int measureOrRuleChanges = 0;
		int timeChanges = 0;
		int playStateChanges = 0;
		String previousPlayMode = playMode;

		removeExpiredFouls();
		passModeScoreWaitLeft = 0;
		passModeScoreWaitRight = 0;

		for (SExp se : exp.getChildren()) {
			String[] atoms = se.getAtoms();

			if (atoms != null) {
				String atomName = atoms[0];

				switch (atomName) {
				case FIELD_LENGTH:
					fieldLength = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case FIELD_WIDTH:
					fieldWidth = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case FIELD_HEIGHT:
					fieldHeight = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case GOAL_WIDTH:
					goalWidth = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case GOAL_DEPTH:
					goalDepth = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case GOAL_HEIGHT:
					goalHeight = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case FREE_KICK_DST:
					freeKickDist = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case WAIT_BEFORE_KO:
					waitBeforeKickoff = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case AGENT_RADIUS:
					agentRadius = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case BALL_RADIUS:
					ballRadius = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case BALL_MASS:
					ballMass = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case RULE_GOAL_PAUSE_TIME:
					ruleGoalPauseTime = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case RULE_KICK_PAUSE_TIME:
					ruleKickPauseTime = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case RULE_HALF_TIME:
					ruleHalfTime = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case PASS_MODE_MIN_OPP_BALL_DIST:
					passModeValuesReported = true;
					passModeMinOppBallDist = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case PASS_MODE_DURATION:
					passModeValuesReported = true;
					passModeDuration = Float.parseFloat(atoms[1]);
					measureOrRuleChanges++;
					break;
				case PLAY_MODES:
					playModes = new String[atoms.length - 1];
					System.arraycopy(atoms, 1, playModes, 0, playModes.length);
					playStateChanges++;
					break;
				case TIME:
					time = Float.parseFloat(atoms[1]);
					timeChanges++;
					break;
				case HALF:
					half = Integer.parseInt(atoms[1]);
					timeChanges++;
					break;
				case PLAY_MODE:
					int mode = Integer.parseInt(atoms[1]);
					playMode = playModes[mode];
					playStateChanges++;
					break;
				case TEAM_LEFT:
					teamLeft = atoms[1];
					playStateChanges++;
					break;
				case TEAM_RIGHT:
					teamRight = atoms[1];
					playStateChanges++;
					break;
				case SCORE_LEFT:
					scoreLeft = Integer.parseInt(atoms[1]);
					playStateChanges++;
					break;
				case SCORE_RIGHT:
					scoreRight = Integer.parseInt(atoms[1]);
					playStateChanges++;
					break;
				case FOUL:
					Foul foul = new Foul();
					foul.time = time;
					foul.index = Integer.parseInt(atoms[1]);
					foul.type = GameState.FoulType.values()[Integer.parseInt(atoms[2])];
					foul.team = Integer.parseInt(atoms[3]);
					foul.agentID = Integer.parseInt(atoms[4]);
					foul.receivedTime = System.currentTimeMillis();
					addFoul(foul);
					break;
				case PASS_MODE_SCORE_WAIT_LEFT:
					passModeValuesReported = true;
					passModeScoreWaitLeft = Float.parseFloat(atoms[1]);
					timeChanges++;
					break;
				case PASS_MODE_SCORE_WAIT_RIGHT:
					passModeValuesReported = true;
					passModeScoreWaitRight = Float.parseFloat(atoms[1]);
					timeChanges++;
					break;
				}
			}
		}

		playModeJustChanged = previousPlayMode == null || !previousPlayMode.equals(playMode);
		if (playModeJustChanged) {
			playModeHistory.add(new HistoryItem(time, playMode));
			while (playModeHistory.size() > 2) {
				playModeHistory.remove(0);
			}
		}

		initialized = true;

		for (ServerMessageReceivedListener l : smListeners)
			l.gsServerMessageProcessed(this);

		int changes = playStateChanges + timeChanges + measureOrRuleChanges;
		if (changes > 0) {
			for (GameStateChangeListener l : listeners) {
				if (playStateChanges > 0)
					l.gsPlayStateChanged(this);
				if (timeChanges > 0)
					l.gsTimeChanged(this);
				if (measureOrRuleChanges > 0)
					l.gsMeasuresAndRulesChanged(this);
			}
		}
	}

	@Override
	public void connectionChanged(ServerComm server)
	{
		if (server.isConnected()) {
			server.getWorldModel().reset();
		}
	}
}
