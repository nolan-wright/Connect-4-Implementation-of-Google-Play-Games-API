package com.purduegmail.mobileapps.project_v3;

import android.app.Fragment;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by Nolan Wright on 11/12/2017.
 */

public class GameFragment extends Fragment {

    private GameFragmentListener listener;
    private LinearLayout[] columns;
    private SparseArray cellMap;
    private View.OnClickListener columnListener  = new View.OnClickListener() {
        @Override
        public void onClick(View v) {switch (v.getId()) {
                case R.id.column0:
                    listener.onColumnClicked(0);
                    break;
                case R.id.column1:
                    listener.onColumnClicked(1);
                    break;
                case R.id.column2:
                    listener.onColumnClicked(2);
                    break;
                case R.id.column3:
                    listener.onColumnClicked(3);
                    break;
                case R.id.column4:
                    listener.onColumnClicked(4);
                    break;
                case R.id.column5:
                    listener.onColumnClicked(5);
                    break;
                case R.id.column6:
                    listener.onColumnClicked(6);
                    break;
            }}
    };

    // creates new fragment instance, effectively a constructor
    public static GameFragment newInstance(GameFragmentListener listener) {
        GameFragment fragment = new GameFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        // inflate the layout associated with this fragment
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);
        // initialize columns
        initializeColumns(rootView);
        // initialize cellMap
        initializeCellMap();
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listener.onFragmentInflated();
    }

    public void highlightSequences(ArrayList<int[][]> sequences, int marker) {
        int resource = (marker == Game.MY_MARKER)
                ? R.drawable.winning_red_chip : R.drawable.winning_green_chip;
        for (int[][] sequence : sequences) {
            for (int[] coordinate : sequence) {
                // coordinate[0] = row
                // coordinate[1] = column
                int[] rowArray = (int[])cellMap.get(coordinate[0]);
                ImageView cell = getView().findViewById(rowArray[coordinate[1]]);
                cell.setImageResource(resource);
            }
        }
    }

    public void drawUpdate(int row, int column, int marker) {
        int[] rowArray = (int[])cellMap.get(row);
        ImageView cell = getView().findViewById(rowArray[column]);
        if (marker == Game.MY_MARKER) {
            cell.setImageResource(R.drawable.red_chip);
        }
        else /* marker == Game.OPPONENT_MARKER */ {
            cell.setImageResource(R.drawable.green_chip);
        }
    }

    public void setColumnsClickable(boolean clickable) {
        columns[0].setClickable(clickable);
        columns[1].setClickable(clickable);
        columns[2].setClickable(clickable);
        columns[3].setClickable(clickable);
        columns[4].setClickable(clickable);
        columns[5].setClickable(clickable);
        columns[6].setClickable(clickable);
    }

    /*
     * helper methods
     */
    // called in onCreate
    private void initializeColumns(View v) {
        columns = new LinearLayout[7];
        columns[0] = v.findViewById(R.id.column0);
        columns[0].setOnClickListener(columnListener);
        columns[1] = v.findViewById(R.id.column1);
        columns[1].setOnClickListener(columnListener);
        columns[2] = v.findViewById(R.id.column2);
        columns[2].setOnClickListener(columnListener);
        columns[3] = v.findViewById(R.id.column3);
        columns[3].setOnClickListener(columnListener);
        columns[4] = v.findViewById(R.id.column4);
        columns[4].setOnClickListener(columnListener);
        columns[5] = v.findViewById(R.id.column5);
        columns[5].setOnClickListener(columnListener);
        columns[6] = v.findViewById(R.id.column6);
        columns[6].setOnClickListener(columnListener);
    }
    private void initializeCellMap() {
        cellMap = new SparseArray();
        int[] row0 = new int[7];
        row0[0] = R.id.column0_row0;
        row0[1] = R.id.column1_row0;
        row0[2] = R.id.column2_row0;
        row0[3] = R.id.column3_row0;
        row0[4] = R.id.column4_row0;
        row0[5] = R.id.column5_row0;
        row0[6] = R.id.column6_row0;
        cellMap.append(0, row0);
        int[] row1 = new int[7];
        row1[0] = R.id.column0_row1;
        row1[1] = R.id.column1_row1;
        row1[2] = R.id.column2_row1;
        row1[3] = R.id.column3_row1;
        row1[4] = R.id.column4_row1;
        row1[5] = R.id.column5_row1;
        row1[6] = R.id.column6_row1;
        cellMap.append(1, row1);
        int[] row2 = new int[7];
        row2[0] = R.id.column0_row2;
        row2[1] = R.id.column1_row2;
        row2[2] = R.id.column2_row2;
        row2[3] = R.id.column3_row2;
        row2[4] = R.id.column4_row2;
        row2[5] = R.id.column5_row2;
        row2[6] = R.id.column6_row2;
        cellMap.append(2, row2);
        int[] row3 = new int[7];
        row3[0] = R.id.column0_row3;
        row3[1] = R.id.column1_row3;
        row3[2] = R.id.column2_row3;
        row3[3] = R.id.column3_row3;
        row3[4] = R.id.column4_row3;
        row3[5] = R.id.column5_row3;
        row3[6] = R.id.column6_row3;
        cellMap.append(3, row3);
        int[] row4 = new int[7];
        row4[0] = R.id.column0_row4;
        row4[1] = R.id.column1_row4;
        row4[2] = R.id.column2_row4;
        row4[3] = R.id.column3_row4;
        row4[4] = R.id.column4_row4;
        row4[5] = R.id.column5_row4;
        row4[6] = R.id.column6_row4;
        cellMap.append(4, row4);
        int[] row5 = new int[7];
        row5[0] = R.id.column0_row5;
        row5[1] = R.id.column1_row5;
        row5[2] = R.id.column2_row5;
        row5[3] = R.id.column3_row5;
        row5[4] = R.id.column4_row5;
        row5[5] = R.id.column5_row5;
        row5[6] = R.id.column6_row5;
        cellMap.append(5, row5);
    }

    /*
     * interface specification
     */
    public interface GameFragmentListener {
        void onColumnClicked(int column);
        void onFragmentInflated();
    }

}