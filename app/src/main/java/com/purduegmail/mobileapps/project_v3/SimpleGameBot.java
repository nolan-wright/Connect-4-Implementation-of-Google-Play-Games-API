package com.purduegmail.mobileapps.project_v3;

/**
 * Created by Nolan Wright on 12/2/2017.
 */

public class SimpleGameBot extends GameBot {

    SimpleGameBot(int depth) {
        this.depth = depth;
    }

    public int evaluate(final int[][] state) {
        int botWinnableSequences = getNumberOfWinnableSequences(state, BOT_MARKER);
        int botCellEvaluation = evaluateCells(state, BOT_MARKER);
        int opponentWinnableSequences = getNumberOfWinnableSequences(state, OPPONENT_MARKER);
        int opponentCellEvaluation = evaluateCells(state, OPPONENT_MARKER);
        int evaluation = (botWinnableSequences - opponentWinnableSequences) * 100;
        evaluation += botCellEvaluation - opponentCellEvaluation;
        return evaluation;
    }

    /*
     * helper functions
     */
    // called in evaluate
    private int evaluateCells(int[][] state, int marker) {
        int points = 0;
        for (int row = 0; row < Game.ROWS; row++) {
            for (int column = 0; column < Game.COLUMNS; column++) {
                if (state[row][column] == marker) {
                    int[][] neighbors = {
                            {row - 1, column - 1}, // above and to the left
                            {row, column - 1}, // to the left
                            {row + 1, column - 1}, // below and to the left
                            {row - 1, column + 1}, // above and to the right
                            {row, column + 1}, // to the right
                            {row + 1, column + 1} // below and to the right
                    };
                    for (int[] neighbor : neighbors) {
                        int neighborRow = neighbor[0];
                        int neighborCol = neighbor[1];
                        if ((neighborRow >= 0 && neighborRow < Game.ROWS) &&
                                (neighborCol >= 0 && neighborCol < Game.COLUMNS)) {
                            if (state[neighborRow][neighborCol] == Game.NULL_MARKER ||
                                    state[neighborRow][neighborCol] == marker) {
                                points++;
                            }
                        }
                    }
                }
            }
        }
        return points;
    }
    private int getNumberOfWinnableSequences(int[][] state, int marker) {
        int count = 0;
        count += getNumberOfWinnableColumns(state, marker);
        count += getNumberOfWinnableRows(state, marker);
        count += getNumberOfWinnableDiagonals(state, marker);
        return count;
    }
    // called in getNumberOfWinnableSequences
    private int getNumberOfWinnableColumns(int[][] state, int marker) {
        int winnableColumns = 0;
        for (int column = 0; column < Game.COLUMNS; column++) {
            int count = 0;
            for (int row = Game.ROWS - 1; row >= 0; row--) {
                if (state[row][column] == marker || state[row][column] == Game.NULL_MARKER) {
                    count++;
                }
                else {
                    count = 0;
                }
            }
            if (count >= 4) {
                winnableColumns++;
            }
        }
        return winnableColumns;
    }
    private int getNumberOfWinnableRows(int[][] state, int marker) {
        int winnableRows = 0;
        for (int row = 0; row < Game.ROWS; row++) {
            int count = 0;
            for (int column = 0; column < Game.COLUMNS; column++) {
                if (state[row][column] == Game.NULL_MARKER || state[row][column] == marker) {
                    count++;
                }
                else {
                    if (count >= 4) {
                        break;
                    }
                    else {
                        count = 0;
                    }
                }
            }
            if (count >= 4) {
                winnableRows++;
            }
        }
        return winnableRows;
    }
    private int getNumberOfWinnableDiagonals(int[][] state, int marker) {
        int count = 0;
        count += getNumberOfWinnableForwardDiagonals(state, marker);
        count += getNumberOfWinnableBackwardDiagonals(state, marker);
        return count;
    }
    // called in getNumberOfWinnableDiagonals
    private int getNumberOfWinnableForwardDiagonals(int[][] state, int marker) {
        int winningForwardDiagonals = 0;
        int limit = 3; // bound of winnable area
        for (int row = Game.ROWS - 1; row >= 3; row--) {
            for (int column = 0; column <= limit; column++) {
                // start in bottom-left
                int count = 0;
                int margin = 0;
                while ((row - margin) >= 0 && (column + margin) < Game.COLUMNS) {
                    if (state[row - margin][column + margin] == marker ||
                            state[row - margin][column + margin] == Game.NULL_MARKER) {
                        count++;
                        // move forward diagonally
                        margin++;
                    }
                    else {
                        if (count >= 4) {
                            // winning sequence
                            break;
                        }
                        else /* not a winning sequence */ {
                            count = 0;
                            // move forward diagonally
                            margin++;
                            // stop if position + margin is outside winnable area
                            if ((row - margin) < 3 || (column + margin) > limit) {
                                break;
                            }
                        }
                    }
                }
                // before shifting right, check count
                if (count >= 4) {
                    // winning sequence
                    winningForwardDiagonals++;
                }
            }
            // so that diagonals are not rechecked
            limit = 0;
        }
        return winningForwardDiagonals;
    }
    private int getNumberOfWinnableBackwardDiagonals(int[][] state, int marker) {
        int winnableBackwardDiagonals = 0;
        int limit = 3; // bound of winnable area
        for (int row = Game.ROWS - 1; row >= 3; row--) {
            for (int column = Game.COLUMNS - 1; column >= limit; column--) {
                // start in bottom-right
                int count = 0;
                int margin = 0;
                while ((row - margin) >= 0 && (column - margin) >= 0) {
                    if (state[row - margin][column - margin] == marker) {
                        count++;
                        // move backward diagonally
                        margin++;
                    }
                    else {
                        if (count >= 4) {
                            // winning sequence
                            break;
                        }
                        else /* not a winning sequence */ {
                            count = 0;
                            // move backward diagonally
                            margin++;
                            // stop if position + margin is outside winnable area
                            if ((row - margin) < 3 || (column - margin) < limit) {
                                break;
                            }
                        }
                    }
                }
                // before shifting left, check count
                if (count >= 4) {
                    // winning sequence
                    winnableBackwardDiagonals++;
                }
            }
            // so that diagonals are not rechecked
            limit = 6;
        }
        return winnableBackwardDiagonals;
    }

}