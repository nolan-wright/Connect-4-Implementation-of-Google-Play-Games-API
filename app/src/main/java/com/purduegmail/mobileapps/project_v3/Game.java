package com.purduegmail.mobileapps.project_v3;

import java.util.ArrayList;

/**
 * Created by Nolan Wright on 11/12/2017.
 */

public class Game {
    /**
     * Row and column sizes
     */
    private static final int ROWS = 6;
    private static final int COLUMNS = 7;
    /**
     * MY_MARKER is the chip placed for my moves
     * OPPONENT_MARKER is the chip placed for the opponent
     * NULL_MARKER means nothing has been placed there
     */
    public static final int MY_MARKER = 1;
    public static final int OPPONENT_MARKER = 2;
    private static final int NULL_MARKER = 0;

    /**
     * an instance of this class
     * has one listener associated
     * with it
     */
    private GameListener listener;

    /**
     * contains board state
     * board is 7 columns wide
     * and 6 rows long, like so:
     * # # # # # # #
     * # # # # # # #
     * # # # # # # #
     * # # # # # # #
     * # # # # # # #
     * # # # # # # #
     */
    private int[][] board;

    /**
     * list of 4 in a row sequences
     * it is possible to have more than one
     */
    private ArrayList<int[][]> winningSequences;

    public Game(GameListener listener) {
        winningSequences = new ArrayList<>();
        this.listener = listener;
        board = generateNewBoard();
    }

