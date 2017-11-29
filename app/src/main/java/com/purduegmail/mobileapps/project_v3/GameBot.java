package com.purduegmail.mobileapps.project_v3;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Nolan Wright on 11/20/2017.
 */

class GameBot {

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
    GameBot(int difficulty) throws BadDepthException {
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
        node.evaluation = evaluateBoard(state);
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

    // TODO: make below private after testing
    public int[] evaluateBoard(int[][] state) {
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
                int sequence_computer_score = evaluateSequence(state, sequence, COMPUTER_MARKER);
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
                int sequence_human_score = evaluateSequence(state, sequence, HUMAN_MARKER);
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
        // cell-level evaluation
        if (computerScoreIsSet && humanScoreIsSet) {
            // do nothing
        }
        else if (computerScoreIsSet && !humanScoreIsSet) {
            // evaluate human's cells
            total_human_score += evaluateCells(state, HUMAN_MARKER);
        }
        else if (!computerScoreIsSet && humanScoreIsSet) {
            // evaluate computer's cells
            total_computer_score += evaluateCells(state, COMPUTER_MARKER);
        }
        else { // neither score is set
            total_computer_score += evaluateCells(state, COMPUTER_MARKER);
            total_human_score += evaluateCells(state, HUMAN_MARKER);
        }
        return new int[] {total_computer_score, total_human_score};
    }
    /*
     * helper functions for evaluateBoard
     */
    private int evaluateCells(int[][] state, int marker) {
        int score = 0;
        for (int row = 0; row < Game.ROWS; row++) {
            for (int column = 0; column < Game.COLUMNS; column++) {
                if (state[row][column] == marker) {
                    score += (getNumberOfPossibleWinningDirections(row, column, state, marker) * 2);
                }
            }
        }
        return score;
    }
    private int evaluateSequence(int[][] state, int[][] sequence, int marker) {
        if (hasWin(sequence, marker)) {
            return EVALUATION_WON;
        }
        if (hasWinInOne(state, sequence, marker)) {
            return EVALUATION_WIN_IN_ONE;
        }
        if (hasWinInTwo(state, sequence, marker)) {
            return EVALUATION_WIN_IN_TWO;
        }
        int pointsForConnections = getPointsForConnections(sequence, marker, state);
        int numberOfConsecutiveAdjacentAccessibleCells =
                getNumberOfConsecutiveAdjacentAccessibleCells(state, sequence, marker);
        return pointsForConnections + numberOfConsecutiveAdjacentAccessibleCells;
    }
    /*
     * helper functions for evaluateSequence
     */
    private int getNumberOfConsecutiveAdjacentAccessibleCells(
            int[][] state, int[][] sequence, int marker) {
        int count = 0;
        for (int index = 0; index < sequence.length; index++) {
            if (sequence[index][CELL_VALUE] == marker) {
                int margin = 1;
                while (index - margin >= 0) {
                    if (sequence[index - margin][CELL_VALUE] == 0 &&
                            cellIsAccessible(sequence[index - margin][CELL_ROW],
                                    sequence[index - margin][CELL_COLUMN], state)) {
                        count++;
                        margin++;
                    }
                    else {
                        break;
                    }
                }
                margin = 1;
                while (index + margin < sequence.length) {
                    if (sequence[index + margin][CELL_VALUE] == 0 &&
                            cellIsAccessible(sequence[index + margin][CELL_ROW],
                                    sequence[index + margin][CELL_COLUMN], state)) {
                        count++;
                        margin++;
                    }
                    else {
                        break;
                    }
                }
            }
        }
        return count;
    }
    private int getPointsForConnections(int[][] sequence, int marker, int[][] state) {
        // 3 points for two connected markers next to an empty cell
        // 3 additional points if next to two empty cells
        // 3 additional points if one of the adjacent cells is accessible
        // 5 points for three connected markers next to an empty cell
        // 5 additional points if next to two empty cells
        // 5 additional points if one of the adjacent cells is accessible
        int points = 0;
        boolean precedingCellIsEmpty;
        boolean precedingCellIsAccessible;
        boolean twoMarkersAreAdjacent;
        boolean threeMarkersAreAdjacent;
        boolean terminatingCellIsEmpty;
        boolean terminatingCellIsAccessible;
        for (int index = 0; index < sequence.length; index++) {
            precedingCellIsEmpty = false;
            precedingCellIsAccessible = false;
            twoMarkersAreAdjacent = false;
            threeMarkersAreAdjacent = false;
            terminatingCellIsEmpty = false;
            terminatingCellIsAccessible = false;
            if (sequence[index][CELL_VALUE] == marker) {
                int[] precedingCell = null;
                if (index - 1 >= 0) {
                    precedingCell = sequence[index - 1];
                }
                int[] followingCell = null;
                if (index + 1 < sequence.length) {
                    followingCell = sequence[index + 1];
                }
                if (precedingCell != null && precedingCell[CELL_VALUE] == Game.NULL_MARKER) {
                    // preceding cell is empty
                    precedingCellIsEmpty = true;
                    if (cellIsAccessible(precedingCell[CELL_ROW],
                            precedingCell[CELL_COLUMN], state)) {
                        precedingCellIsAccessible = true;
                    }
                }
                if (followingCell != null && followingCell[CELL_VALUE] == marker) {
                    // initial marker and this marker are adjacent
                    twoMarkersAreAdjacent = true;
                    index++; // no need to recheck this cell
                    int[] nextCell = null;
                    if (index + 1 < sequence.length) {
                        nextCell = sequence[index + 1];
                    }
                    if (nextCell != null) {
                        if (nextCell[CELL_VALUE] == marker) {
                            // initial marker and following marker and this marker are adjacent
                            threeMarkersAreAdjacent = true;
                            index++; // no need to recheck this cell
                            int[] followingNextCell = null;
                            if (index + 1 < sequence.length) {
                                followingNextCell = sequence[index + 1];
                            }
                            if (followingNextCell != null) {
                                // there cannot be a fourth adjacent marker
                                if (followingNextCell[CELL_VALUE] == Game.NULL_MARKER) {
                                    // cell after the three adjacent markers is empty
                                    terminatingCellIsEmpty = true;
                                    if (cellIsAccessible(followingNextCell[CELL_ROW],
                                            followingNextCell[CELL_COLUMN], state)) {
                                        terminatingCellIsAccessible = true;
                                    }
                                }
                            }
                        }
                        else if (nextCell[CELL_VALUE] == Game.NULL_MARKER) {
                            // cell following the two adjacent markers is empty
                            terminatingCellIsEmpty = true;
                            if (cellIsAccessible(nextCell[CELL_ROW], nextCell[CELL_ROW], state)) {
                                terminatingCellIsAccessible = true;
                            }
                        }
                    }
                }
            }
            if (threeMarkersAreAdjacent) {
                if (precedingCellIsEmpty) {
                    points += 5;
                }
                if (terminatingCellIsEmpty) {
                    points += 5;
                }
                if (precedingCellIsAccessible || terminatingCellIsAccessible) {
                    points += 5;
                }
            }
            else if (twoMarkersAreAdjacent) {
                if (precedingCellIsEmpty) {
                    points += 3;
                }
                if (terminatingCellIsEmpty) {
                    points += 3;
                }
                if (precedingCellIsAccessible || terminatingCellIsAccessible) {
                    points += 3;
                }
            }
        }
        return points;
    }
    private int getNumberOfPossibleWinningDirections(
            int row, int column, int[][] state, int marker) {
        int numberOfPossibleWinningDirections = 0;
        // going left
        int count = 0;
        int margin = 1;
        while (column - margin >= 0) {
            if (state[row][column - margin] == 0 || state[row][column - margin] == marker) {
                count++;
            }
            else {
                break;
            }
            if (count == 3) {
                numberOfPossibleWinningDirections++;
                break;
            }
            margin++;
        }
        // going right
        count = 0;
        margin = 1;
        while (column + margin < Game.COLUMNS) {
            if (state[row][column + margin] == 0 || state[row][column + margin] == marker) {
                count++;
            }
            else {
                break;
            }
            if (count == 3) {
                numberOfPossibleWinningDirections++;
                break;
            }
            margin++;
        }
        // going down and to the left
        count = 0;
        margin = 1;
        while (row + margin < Game.ROWS && column - margin >= 0) {
            if (state[row + margin][column - margin] == 0 ||
                    state[row + margin][column - margin] == marker) {
                count++;
            }
            else {
                break;
            }
            if (count == 3) {
                numberOfPossibleWinningDirections++;
                break;
            }
            margin++;
        }
        // going up and to the left
        count = 0;
        margin = 1;
        while (row - margin >= 0 && column - margin >= 0) {
            if (state[row - margin][column - margin] == 0 ||
                    state[row - margin][column - margin] == marker) {
                count++;
            }
            else {
                break;
            }
            if (count == 3) {
                numberOfPossibleWinningDirections++;
                break;
            }
            margin++;
        }
        // going up and to the right
        count = 0;
        margin = 1;
        while (row - margin >= 0 && column + margin < Game.COLUMNS) {
            if (state[row - margin][column + margin] == 0 ||
                    state[row - margin][column + margin] == marker) {
                count++;
            }
            else {
                break;
            }
            if (count == 3) {
                numberOfPossibleWinningDirections++;
                break;
            }
            margin++;
        }
        // going down and to the right
        count = 0;
        margin = 1;
        while (row + margin < Game.ROWS && column + margin < Game.COLUMNS) {
            if (state[row + margin][column + margin] == 0 ||
                    state[row + margin][column + margin] == marker) {
                count++;
            }
            else {
                break;
            }
            if (count == 3) {
                numberOfPossibleWinningDirections++;
                break;
            }
            margin++;
        }
        return numberOfPossibleWinningDirections;
    }
    private boolean hasWin(int[][] sequence, int marker) {
        int count = 0;
        for (int[] cell : sequence) {
            if (cell[CELL_VALUE] == marker) {
                count++;
                if (count == 4) {
                    return true;
                }
            }
            else {
                count = 0;
            }
        }
        return false;
    }
    private boolean hasWinInOne(int[][] state, int[][] sequence, int marker) {
        ArrayList<Integer> cells = new ArrayList<>();
        for (int index = 0; index < sequence.length; index++) {
            int[] cell = sequence[index];
            if (cell[CELL_VALUE] == marker) {
                cells.add(index);
                if (cells.size() == 3) { // a sequence may satisfy this condition only once
                    int startingIndex = cells.get(0);
                    if (startingIndex == 0) {
                        return false; // there is no previous cell in the sequence
                    }
                    int[] previousCell = sequence[startingIndex - 1];
                    if (previousCell[CELL_VALUE] != Game.NULL_MARKER) {
                        return false; // cannot make a move here
                    }
                    if (!cellIsAccessible(previousCell[CELL_ROW], previousCell[CELL_COLUMN], state)) {
                        return false; // this cell is not accessible
                    }
                    int terminatingIndex = cells.get(2);
                    if (terminatingIndex == sequence.length - 1) {
                        return false; // there is no following cell in the sequence
                    }
                    int[] followingCell = sequence[terminatingIndex + 1];
                    if (followingCell[CELL_VALUE] != Game.NULL_MARKER) {
                        return false; // cannot make a move here
                    }
                    if (!cellIsAccessible(followingCell[CELL_ROW], followingCell[CELL_COLUMN], state)) {
                        return false; // this cell is not accessible
                    }
                    return true;
                }
            }
            else {
                cells.clear();
            }
        }
        return false;
    }
    // TODO: make below private after testing
    public boolean hasWinInTwo(int[][] state, int[][] sequence, int marker) {
        ArrayList<Integer> cells = new ArrayList<>();
        for (int index = 0; index < sequence.length; index++) {
            int[] cell = sequence[index];
            if (cell[CELL_VALUE] == marker) {
                cells.add(index);
                if (cells.size() == 2) {
                    int startingIndex = cells.get(0);
                    if (startingIndex == 0) {
                        cells.clear(); // there is no previous cell in the sequence
                        continue;
                    }
                    int[] previousCell = sequence[startingIndex - 1];
                    if (previousCell[CELL_VALUE] != Game.NULL_MARKER) {
                        cells.clear(); // cannot make a move here
                        continue;
                    }
                    if (!cellIsAccessible(previousCell[CELL_ROW], previousCell[CELL_COLUMN], state)) {
                        cells.clear(); // this cell is not accessible
                        continue;
                    }
                    int terminatingIndex = cells.get(1);
                    if (terminatingIndex == sequence.length - 1) {
                        cells.clear(); // there is no following cell in the sequence
                        continue;
                    }
                    int[] followingCell = sequence[terminatingIndex + 1];
                    if (followingCell[CELL_VALUE] != Game.NULL_MARKER) {
                        cells.clear(); // cannot make a move here
                        continue;
                    }
                    if (!cellIsAccessible(followingCell[CELL_ROW], followingCell[CELL_COLUMN], state)) {
                        cells.clear(); // this cell is not accessible
                        continue;
                    }
                    // does there exist a second accessible cell on one of the two ends
                    boolean secondAdjacentAccessibleCellBefore = false;
                    if (startingIndex - 2 >= 0) {
                        int[] cellBeforePreviousCell = sequence[startingIndex - 2];
                        if (cellBeforePreviousCell[CELL_VALUE] == 0 &&
                                cellIsAccessible(cellBeforePreviousCell[CELL_ROW],
                                        cellBeforePreviousCell[CELL_COLUMN], state)) {
                            secondAdjacentAccessibleCellBefore = true;
                        }
                    }
                    boolean secondAdjacentAccessibleCellAfter = false;
                    if (terminatingIndex + 2 <= sequence.length - 1) {
                        int[] cellAfterFollowingCell = sequence[terminatingIndex + 2];
                        if (cellAfterFollowingCell[CELL_VALUE] == 0 &&
                                cellIsAccessible(cellAfterFollowingCell[CELL_ROW],
                                        cellAfterFollowingCell[CELL_COLUMN], state)) {
                            secondAdjacentAccessibleCellAfter = true;
                        }
                    }
                    if (!secondAdjacentAccessibleCellBefore && !secondAdjacentAccessibleCellAfter) {
                        cells.clear();
                        continue;
                    }
                    return true;
                }
            }
            else {
                cells.clear();
            }
        }
        return false;
    }
    private boolean cellIsAccessible(int row, int column, int[][] state) {
        if (row == Game.ROWS - 1) {
            return true;
        }
        else if (state[row + 1][column] != Game.NULL_MARKER) {
            return true;
        }
        else {
            return false;
        }
    }

    /*
     * nested classes
     */
    private class BadDepthException extends RuntimeException {
        public BadDepthException() {
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