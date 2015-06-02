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

import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import rv.comm.rcssserver.ServerComm.ServerChangeListener;
import rv.world.WorldModel;

/**
 * Contains soccer game state information collected from rcssserver: teams, scores, play mode, time,
 * field dimensions, etc.
 * 
 * @author Justin Stoecker
 */
public class GameState implements ServerChangeListener {

    public interface GameStateChangeListener {
        /** Called when measurements (field dimensions, etc.) or rules change */
        void gsMeasuresAndRulesChanged(GameState gs);

        /** Called when team names, scores, or play mode change */
        void gsPlayStateChanged(GameState gs);

        /** Called when the time or half changes */
        void gsTimeChanged(GameState gs);
    }

    // Measurements and Rules
    public static final String                  FIELD_LENGTH         = "FieldLength";
    public static final String                  FIELD_WIDTH          = "FieldWidth";
    public static final String                  FIELD_HEIGHT         = "FieldHeight";
    public static final String                  GOAL_WIDTH           = "GoalWidth";
    public static final String                  GOAL_DEPTH           = "GoalDepth";
    public static final String                  GOAL_HEIGHT          = "GoalHeight";
    public static final String                  FREE_KICK_DST        = "FreeKickDistance";
    public static final String                  WAIT_BEFORE_KO       = "WaitBeforeKickOff";
    public static final String                  AGENT_RADIUS         = "AgentRadius";
    public static final String                  BALL_RADIUS          = "BallRadius";
    public static final String                  BALL_MASS            = "BallMass";
    public static final String                  RULE_GOAL_PAUSE_TIME = "RuleGoalPauseTime";
    public static final String                  RULE_KICK_PAUSE_TIME = "RuleKickInPauseTime";
    public static final String                  RULE_HALF_TIME       = "RuleHalfTime";

    // Play State
    public static final String                  PLAY_MODES           = "play_modes";
    public static final String                  TEAM_LEFT            = "team_left";
    public static final String                  TEAM_RIGHT           = "team_right";
    public static final String                  SCORE_LEFT           = "score_left";
    public static final String                  SCORE_RIGHT          = "score_right";
    public static final String                  PLAY_MODE            = "play_mode";

    // Time
    public static final String                  TIME                 = "time";
    public static final String                  HALF                 = "half";

    private float                               fieldLength;
    private float                               fieldWidth;
    private float                               fieldHeight;
    private float                               goalWidth;
    private float                               goalDepth;
    private float                               goalHeight;
    private float                               freeKickDist;
    private float                               waitBeforeKickoff;
    private float                               agentRadius;
    private float                               ballRadius;
    private float                               ballMass;
    private float                               ruleGoalPauseTime;
    private float                               ruleKickPauseTime;
    private float                               ruleHalfTime;
    private String[]                            playModes;
    private String                              teamLeft;
    private String                              teamRight;
    private int                                 scoreLeft;
    private int                                 scoreRight;
    private String                              playMode             = "<Play Mode>";
    private float                               time                 = 0;
    private int                                 half;

    private float                               serverSpeed          = -1;
    private TreeMap<Long, Float>                serverMsgDeltas      = new TreeMap<Long, Float>();
    private float                               accumulatedServerTime_S;

    private final List<GameStateChangeListener> listeners            = new CopyOnWriteArrayList<>();

    public float getFieldLength() {
        return fieldLength;
    }

    public float getFieldWidth() {
        return fieldWidth;
    }

    public float getFieldHeight() {
        return fieldHeight;
    }

    public float getGoalWidth() {
        return goalWidth;
    }

    public float getGoalDepth() {
        return goalDepth;
    }

    public float getGoalHeight() {
        return goalHeight;
    }

    public float getFreeKickDistance() {
        return freeKickDist;
    }

    public float getWaitBeforeKickOff() {
        return waitBeforeKickoff;
    }

    public float getAgentRadius() {
        return agentRadius;
    }

    public float getBallRadius() {
        return ballRadius;
    }

    public float getBallMass() {
        return ballMass;
    }

    public float getGoalPauseTime() {
        return ruleGoalPauseTime;
    }

    public float getKickPauseTime() {
        return ruleKickPauseTime;
    }

