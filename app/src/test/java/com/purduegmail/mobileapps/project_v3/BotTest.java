package com.purduegmail.mobileapps.project_v3;

import org.junit.Test;

/**
 * Created by Nolan Wright on 11/22/2017.
 */

public class BotTest {

    @Test
    public void executeBotMove() {
        int[][] state2 = {
                {1, 0, 2, 2, 1, 0, 2},
                {2, 0, 1, 2, 2, 0, 1},
                {1, 0, 1, 1, 1, 0, 2},
                {2, 0, 2, 2, 2, 0, 1},
                {1, 0, 1, 1, 1, 0, 2},
                {2, 1, 2, 1, 2, 0, 1}
        };
        int[][] state3 = {
                {0, 2, 2, 2, 0, 2, 0},
                {0, 1, 1, 1, 0, 1, 0},
                {0, 1, 1, 2, 0, 1, 1},
                {1, 2, 2, 2, 0, 2, 2},
                {2, 1, 1, 1, 0, 1, 1},
                {2, 2, 2, 1, 0, 2, 1}
        };
        GameBot2 bot = new GameBot2(4);
        int[] selectedMove = bot.move(state3);
        System.out.println("\nSelected Move");
        System.out.println("------------------------------");
        System.out.println("Row: " + String.valueOf(selectedMove[0])
                + " Column: " + String.valueOf(selectedMove[1]));
    }

    @Test
    public void botBattle() {
        Game g = new Game(null);
        GameBot bot2 = new GameBot(2);
        GameBot bot1 = new GameBot(6);
        while (true) {
            int[][] reverseState = bot1.copyState(g.getBoard());
            int[] easyMove = bot1.move(reverseBoard(reverseState));
            int[][] newBoard = g.getBoard();
            newBoard[easyMove[0]][easyMove[1]] = 1;
            System.out.println("-------------------------");
            bot1.printState(newBoard);
            g.setBoard(newBoard);
            if (g.getLosingSequences().size() > 0) {
                break;
            }
            int[] hardMove = bot2.move(g.getBoard());
            newBoard = g.getBoard();
            newBoard[hardMove[0]][hardMove[1]] = 2;
            System.out.println("-------------------------");
            bot2.printState(newBoard);
            g.setBoard(newBoard);
            if (!g.getLosingSequences().isEmpty()) {
                break;
            }
        }
    }

    private int[][] reverseBoard(int[][] state) {
        for (int row = 0; row < state.length; row++) {
            for (int column = 0; column < state[row].length; column++) {
                if (state[row][column] == 1) {
                    state[row][column] = 2;
                }
                else if (state[row][column] == 2) {
                    state[row][column] = 1;
                }
            }
        }
        return state;
    }

    @Test
    public void evaluateBoard() {
        int[][] state = {
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0}
        };
        int[][] state2 = {
                {1, 0, 2, 2, 1, 0, 2},
                {2, 0, 1, 2, 2, 0, 1},
                {1, 0, 1, 1, 1, 0, 2},
                {2, 0, 2, 2, 2, 0, 1},
                {1, 0, 1, 1, 1, 0, 2},
                {2, 1, 2, 1, 2, 0, 1}
        };

        GameBot2 bot2 = new GameBot2(1);
        int[] evaluation = bot2.test_evaluateBoard(state2);
        System.out.println("Computer Score: " + evaluation[0]);
        System.out.println("Human Score: " + evaluation[1]);
    }

    @Test
    public void hasWinInTwo() {
        int[][] state = {
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 0, 0},
                {0, 1, 0, 1, 0, 0, 0}
        };
        int[][] sequence = {
                {5, 0, 0},
                {5, 1, 1},
                {5, 2, 0},
                {5, 3, 1},
                {5, 4, 0},
                {5, 5, 0},
                {5, 6, 0}
        };
        GameBot bot = new GameBot(1);
        boolean b = bot.hasWinInTwo(state, sequence, GameBot.HUMAN_MARKER);
        System.out.println("Has win in 2: " + String.valueOf(b));
    }
}
