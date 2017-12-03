package com.purduegmail.mobileapps.project_v3;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Nolan Wright on 12/1/2017.
 */

abstract class GameBot {

    private final static int EVALUATION_WON = Integer.MAX_VALUE - 1;
    private final static int EVALUATION_LOST = Integer.MIN_VALUE + 1;

    protected final static int OPPONENT_MARKER = 1;
    protected final static int BOT_MARKER = 2;

    protected int depth;

    protected int[] move(int[][] state) {
        Node root = new Node(state);
        buildTree(root, depth, BOT_MARKER);
        evaluateTree(root, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return Collections.max(root.children).move;
    }

    /*
     * helper methods
     */
    // called in move
    private void evaluateTree(Node node, boolean maximizingPlayer, int alpha, int beta) {
        if (node.isTerminal()) { // node is a leaf node
            if (!node.hasReachedEndState) { // node's state does not satisfy the win condition
                node.evaluation = evaluate(node.state);
            }
        }
        else {
            if (maximizingPlayer) {
                for (Node child : node.children) {
                    evaluateTree(child, false, alpha, beta);
                    // alpha beta pruning
                    if (child.evaluation > alpha) alpha = child.evaluation;
                    if (beta <= alpha) {
                        node.evaluation = alpha;
                        return;
                    }
                }
                node.evaluation = Collections.max(node.children).evaluation;
            }
            else {
                for (Node child : node.children) {
                    evaluateTree(child, true, alpha, beta);
                    // alpha beta pruning
                    if (child.evaluation < beta) beta = child.evaluation;
                    if (beta <= alpha) {
                        node.evaluation = beta;
                        return;
                    }
                }
                node.evaluation = Collections.min(node.children).evaluation;
            }
        }
    }
    private void buildTree(Node node, int depth, int marker) {
        if (depth > 0) {
            for (int[] possibleMove : getPossibleMoves(node.state)) {
                Node child = new Node(copyState(node.state));
                child.move = possibleMove;
                child.applyMove(marker);
                if (hasWinningSequence(child.state, marker)) {
                    child.hasReachedEndState = true;
                    child.evaluation = (marker == BOT_MARKER) ? EVALUATION_WON : EVALUATION_LOST;
                }
                else {
                    int oppositeMarker = (marker == BOT_MARKER) ? OPPONENT_MARKER : BOT_MARKER;
                    buildTree(child, depth - 1, oppositeMarker);
                }
                node.children.add(child);
            }
        }
    }
    // called in buildTree
    private int[][] copyState(final int[][] state) {
        int[][] copiedState = new int[state.length][];
        for (int i = 0; i < state.length; i++) {
            int[] row = state[i];
            copiedState[i] = new int[row.length];
            System.arraycopy(row, 0, copiedState[i], 0, row.length);
        }
        return copiedState;
    }
    private ArrayList<int[]> getPossibleMoves(final int[][] state) {
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
    private boolean hasWinningSequence(final int[][] state, final int marker) {
        return hasWinningColumn(state, marker) ||
                hasWinningRow(state, marker) ||
                hasWinningDiagonal(state, marker);
    }
    // called in hasWinningSequence
    private boolean hasWinningColumn(int[][] state, int marker) {
        final int winning_threshold = 2;
        // starts at bottom-left, works up and across
        for (int column = 0; column < Game.COLUMNS; column++) {
            int count = 0;
            int row = Game.ROWS - 1;
            while (row > winning_threshold) {
                // impossible to win if starting at 2nd row or higher
                while (row >= 0 && state[row][column] == marker) {
                    count++;
                    row--;
                }
                if (count >= 4) {
                    // winning sequence
                    return true;
                }
                else /* not a winning sequence */ {
                    count = 0;
                }
                if (row >= 0 && state[row][column] == Game.NULL_MARKER) {
                    // stop checking this column
                    break;
                }
                row--;
            }
        }
        return false;
    }
    private boolean hasWinningRow(int[][] state, int marker) {
        final int winning_threshold = 4;
        // starts at bottom-left, works across and up
        for (int row = Game.ROWS - 1; row >= 0; row--) {
            int count = 0;
            int column = 0;
            while (column < winning_threshold) {
                // impossible to win if starting at or past 4th column
                while (column < Game.COLUMNS && state[row][column] == marker) {
                    count++;
                    column++;
                }
                if (count >= 4) {
                    // winning sequence
                    return true;
                }
                else /* not a winning sequence */ {
                    count = 0;
                }
                column++;
            }
        }
        return false;
    }
    private boolean hasWinningDiagonal(int[][] state, int marker) {
        return hasWinningForwardDiagonal(state, marker)
                || hasWinningBackwardDiagonal(state, marker);
    }
    // called in hasWinningDiagonal
    private boolean hasWinningForwardDiagonal(int[][] state, int marker) {
        int limit = 3; // bound of winnable area
        for (int row = Game.ROWS - 1; row >= 3; row--) {
            for (int column = 0; column <= limit; column++) {
                // start in bottom-left
                int count = 0;
                int margin = 0;
                while ((row - margin) >= 0 && (column + margin) < Game.COLUMNS) {
                    if (state[row - margin][column + margin] == marker) {
                        count++;
                        // move forward diagonally
                        margin++;
                    }
                    else {
                        if (count >= 4) {
                            // winning sequence
                            return true;
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
                // before shifting right, check count again
                if (count >= 4) {
                    // winning sequence
                    return true;
                }
            }
            // so that diagonals are not rechecked
            limit = 0;
        }
        return false;
    }
    private boolean hasWinningBackwardDiagonal(int[][] state, int marker) {
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
                            return true;
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
                // before shifting left, check count again
                if (count >= 4) {
                    // winning sequence
                    return true;
                }
            }
            // so that diagonals are not rechecked
            limit = 6;
        }
        return false;
    }

    /*
     * method to be implemented
     */
    protected abstract int evaluate(final int[][] state);

    /*
     * nested class
     */
    private class Node implements Comparable<Node> {

        @Override
        public int compareTo(@NonNull Node other) {
            // return -1 if this < other, 1 if this > other
            return (this.evaluation > other.evaluation) ? 1 : -1;
        }

        int[][] state;
        int[] move;
        int evaluation;
        boolean hasReachedEndState = false; // win condition has been meet
        ArrayList<Node> children;

        public Node(int[][] state) {
            children = new ArrayList<>();
            this.state = state;
        }

        void applyMove(int marker) {
            int row = move[0]; int column = move[1];
            state[row][column] = marker;
        }
        boolean isTerminal() {
            return children.isEmpty();
        }
    }

}