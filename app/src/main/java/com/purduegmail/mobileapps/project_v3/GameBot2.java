package com.purduegmail.mobileapps.project_v3;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Nolan Wright on 11/20/2017.
 */

class GameBot2 {

    // marker constants
    // TODO: make below private after testing
    public final static int HUMAN_MARKER = 1;
    private final static int COMPUTER_MARKER = 2;
    // cell field label constants
    private final static int CELL_ROW = 0;
    private final static int CELL_COLUMN = 1;
    private final static int CELL_VALUE = 2;
    // evaluation constants
    private static final int EVALUATION_WON = Integer.MAX_VALUE;
    private static final int EVALUATION_WIN_IN_ONE = Integer.MAX_VALUE - 1;
    private static final int EVALUATION_WIN_IN_TWO = Integer.MAX_VALUE - 2;
    // level constants
    private static final int LEVEL_COMPUTER_MAXIMIZES = 0;
    private static final int LEVEL_HUMAN_MAXIMIZES = 1;

    // creates 'depth' levels under root
    private int depth; // must be an even integer >= 1

    // constructor
    GameBot2(int difficulty) throws BadDepthException {
        // difficulty == depth of tree
        if (difficulty <= 0 || (difficulty % 2 != 0 && difficulty != 1)) {
            throw new BadDepthException();
        }
        depth = difficulty;
    }

    /*
     * returns array in format: {row, column},
     * where the row, column coordinate represent
     * the computer's intended move
     */
    int[] move(int[][] state) {
        // returns column that bot has selected as its move
        Node root = buildTree(state, depth, COMPUTER_MARKER, null, null);
        //printTree(root, 0);
        Node bottomNode = minimax(root, LEVEL_COMPUTER_MAXIMIZES);
        while (bottomNode.parent.parent != null) {
            bottomNode = bottomNode.parent;
        }
        return bottomNode.delta;
    }

    private Node minimax(Node node, int level) {
        if (node.children.contains(null) || node.children.size() == 0) {
            return node;
        }
        ArrayList<Node> minimaxedChildren = new ArrayList<>();
        int next_level = (level == LEVEL_COMPUTER_MAXIMIZES) ?
                LEVEL_HUMAN_MAXIMIZES : LEVEL_COMPUTER_MAXIMIZES;
        for (Node child : node.children) {
            minimaxedChildren.add(minimax(child, next_level));
        }
        if (level == LEVEL_COMPUTER_MAXIMIZES) {
            return Collections.max(minimaxedChildren);
        }
        else /* level == LEVEL_HUMAN_MAXIMIZES */ {
            return Collections.min(minimaxedChildren);
        }
    }

    private Node buildTree(int[][] state, int depth, int marker, int[] move, Node parent) {
        if (depth < 0) {
            return null;
        }
        Node node = new Node(state);
        node.delta = move;
        node.parent = parent;
        node.whoseMove = marker;
        node.evaluation = test_evaluateBoard(state);
        if (node.evaluation[Node.COMPUTER_SCORE] == EVALUATION_WON ||
                node.evaluation[Node.HUMAN_SCORE] == EVALUATION_WON) {
            return node;
        }
        ArrayList<int[]> possibleMoves = getPossibleMoves(state);
        for (int[] moveToMake : possibleMoves) {
            int[][] possibleState = copyState(state); // get a copy of the state
            int possibleRow = moveToMake[0]; // get the row of the next move
            int possibleColumn = moveToMake[1]; // get the column of the next move
            possibleState[possibleRow][possibleColumn] = marker; // make the next move
            node.children.add(buildTree(possibleState, depth - 1,
                    getOtherMarker(marker), moveToMake, node));
        }
        return node;
    }
    /*
     * helper functions for buildTree
     */
    // TODO: make below private after testing
    public int[][] copyState(int[][] state) {
        int[][] copiedState = new int[state.length][];
        for (int i = 0; i < state.length; i++) {
            int[] row = state[i];
            copiedState[i] = new int[row.length];
            System.arraycopy(row, 0, copiedState[i], 0, row.length);
        }
        return copiedState;
    }
    private int getOtherMarker(int marker) {
        return (marker == COMPUTER_MARKER) ? HUMAN_MARKER : COMPUTER_MARKER;
    }
    private ArrayList<int[]> getPossibleMoves(int[][] state) {
        ArrayList<int[]> possibleMoves = new ArrayList<>();
        for (int column = 0; column < Game.COLUMNS; column++) {
            // iterate through each column, find first row with NULL_MARKER, if any
            for (int row = Game.ROWS - 1; row >= 0; row--) {
                if (state[row][column] == Game.NULL_MARKER) {
                    int[] move = {row, column};
                    possibleMoves.add(move);
                    break;
                }
            }
        }
        return possibleMoves;
    }

