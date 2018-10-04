package sample;

import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainNeuralNetwork extends MultiLayerPerceptron {

    private static final int PAST_PERIODS = 5;
    private static final int LEN_ONE_TIC_INPUT = 10;
    private static MainNeuralNetwork ANN;

    private MainNeuralNetwork(int[] layers) {
        super(layers);
    }

    public static void init() {
        int[] layers = {PAST_PERIODS*LEN_ONE_TIC_INPUT, PAST_PERIODS*LEN_ONE_TIC_INPUT*2/3, 3};
        MainNeuralNetwork.ANN = new MainNeuralNetwork(layers);
    }

    public static MainNeuralNetwork getANN() {
        return ANN;
    }

    public void train(CommonCallback<Boolean, Double> callback) throws SQLException {
        this.randomizeWeights();
        ArrayList<PreperedDataRow> preperedData = PreperedDataRow.getArrayOfDataFromDB();

        DataSet dataSet = new DataSet(PAST_PERIODS*LEN_ONE_TIC_INPUT, 3);
        for (int i = 0+PAST_PERIODS; i < preperedData.size(); i++) {
            double[] input = new double[dataSet.getInputSize()];
            double[] output = new double[dataSet.getOutputSize()];
            output[0] = preperedData.get(i).isFutureDown() ? 1 : 0;
            output[1] = preperedData.get(i).isFutureSame() ? 1 : 0;
            output[2] = preperedData.get(i).isFutureUp() ? 1 : 0;
            for (int j = 0; j < PAST_PERIODS; j++) {
                input[j*LEN_ONE_TIC_INPUT + 0] = preperedData.get(i).getBodyPos();
                input[j*LEN_ONE_TIC_INPUT + 1] = preperedData.get(i).getBodyNeg();
                input[j*LEN_ONE_TIC_INPUT + 2] = preperedData.get(i).getTopTail();
                input[j*LEN_ONE_TIC_INPUT + 3] = preperedData.get(i).getBottomTail();
                input[j*LEN_ONE_TIC_INPUT + 4] = preperedData.get(i).isM1m2t() ? 1 : 0;
                input[j*LEN_ONE_TIC_INPUT + 5] = preperedData.get(i).isM2m1t() ? 1 : 0;
                input[j*LEN_ONE_TIC_INPUT + 6] = preperedData.get(i).isM1tm2() ? 1 : 0;
                input[j*LEN_ONE_TIC_INPUT + 7] = preperedData.get(i).isM2tm1() ? 1 : 0;
                input[j*LEN_ONE_TIC_INPUT + 8] = preperedData.get(i).isTm1m2() ? 1 : 0;
                input[j*LEN_ONE_TIC_INPUT + 9] = preperedData.get(i).isTm2m1() ? 1 : 0;
            }
            dataSet.addRow(input, output);
        }
        if (callback.call(train(dataSet))) {
            train(callback);
        }
    }


    private double train(DataSet dataSet) {
        BackPropagation backPropagation = new BackPropagation();
        backPropagation.setMaxIterations(10);
        this.learn(dataSet, backPropagation);
        return backPropagation.getTotalNetworkError();
    }
}