    /** create new board
     * set all cells in the board to NULL_MARKER initially
     * NULL_MARKER represents an open cell in the board
     */
    private int[][] generateNewBoard() {
        int[][] newBoard = new int[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                newBoard[row][column] = NULL_MARKER;
            }
        }
        return newBoard;
    }

    public void processMyMove(int column) {
        int row = ROWS - 1;
        while (row >= 0) {
            if (board[row][column] == NULL_MARKER) {
                board[row][column] = MY_MARKER;
                // check for catscratch
                if (isCatscratch())  {
                    listener.onGameTied();
                }
                // check if win condition is met
                else if (hasWinningSequence()) {
                    listener.onGameWon(winningSequences);
                }
                // notify listener of board update
                listener.onMyMoveProcessed(row, column);
                return;
            }
            else {
                row--;
            }
        }
    }
    public void processOpponentMove(int row, int column) {
        board[row][column] = OPPONENT_MARKER;
    }

    /**
     * returns the losing sequences for this board
     */
    public ArrayList<int[][]> getLosingSequences() {
        ArrayList<int[][]> losingSequences = new ArrayList<>();
        if (getLosingColumns() != null) {
            losingSequences.add(getLosingColumns());
        }
        if (getLosingRows() != null) {
            losingSequences.add(getLosingRows());
        }
        losingSequences.addAll(getLosingDiagonals());
        return losingSequences;
    }
    private int[][] getLosingColumns() {
        int winning_threshold = 2;
        ArrayList<int[]> sequence = new ArrayList<>();
        // starts at bottom-left, works up and across
        for (int column = 0; column < COLUMNS; column++) {
            int row = ROWS - 1;
            while (row > winning_threshold) {
                // impossible to lose if starting at 2nd row or higher
                while (row >= 0 && board[row][column] == OPPONENT_MARKER) {
                    int[] coordinate = {row, column};
                    sequence.add(coordinate);
                    row--;
                }
                if (sequence.size() >= 4) {
                    // losing sequence
                    // only one column can have a losing sequence
                    int[][] sequenceArray = new int[sequence.size()][2];
                    sequenceArray = sequence.toArray(sequenceArray);
                    return sequenceArray;

                }
                else /* not a losing sequence */ {
                    sequence.clear();
                }
                if (board[row][column] == NULL_MARKER) {
                    // stop checking this column
                    break;
                }
                else {
                    row--;
                }
            }
        }
        return null;
    }
    private int[][] getLosingRows() {
        int winning_threshold = 4;
        ArrayList<int[]> sequence = new ArrayList<>();
        // starts at bottom-left, works across and up
        for (int row = ROWS - 1; row >= 0; row--) {
            int column = 0;
            while (column < winning_threshold) {
                // impossible to lose if starting at or past 4th column
                while (column < COLUMNS && board[row][column] == OPPONENT_MARKER) {
                    int[] coordinate = {row, column};
                    sequence.add(coordinate);
                    column++;
                }
                if (sequence.size() >= 4) {
                    // losing sequence
                    // only one row per turn can have a losing sequence
                    int[][] sequenceArray = new int[sequence.size()][2];
                    sequenceArray = sequence.toArray(sequenceArray);
                    return sequenceArray;
                }
                else /* not a winning sequence */ {
                    sequence.clear();
                }
                column++;
            }
        }
        return null;
    }
    private ArrayList<int[][]> getLosingDiagonals() {
        ArrayList<int[][]> losingDiagonals = new ArrayList<>();
        if (getLosingForwardDiagonals() != null) {
            losingDiagonals.add(getLosingForwardDiagonals());
        }
        if (getLosingBackwardDiagonals() != null) {
            losingDiagonals.add(getLosingBackwardDiagonals());
        }
        return losingDiagonals;
    }
    private int[][] getLosingForwardDiagonals() {
        int limit = 3; // bound of losable area
        ArrayList<int[]> sequence = new ArrayList<>();
        for (int row = ROWS - 1; row >= 3; row--) {
            for (int column = 0; column <= limit; column++) {
                // start in bottom-left
                int margin = 0;
                while ((row - margin) >= 0 && (column + margin) < COLUMNS) {
                    if (board[row - margin][column + margin] == OPPONENT_MARKER) {
                        int[] coordinate = {row - margin, column + margin};
                        sequence.add(coordinate);
                        // move forward diagonally
                        margin++;
                    }
                    else {
                        if (sequence.size() >= 4) {
                            // losing sequence
                            // only one forward diagonal per turn can have a losing sequence
                            int[][] sequenceArray = new int[sequence.size()][2];
                            sequenceArray = sequence.toArray(sequenceArray);
                            return sequenceArray;
                        }
                        else /* not a losing sequence */ {
                            sequence.clear();
                            // move forward diagonally
                            margin++;
                            // stop if position + margin is outside losable area
                            if ((row - margin) < 3 || (column + margin) > limit) {
                                break;
                            }
                        }
                    }
                }
            }
            // so that diagonals are not rechecked
            limit = 0;
        }
        return null;
    }
    private int[][] getLosingBackwardDiagonals() {
        int limit = 3; // bound of losable area
        ArrayList<int[]> sequence = new ArrayList<>();
        for (int row = ROWS - 1; row >= 3; row--) {
            for (int column = COLUMNS - 1; column >= limit; column--) {
                // start in bottom-right
                int margin = 0;
                while ((row - margin) >= 0 && (column - margin) >= 0) {
                    if (board[row - margin][column - margin] == OPPONENT_MARKER) {
                        int[] coordinate = {row - margin, column - margin};
                        sequence.add(coordinate);
                        // move backward diagonally
                        margin++;
                    }
                    else {
                        if (sequence.size() >= 4) {
                            // losing sequence
                            // only one backward diagonal per turn can have a losing sequence
                            int[][] sequenceArray = new int[sequence.size()][2];
                            sequenceArray = sequence.toArray(sequenceArray);
                            return sequenceArray;
                        }
                        else /* not a losing sequence */ {
                            sequence.clear();
                            // move backward diagonally
                            margin++;
                            // stop if position + margin is outside losable area
                            if ((row - margin) < 3 || (column - margin) < limit) {
                                break;
                            }
                        }
                    }
                }
            }
            // so that diagonals are not rechecked
            limit = 6;
        }
        return null;
    }

    private boolean hasWinningSequence() {
        return checkColumns() || checkRows() || checkDiagonals();
    }
    /**
     * indices
     * failing column
     * failing column
     * passing column
     * 0 0 0 0
     * 1 0 1 1
     * 2 1 2 1
     * 3 1 1 1
     * 4 1 1 1
     * 5 2 1 2
     */
    private boolean checkColumns() {
        int winning_threshold = 2;
        ArrayList<int[]> sequence = new ArrayList<>();
        // starts at bottom-left, works up and across
        for (int column = 0; column < COLUMNS; column++) {
            int row = ROWS - 1;
            while (row > winning_threshold) {
                // impossible to win if starting at 2nd row or higher
                while (row >= 0 && board[row][column] == MY_MARKER) {
                    int[] coordinate = {row, column};
                    sequence.add(coordinate);
                    row--;
                }
                if (sequence.size() >= 4) {
                    // winning sequence
                    // add to winning sequences
                    int[][] sequenceArray = new int[sequence.size()][2];
                    sequenceArray = sequence.toArray(sequenceArray);
                    winningSequences.add(sequenceArray);
                    // only one column per turn can have a winning sequence
                    return true;
                }
                else /* not a winning sequence */ {
                    sequence.clear();
                }
                if (board[row][column] == NULL_MARKER) {
                    // stop checking this column
                    break;
                }
                else {
                    row--;
                }
            }
        }
        return false;
    }
    /**
     * Example:
     * 0 1 2 3 4 5 6 (indices)
     * 1 1 1 2 1 1 1 (failing row)
     * 1 2 1 1 1 1 2 (passing row)
     */
    private boolean checkRows() {
        int winning_threshold = 4;
        ArrayList<int[]> sequence = new ArrayList<>();
        // starts at bottom-left, works across and up
        for (int row = ROWS - 1; row >= 0; row--) {
            int column = 0;
            while (column < winning_threshold) {
                // impossible to win if starting at or past 4th column
                while (column < COLUMNS && board[row][column] == MY_MARKER) {
                    int[] coordinate = {row, column};
                    sequence.add(coordinate);
                    column++;
                }
                if (sequence.size() >= 4) {
                    // winning sequence
                    // add to winning sequences
                    int[][] sequenceArray = new int[sequence.size()][2];
                    sequenceArray = sequence.toArray(sequenceArray);
                    winningSequences.add(sequenceArray);
                    // only one row per turn can have a winning sequence
                    return true;
                }
                else /* not a winning sequence */ {
                    sequence.clear();
                }
                column++;
            }
        }
        return false;
    }
    private boolean checkDiagonals() {
        // check bottom-left 3rows x 4cols for forward diagonals /
        // check bottom-right 3rows x 4cols for backward diagonals \
        return checkForwardDiagonals() || checkBackwardDiagonals();
    }
    /**
     * forward diagonals
     *   0 1 2 3 4 5 6
     * 0 # # # # # # #
     * 1 # # # # # # #
     * 2 # # # # # # #
     * 3 $ $ $ $ # # #
     * 4 $ $ $ $ # # #
     * 5 $ $ $ $ # # #
     */
    private boolean checkForwardDiagonals() {
        int limit = 3; // bound of winnable area
        ArrayList<int[]> sequence = new ArrayList<>();
        for (int row = ROWS - 1; row >= 3; row--) {
            for (int column = 0; column <= limit; column++) {
                // start in bottom-left
                int margin = 0;
                while ((row - margin) >= 0 && (column + margin) < COLUMNS) {
                    if (board[row - margin][column + margin] == MY_MARKER) {
                        int[] coordinate = {row - margin, column + margin};
                        sequence.add(coordinate);
                        // move forward diagonally
                        margin++;
                    }
                    else {
                        if (sequence.size() >= 4) {
                            // winning sequence
                            // add to winning sequences
                            int[][] sequenceArray = new int[sequence.size()][2];
                            sequenceArray = sequence.toArray(sequenceArray);
                            winningSequences.add(sequenceArray);
                            // only one forward diagonal per turn can have a winning sequence
                            return true;
                        }
                        else /* not a winning sequence */ {
                            sequence.clear();
                            // move forward diagonally
                            margin++;
                            // stop if position + margin is outside winnable area
                            if ((row - margin) < 3 || (column + margin) > limit) {
                                break;
                            }
                        }
                    }
                }
            }
            // so that diagonals are not rechecked
            limit = 0;
        }
        return false;
    }
    /**
     * backward diagonals
     *   0 1 2 3 4 5 6
     * 0 # # # # # # #
     * 1 # # # # # # #
     * 2 # # # # # # #
     * 3 # # # $ $ $ $
     * 4 # # # $ $ $ $
     * 5 # # # $ $ $ $
     */
    private boolean checkBackwardDiagonals() {
        int limit = 3; // bound of winnable area
        ArrayList<int[]> sequence = new ArrayList<>();
        for (int row = ROWS - 1; row >= 3; row--) {
            for (int column = COLUMNS - 1; column >= limit; column--) {
                // start in bottom-right
                int margin = 0;
                while ((row - margin) >= 0 && (column - margin) >= 0) {
                    if (board[row - margin][column - margin] == MY_MARKER) {
                        int[] coordinate = {row - margin, column - margin};
                        sequence.add(coordinate);
                        // move backward diagonally
                        margin++;
                    }
                    else {
                        if (sequence.size() >= 4) {
                            // winning sequence
                            // add to winning sequences
                            int[][] sequenceArray = new int[sequence.size()][2];
                            sequenceArray = sequence.toArray(sequenceArray);
                            winningSequences.add(sequenceArray);
                            // only one backward diagonal per turn can have a winning sequence
                            return true;
                        }
                        else /* not a winning sequence */ {
                            sequence.clear();
                            // move backward diagonally
                            margin++;
                            // stop if position + margin is outside winnable area
                            if ((row - margin) < 3 || (column - margin) < limit) {
                                break;
                            }
                        }
                    }
                }
            }
            // so that diagonals are not rechecked
            limit = 6;
        }
        return false;
    }

    /**
     * determines whether any possible ways
     * to win still exist
     */
    private boolean isCatscratch() {
        return columnsCatscratch() && rowsCatscratch() && diagonalsCatscratch();
    }
    private boolean columnsCatscratch() {
        int myCounter = 0;
        int opponentCounter = 0;
        for (int column = 0; column < COLUMNS; column++) {
            // start at leftmost column, work across
            for (int row = ROWS - 1; row >= 0; row--) {
                // start at bottom row, work up
                if (board[row][column] == NULL_MARKER) {
                    // this cell could potentially contribute to a win for me or my opponent
                    myCounter++;
                    opponentCounter++;
                }
                else if (board[row][column] == MY_MARKER) {
                    // this cell could potentially contribute to a win for me
                    myCounter++;
                    opponentCounter = 0;
                }
                else /* board[row][column] == OPPONENT_MARKER */ {
                    // this cell could potentially contribute to a win for my opponent
                    opponentCounter++;
                    myCounter = 0;
                }
                if (myCounter >= 4 || opponentCounter >= 4) {
                    // possible win sequence
                    return false;
                }
            }
            myCounter = 0;
            opponentCounter = 0;
        }
        return true;
    }
    private boolean rowsCatscratch() {
        int myCounter = 0;
        int opponentCounter = 0;
        for (int row = 0; row < ROWS; row++) {
            // start at top, work down
            for (int column = 0; column < COLUMNS; column++) {
                // start at leftmost column, work across
                if (board[row][column] == NULL_MARKER) {
                    // this cell could potentially contribute to a win for me or my opponent
                    myCounter++;
                    opponentCounter++;
                }
                else if (board[row][column] == MY_MARKER) {
                    // this cell could potentially contribute to a win for me
                    myCounter++;
                    opponentCounter = 0;
                }
                else /* board[row][column] == OPPONENT_MARKER */ {
                    // this cell could potentially contribute to a win for my opponent
                    opponentCounter++;
                    myCounter = 0;
                }
                if (myCounter >= 4 || opponentCounter >= 4) {
                    // possible win sequence
                    return false;
                }
            }
            myCounter = 0;
            opponentCounter = 0;
        }
        return true;
    }
    private boolean diagonalsCatscratch() {
        return forwardDiagonalsCatscratch() && backwardDiagonalsCatscratch();
    }
    private boolean forwardDiagonalsCatscratch() {
        int myCounter = 0;
        int opponentCounter = 0;
        int limit = 3; // bound of winnable area
        for (int row = ROWS - 1; row >= 3; row--) {
            for (int column = 0; column <= limit; column++) {
                // start in bottom-left
                int margin = 0;
                while ((row - margin) >= 0 && (column + margin) < COLUMNS) {
                    if (board[row - margin][column + margin] == NULL_MARKER) {
                        // this cell could potentially contribute to a win for me or my opponent
                        myCounter++;
                        opponentCounter++;
                    }
                    else if (board[row - margin][column + margin] == MY_MARKER) {
                        myCounter++;
                        opponentCounter = 0;
                    }
                    else /* board[row - margin][column + margin] == OPPONENT_MARKER */ {
                        opponentCounter++;
                        myCounter = 0;
                    }
                    if (myCounter >= 4 || opponentCounter >= 4) {
                        // possible win sequence exists
                        return false;
                    }
                    else {
                        // move to the next cell on the diagonal
                        margin++;
                    }
                }
                myCounter = 0;
                opponentCounter = 0;
            }
            // so that diagonals are not rechecked
            limit = 0;
        }
        return true;
    }
    private boolean backwardDiagonalsCatscratch() {
        int myCounter = 0;
        int opponentCounter = 0;
        int limit = 3; // bound of winnable area
        for (int row = ROWS - 1; row >= 3; row--) {
            for (int column = COLUMNS - 1; column >= limit; column--) {
                // start in bottom-right
                int margin = 0;
                while ((row - margin) >= 0 && (column - margin) >= 0) {
                    if (board[row - margin][column - margin] == NULL_MARKER) {
                        // this cell could potentially contribute to a win for me or my opponent
                        myCounter++;
                        opponentCounter++;
                    }
                    else if (board[row - margin][column - margin] == MY_MARKER) {
                        myCounter++;
                        opponentCounter = 0;
                    }
                    else /* board[row - margin][column - margin] == OPPONENT_MARKER */ {
                        opponentCounter++;
                        myCounter = 0;
                    }
                    if (myCounter >= 4 || opponentCounter >= 4) {
                        // possible win sequence exists
                        return false;
                    }
                    else {
                        // move to the next cell on the diagonal
                        margin++;
                    }
                }
                myCounter = 0;
                opponentCounter = 0;
            }
            // so that diagonals are not rechecked
            limit = 6;
        }
        return true;
    }

    // prints the board
    public void printBoard() {
        String columnHeader = "    0 1 2 3 4 5 6";
        String headerDivider = "-----------------";
        System.out.println(columnHeader);
        System.out.println(headerDivider);
        for (int row = 0; row < ROWS; row++) {
            String board = String.valueOf(row) + " | ";
            for (int column = 0; column < COLUMNS; column++) {
                board += String.valueOf(this.board[row][column]) + " ";
            }
            System.out.println(board);
        }
    }

    // interface specification
    public interface GameListener {
        void onMyMoveProcessed(int row, int column);
        void onGameTied();
        void onGameWon(ArrayList<int[][]> winningSequences);
    }
}