    // get all sequences of length >= 4 so that they can be evaluated
    private ArrayList<int[][]> getViableSequences(int[][] state) {
        // sequence is in format: { {row, column, value, direction}, ... }
        ArrayList<int[][]> sequences = new ArrayList<>();
        sequences.addAll(getColumns(state));
        sequences.addAll(getRows(state));
        sequences.addAll(getDiagonals(state));
        return sequences;
    }
    /*
     * helper functions for getViableSequences
     */
    private ArrayList<int[][]> getColumns(int[][] state) {
        ArrayList<int[][]> columns = new ArrayList<>();
        for (int column = 0; column < Game.COLUMNS; column++) {
            int[][] columnSequence = new int[Game.ROWS][3];
            for (int row = 0; row < Game.ROWS; row++) {
                columnSequence[row][CELL_ROW] = row;
                columnSequence[row][CELL_COLUMN] = column;
                columnSequence[row][CELL_VALUE] = state[row][column];
            }
            columns.add(columnSequence);
        }
        return columns;
    }
    private ArrayList<int[][]> getRows(int[][] state) {
        ArrayList<int[][]> rows = new ArrayList<>();
        for (int row = 0; row < Game.ROWS; row++) {
            int[][] rowSequence = new int[Game.COLUMNS][3];
            for (int column = 0; column < Game.COLUMNS; column++) {
                rowSequence[column][CELL_ROW] = row;
                rowSequence[column][CELL_COLUMN] = column;
                rowSequence[column][CELL_VALUE] = state[row][column];
            }
            rows.add(rowSequence);
        }
        return rows;
    }
    private ArrayList<int[][]> getDiagonals(int[][] state) {
        ArrayList<int[][]> diagonals = new ArrayList<>();
        diagonals.addAll(getForwardDiagonals(state));
        diagonals.addAll(getBackwardDiagonals(state));
        return diagonals;
    }
    private ArrayList<int[][]> getForwardDiagonals(int[][] state) {
        ArrayList<int[][]> forwardDiagonals = new ArrayList<>();
        // there are 6 viable forward diagonals on a 6x7 board
        final int[][] startPoints = {{5, 0}, {5, 1}, {5, 2}, {5, 3}, {4, 0}, {3, 0}};
        for (int[] startPoint : startPoints) {
            ArrayList<int[]> sequence = new ArrayList<>();
            int row = startPoint[0];
            int column = startPoint[1];
            while (row >= 0 && column < Game.COLUMNS) {
                int[] cell = {row, column, state[row][column]};
                sequence.add(cell);
                row--;
                column++;
            }
            int[][] sequenceArray = new int[sequence.size()][3];
            for (int i = 0; i < sequence.size(); i++) {
                sequenceArray[i] = sequence.get(i);
            }
            forwardDiagonals.add(sequenceArray);
        }
        return forwardDiagonals;
    }
    private ArrayList<int[][]> getBackwardDiagonals(int[][] state) {
        ArrayList<int[][]> backwardDiagonals = new ArrayList<>();
        // there are 6 viable backward diagonals on a 6x7 board
        final int[][] startPoints = {{5, 6}, {5, 5}, {5, 4}, {5, 3}, {4, 6}, {3, 6}};
        for (int[] startPoint : startPoints) {
            ArrayList<int[]> sequence = new ArrayList<>();
            int row = startPoint[0];
            int column = startPoint[1];
            while (row >= 0 && column >= 0) {
                int[] cell = {row, column, state[row][column]};
                sequence.add(cell);
                row--;
                column--;
            }
            int[][] sequenceArray = new int[sequence.size()][3];
            for (int i = 0; i < sequence.size(); i++) {
                sequenceArray[i] = sequence.get(i);
            }
            backwardDiagonals.add(sequenceArray);
        }
        return backwardDiagonals;
    }


