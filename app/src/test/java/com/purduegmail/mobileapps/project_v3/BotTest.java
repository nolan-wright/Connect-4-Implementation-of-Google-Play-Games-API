package com.purduegmail.mobileapps.project_v3;

import org.junit.Test;

/**
 * Created by Nolan Wright on 11/22/2017.
 */

public class BotTest {

    @Test
    public void executeBotMove() {
        int[][] state = {
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 0, 0},
                {0, 0, 1, 1, 0, 0, 0},
        };
        GameBot bot = new GameBot(6);
        int[] selectedMove = bot.move(state);
        System.out.println("\nSelected Move");
        System.out.println("------------------------------");
        System.out.println("Row: " + String.valueOf(selectedMove[0])
                + " Column: " + String.valueOf(selectedMove[1]));
    }

    @Test
    public void evaluateBoard() {
        int[][] state = {
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 0, 0},
        };
        GameBot bot = new GameBot(1);
        int[] evaluation = bot.evaluateBoard(state);
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
