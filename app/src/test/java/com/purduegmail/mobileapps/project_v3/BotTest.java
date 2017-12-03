package com.purduegmail.mobileapps.project_v3;

import org.junit.Test;

/**
 * Created by Nolan Wright on 11/22/2017.
 */

public class BotTest {

    @Test
    public void executeBotMove() {
        int[][] state1 = {
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 0, 0}
        };
        int[][] state2 = {
                {0, 0, 0, 1, 0, 0, 0},
                {0, 1, 2, 2, 0, 0, 0},
                {2, 1, 1, 2, 0, 0, 0},
                {1, 2, 2, 1, 0, 0, 0},
                {2, 1, 1, 2, 0, 1, 0},
                {2, 1, 2, 1, 0, 1, 2}
        };
        int[][] state3 = {
                {0, 0, 2, 2, 0, 0, 2},
                {0, 0, 1, 2, 0, 0, 1},
                {2, 0, 1, 1, 0, 0, 2},
                {1, 0, 2, 2, 0, 0, 1},
                {2, 0, 1, 1, 0, 0, 2},
                {1, 0, 2, 1, 1, 0, 1}
        };
        SimpleGameBot bot = new SimpleGameBot(4);
        int[] selectedMove = bot.move(state2);
        System.out.println("\nSelected Move");
        System.out.println("------------------------------");
        System.out.println("Row: " + String.valueOf(selectedMove[0])
                + " Column: " + String.valueOf(selectedMove[1]));
    }

    @Test
    public void evaluateBoard() {
        int[][] state1 = {
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 0, 0},
                {0, 0, 0, 2, 0, 0, 0},
                {0, 0, 1, 1, 1, 0, 0}
        };
        int[][] state2 = {
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0}
        };
        SimpleGameBot bot = new SimpleGameBot(1);
        int evaluation = bot.evaluate(state2);
        System.out.println("Score: " + evaluation);
    }
}