    // TODO: testing below
    public int[] test_evaluateBoard(int[][] state) {
        // returns an array of length 2: {computer_score, human_score}
        int total_computer_score = 0;
        int total_human_score = 0;
        boolean computerScoreIsSet = false;
        boolean humanScoreIsSet = false;
        // sequence-level evaluation
        for (int[][] sequence : getViableSequences(state)) { // evaluate every sequence
            // there are 4 possible evaluations for a sequence
            // 1. the sequence contains a winning connection: evaluation -> EVALUATION_WON
            // 2. the sequence contains a win in one connection: evaluation -> EVALUATION_WIN_IN_ONE
            // 3. the sequence contains a win in two connection: evaluation -> EVALUATION_WIN_IN_TWO
            // 4. no imminent wins: evaluation -> variable score
            if (!computerScoreIsSet) {
                // we can continue checking for computer sequence scores
                int sequence_computer_score =
                        test_evaluateSequence(state, sequence, COMPUTER_MARKER);
                if (sequence_computer_score == EVALUATION_WON ||
                        sequence_computer_score == EVALUATION_WIN_IN_ONE ||
                        sequence_computer_score == EVALUATION_WIN_IN_TWO) {
                    total_computer_score = sequence_computer_score;
                    computerScoreIsSet = true;
                }
                else {
                    total_computer_score += sequence_computer_score;
                }
            }
            if (!humanScoreIsSet) {
                // we can continue checking for human sequence scores
                int sequence_human_score = test_evaluateSequence(state, sequence, HUMAN_MARKER);
                if (sequence_human_score == EVALUATION_WON ||
                        sequence_human_score == EVALUATION_WIN_IN_ONE ||
                        sequence_human_score == EVALUATION_WIN_IN_TWO) {
                    total_human_score = sequence_human_score;
                    humanScoreIsSet = true;
                }
                else {
                    total_human_score += sequence_human_score;
                }
            }
        }
        return new int[] {total_computer_score, total_human_score};
    }
    private int test_evaluateSequence(int[][] state, int[][] sequence, int marker) {
        return evaluateProcessedSequence(processSequence(state, sequence, marker));
    }
    private int[] processSequence(int[][] state, int[][] sequence, int marker) {
        ArrayList<int[]> passingCells = new ArrayList<>();
        boolean containsMarker = false;
        for (int[] cell : sequence) {
            if (cell[CELL_VALUE] == Game.NULL_MARKER) {
                passingCells.add(cell);
            }
            else if (cell[CELL_VALUE] == marker) {
                passingCells.add(cell);
                containsMarker = true;
            }
            else { // failing cell
                if (passingCells.size() >= 4 && containsMarker) {
                    int[] processedSequence = new int[passingCells.size()];
                    for (int index = 0; index < passingCells.size(); index++) {
                        // check accessibility for each cell marked with null
                        if (passingCells.get(index)[CELL_VALUE] == Game.NULL_MARKER) {
                            processedSequence[index] =
                                    getAccessibility(passingCells.get(index), state);
                        }
                        else {
                            processedSequence[index] = -1;
                        }
                    }
                    return processedSequence;
                }
                passingCells.clear();
                containsMarker = false;
            }
        }
        if (passingCells.size() >= 4 && containsMarker) {
            int[] processedSequence = new int[passingCells.size()];
            for (int index = 0; index < passingCells.size(); index++) {
                // check accessibility for each cell marked with null
                if (passingCells.get(index)[CELL_VALUE] == Game.NULL_MARKER) {
                    processedSequence[index] = getAccessibility(passingCells.get(index), state);
                }
                else {
                    processedSequence[index] = -1;
                }
            }
            return processedSequence;
        }
        return null;
    }
    private int getAccessibility(int[] cell, int[][] state) {
        final int column = cell[CELL_COLUMN];
        for (int row = state.length - 1; row > cell[CELL_ROW]; row--) {
            if (state[row][column] == 0) {
                return row - cell[CELL_ROW];
            }
        }
        return 0; // 0 represents immediate accessibility
    }
    private int evaluateProcessedSequence(int[] sequence) {
        if (sequence == null) {
            return 0;
        }
        int points = 10; // 10 points immediately for being a winnable sequence
        ArrayList<Integer> consecutiveMarkers = new ArrayList<>();
        int index = 0;
        while (index < sequence.length) {
            if (sequence[index] == -1) {
                consecutiveMarkers.add(index);
                int margin = 1;
                while (index + margin < sequence.length && sequence[index + margin] == -1) {
                    consecutiveMarkers.add(index + margin);
                    margin++;
                }
                index += margin; // so that we don't recheck
                if (consecutiveMarkers.size() >= 4) {
                    return EVALUATION_WON;
                }
                else if (consecutiveMarkers.size() == 3) {
                    int cellBefore = consecutiveMarkers.get(0) - 1;
                    int cellAfter = consecutiveMarkers.get(consecutiveMarkers.size() - 1) + 1;
                    if (cellBefore >= 0 && cellAfter < sequence.length) {
                        // is [0, x, x, x, 0]
                        if (sequence[cellBefore] == 0 && sequence[cellAfter] == 0) {
                            // both cells are accessible
                            return EVALUATION_WIN_IN_ONE;
                        }
                        // add 30 points for regular 3-marker-connection
                        points += 30;
                        // add points for the more accessible end
                        if (sequence[cellBefore] < sequence[cellAfter]) {
                            points += getPointsForAccessibility(sequence[cellBefore]);
                        }
                        else {
                            points += getPointsForAccessibility(sequence[cellAfter]);
                        }
                    }
                    else {
                        // is [0, x, x, x] or [x, x, x, 0]
                        points += 30; // 30 points for being one cell from winning
                        if (cellBefore >= 0) {
                            points += getPointsForAccessibility(sequence[cellBefore]);
                        }
                        else {
                            points += getPointsForAccessibility(sequence[cellAfter]);
                        }
                    }
                }
                else if (consecutiveMarkers.size() == 2) {
                    points += 15; // 15 points for having a 2-marker-connection that can win
                    int cellBefore = consecutiveMarkers.get(0) - 1;
                    int cellAfter = consecutiveMarkers.get(consecutiveMarkers.size() - 1) + 1;
                    if (cellBefore >= 0 && cellAfter < sequence.length) { // is [0, x, x, 0]
                        int cellTwiceBefore = cellBefore - 1;
                        int cellTwiceAfter = cellAfter + 1;
                        if (cellTwiceBefore >= 0 && sequence[cellTwiceBefore] == -1) {
                            // is [x, 0, x, x, 0]
                            points += 15; // 30 points for being one cell from winning
                            points += getPointsForAccessibility(sequence[cellBefore]);
                        }
                        else if (cellTwiceAfter < sequence.length &&
                                sequence[cellTwiceAfter] == -1) { // is [0, x, x, 0, x]
                            points += 15; // this and above condition should never both be true
                            points += getPointsForAccessibility(sequence[cellAfter]);
                        }
                        if (cellTwiceBefore >= 0 && sequence[cellTwiceBefore] != -1) {
                            // is [0, 0, x, x, 0]
                            if (sequence[cellTwiceBefore] == 0 && sequence[cellBefore] == 0 &&
                                    sequence[cellAfter] == 0) {
                                // all cells are accessible
                                return EVALUATION_WIN_IN_TWO;
                            }
                        }
                        if (cellTwiceAfter < sequence.length &&
                                sequence[cellTwiceAfter] != -1) {
                            // is [0, x, x, 0, 0]
                            if (sequence[cellTwiceAfter] == 0 && sequence[cellAfter] == 0 &&
                                    sequence[cellBefore] == 0) {
                                // all cells are accessible
                                return EVALUATION_WIN_IN_TWO;
                            }
                        }
                    }
                    else if (cellBefore >= 0) { // could be [x, 0, x, x]
                        int cellTwiceBefore = cellBefore - 1;
                        if (cellTwiceBefore >= 0 && sequence[cellTwiceBefore] == -1) {
                            points += 30; // 30 points for being one cell from winning
                            points += getPointsForAccessibility(sequence[cellBefore]);
                        }
                    }
                    else if (cellAfter < sequence.length) { // could be [x, x, 0, x]
                        int cellTwiceAfter = cellAfter + 1;
                        if (cellTwiceAfter < sequence.length && sequence[cellTwiceAfter] == -1) {
                            points += 30; // 30 points for being one cell from winning
                            points += getPointsForAccessibility(sequence[cellAfter]);
                        }
                    }
                }
                consecutiveMarkers.clear();
            }
            index++;
        }
        return points;
    }
    private int getPointsForAccessibility(int accessibility) {
        switch (accessibility) {
            case 0:
                return 6;
            case 1:
                return 5;
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 2;
            case 5:
            default:
                return 1;
        }
    }
    // TODO: testing above

