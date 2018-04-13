package Tetris.Features;

import Tetris.NextState;
import Tetris.State;

public class AverageHeightFeature extends Feature{

    @Override
    public double getValue(State state) {
        double sumOfHeight = 0;

        int[] tops = state.getTop();
        for (int i = 0; i < tops.length; i++) {
            sumOfHeight += tops[i];
        }

        double average = sumOfHeight / state.COLS;

        return average;
    }

    public double getValue(NextState state) {
        double sumOfHeight = 0;

        int[] tops = state.getTop();
        for (int i = 0; i < tops.length; i++) {
            sumOfHeight += tops[i];
        }

        double average = sumOfHeight / 10;

        return average;
    }
}
