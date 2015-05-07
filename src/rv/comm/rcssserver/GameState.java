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
        public void gsMeasuresAndRulesChanged(GameState gs);

        /** Called when team names, scores, or play mode change */
        public void gsPlayStateChanged(GameState gs);

        /** Called when the time or half changes */
        public void gsTimeChanged(GameState gs);
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
    private float                               time;
    private int                                 half;

    private final List<GameStateChangeListener> listeners            = new ArrayList<>();

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

    public void addListener(GameStateChangeListener l) {
        listeners.add(l);
    }

    public void removeListener(GameStateChangeListener l) {
        listeners.remove(l);
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
    public void connectionChanged(ServerComm server) {
        if (!server.isConnected()) {
            scoreLeft = 0;
            scoreRight = 0;
            teamLeft = null;
            teamRight = null;
        }
    }
}