    /*
     * nested classes
     */
    private class BadDepthException extends RuntimeException {
        BadDepthException() {
            super();
        }
    }
    private class Node implements Comparable<Node> {

        // score constants
        static final int COMPUTER_SCORE = 0;
        static final int HUMAN_SCORE = 1;

        @Override
        public int compareTo(@NonNull Node other) {
            // return -1 if this < other, 1 if this > other
            int this_differential = getDifferential();
            int other_differential = other.getDifferential();
            return (this_differential > other_differential) ? 1 : -1;
        }

        int[][] data; // the state of the board
        int[] delta; // the move that was made to reach the above state, format: {row, column}
        Node parent; // the parent of this node
        ArrayList<Node> children; // possible moves that can be made next
        int[] evaluation; // format: {computer_score, human_score}
        int whoseMove; // the marker that this node will use to make its move

        // constructor
        Node(int[][] data) {
            this.data = data;
            children = new ArrayList<>();
        }

        private int getDifferential() {
            // enforces hierarchy of move situations
            if (whoseMove == COMPUTER_MARKER) {
                // the computer takes the next move
                if (evaluation[COMPUTER_SCORE] == EVALUATION_WIN_IN_ONE) {
                    return EVALUATION_WIN_IN_ONE;
                }
                else if (evaluation[COMPUTER_SCORE] == EVALUATION_WIN_IN_TWO) {
                    if (evaluation[HUMAN_SCORE] == EVALUATION_WIN_IN_ONE) {
                        return -EVALUATION_WIN_IN_ONE;
                    }
                    else {
                        return EVALUATION_WIN_IN_TWO;
                    }
                }
                // computer is not in a high-priority situation
                if (evaluation[HUMAN_SCORE] == EVALUATION_WIN_IN_ONE) {
                    return -EVALUATION_WIN_IN_ONE;
                }
                else if (evaluation[HUMAN_SCORE] == EVALUATION_WIN_IN_TWO) {
                    return -EVALUATION_WIN_IN_TWO;
                }
            }
            else /* whoseMove == HUMAN_MARKER */ {
                // the human player takes the next move
                if (evaluation[HUMAN_SCORE] == EVALUATION_WIN_IN_ONE) {
                    return -EVALUATION_WIN_IN_ONE;
                }
                else if (evaluation[HUMAN_SCORE] == EVALUATION_WIN_IN_TWO) {
                    if (evaluation[COMPUTER_SCORE] == EVALUATION_WIN_IN_ONE) {
                        return EVALUATION_WIN_IN_ONE;
                    }
                    else {
                        return -EVALUATION_WIN_IN_TWO;
                    }
                }
                // human player is not in a high-priority situation
                if (evaluation[COMPUTER_SCORE] == EVALUATION_WIN_IN_ONE) {
                    return EVALUATION_WIN_IN_ONE;
                }
                else if (evaluation[COMPUTER_SCORE] == EVALUATION_WIN_IN_TWO) {
                    return EVALUATION_WIN_IN_TWO;
                }
            }
            return evaluation[COMPUTER_SCORE] - evaluation[HUMAN_SCORE];
        }

    }