    public float getHalfTime() {
        return ruleHalfTime;
    }

    public String[] getPlayModes() {
        return playModes;
    }

    public String getTeamLeft() {
        return teamLeft;
    }

    public String getTeamRight() {
        return teamRight;
    }

    public int getScoreLeft() {
        return scoreLeft;
    }

    public int getScoreRight() {
        return scoreRight;
    }

    public String getPlayMode() {
        return playMode;
    }

    public float getTime() {
        return time;
    }

    public int getHalf() {
        return half;
    }

    public String getServerSpeed() {
        if (serverSpeed < 0) {
            return "---";
        }

        return Integer.toString(Math.round(100 * serverSpeed)) + "%";
    }

    public void addListener(GameStateChangeListener l) {
        listeners.add(l);
    }

    public void removeListener(GameStateChangeListener l) {
        listeners.remove(l);
    }

    public void reset() {
        teamLeft = null;
        teamRight = null;
        scoreLeft = 0;
        scoreRight = 0;
        playMode = "<Play Mode>";
        time = 0;
        half = 0;
        serverSpeed = -1;
        serverMsgDeltas.clear();
    }

    /**
     * Parses expression and updates state
     */
    public void parse(SExp exp, WorldModel world) {
        if (exp.getChildren() == null)
            return;

        int measureOrRuleChanges = 0;
        int timeChanges = 0;
        int playStateChanges = 0;

        long msgTime = System.currentTimeMillis();
        // long msgTime = System.nanoTime();
        float lastGameTime = time;

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
                }
            }
        }

        updateServerSpeed(msgTime, lastGameTime);

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

    private void updateServerSpeed(long msgTime, float lastGameTime) {
        final long TIME_WINDOW_MS = 5000;
        final float DEFAULT_MSG_DELTA_S = 0.04f;

        // Add message time info to map
        if (serverMsgDeltas.isEmpty()) {
            serverMsgDeltas.put(msgTime, -1.0f);
            accumulatedServerTime_S = 0;
        } else {
            serverMsgDeltas.lastEntry();
            long lastMsgTime = serverMsgDeltas.lastEntry().getKey();
            float lastMsgTimeDelta_S = (msgTime - lastMsgTime) / 1000.0f;
            // float lastMsgTimeDelta_S = (msgTime - lastMsgTime)/1000000000.0f;

            float serverTimeDelta_S;
            if (time - lastGameTime > 0) {
                // We have a game time change for the amount of time passed
                serverTimeDelta_S = time - lastGameTime;
            } else {
                // The game is paused so use DEFAULT_MSG_DELTA_S for amount of time passed
                serverTimeDelta_S = DEFAULT_MSG_DELTA_S;
            }

            if (msgTime - lastMsgTime > 0) {
                serverMsgDeltas.put(msgTime, (serverTimeDelta_S + accumulatedServerTime_S)
                        / lastMsgTimeDelta_S);
                accumulatedServerTime_S = 0;
            } else {
                // Messages are coming in so fast that they have the same time stamp so just save
                // the time delta to
                // add to the next entry with a new time stamp
                accumulatedServerTime_S += serverTimeDelta_S;
            }
        }

        // Remove map entries outside of time window
        SortedMap<Long, Float> oldEntries = serverMsgDeltas.headMap(msgTime - TIME_WINDOW_MS);
        while (!oldEntries.isEmpty()) {
            serverMsgDeltas.remove(oldEntries.firstKey());
        }

        float sumDeltas = 0;
        int numEntries = 0;

        Float[] deltas = serverMsgDeltas.values().toArray(new Float[0]);
        for (int i = 0; i < deltas.length; i++) {
            float delta = deltas[i].floatValue();
            if (delta > 0) {
                sumDeltas += delta;
                numEntries++;
            }
        }

        if (numEntries == 0) {
            serverSpeed = -1;
        } else {
            serverSpeed = sumDeltas / numEntries;
        }
    }

    @Override
    public void connectionChanged(ServerComm server) {
        if (server.isConnected()) {
            scoreLeft = 0;
            scoreRight = 0;
            teamLeft = null;
            teamRight = null;
            serverSpeed = -1;
            serverMsgDeltas.clear();
        }
    }
}