    // TODO: remove below after accomplishing
    // tweak evaluation so that it favors sequences that will become accessible earlier
    // is cell evaluation necessary?
    // only give connection points for connections that can win
    // or give more points for markers in a winnable sequence

    // TODO: remove below after testing
    private void printNode(Node n) {
        System.out.println("------------------------------------------------");
        if (n.delta != null) {
            System.out.println(String.valueOf(getOtherMarker(n.whoseMove)) + " placed at:");
            System.out.println("Row: " + n.delta[0] + " Column: " + n.delta[1]);
        }
        if (n.evaluation != null) {
            System.out.println("Computer Score: " + String.valueOf(n.evaluation[Node.COMPUTER_SCORE]));
            System.out.println("Human Score: " + String.valueOf(n.evaluation[Node.HUMAN_SCORE]));
        }
        System.out.println("------------------------------------------------");
        printState(n.data);
    }
    public void printState(int[][] state) {
        String columnHeader = "    0 1 2 3 4 5 6";
        String headerDivider = "-----------------";
        System.out.println(columnHeader);
        System.out.println(headerDivider);
        for (int row = 0; row < state.length; row++) {
            String board = String.valueOf(row) + " | ";
            for (int column = 0; column < state[row].length; column++) {
                board += String.valueOf(state[row][column]) + " ";
            }
            System.out.println(board);
        }
    }
    private void printTree(Node n, int level) {
        System.out.println("------------------------------------------------");
        System.out.println("Level: " + String.valueOf(level));
        printNode(n);
        for (Node node : n.children) {
            if (node != null) {
                printTree(node, level + 1);
            }
        }
    }

}