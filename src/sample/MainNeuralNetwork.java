package sample;

import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainNeuralNetwork extends MultiLayerPerceptron {

    private static final int PAST_PERIODS = 5;
    private static final int LEN_ONE_TIC_INPUT = 10;
    private static MainNeuralNetwork ANN;
    public static final String[] OUTPUT_NAMES = {"Вниз","Так-же","Вверх"};

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
        ArrayList<PreperedDataRow> preperedData = PreperedDataRow.getArrayOfDataFromDB();

        DataSet dataSet = new DataSet(PAST_PERIODS*LEN_ONE_TIC_INPUT, 3);
        for (int i = 0+PAST_PERIODS; i < preperedData.size(); i++) {
            double[] input = new double[dataSet.getInputSize()];
            double[] output = new double[dataSet.getOutputSize()];
            output[0] = preperedData.get(i).isFutureDown() ? 1 : 0;
            output[1] = preperedData.get(i).isFutureSame() ? 1 : 0;
            output[2] = preperedData.get(i).isFutureUp() ? 1 : 0;
            for (int j = 0; j < PAST_PERIODS; j++) {
                input[j*LEN_ONE_TIC_INPUT + 0] = preperedData.get(i-j).getBodyPos();
                input[j*LEN_ONE_TIC_INPUT + 1] = preperedData.get(i-j).getBodyNeg();
                input[j*LEN_ONE_TIC_INPUT + 2] = preperedData.get(i-j).getTopTail();
                input[j*LEN_ONE_TIC_INPUT + 3] = preperedData.get(i-j).getBottomTail();
                input[j*LEN_ONE_TIC_INPUT + 4] = preperedData.get(i-j).isM1m2t() ? 1 : 0;
                input[j*LEN_ONE_TIC_INPUT + 5] = preperedData.get(i-j).isM2m1t() ? 1 : 0;
                input[j*LEN_ONE_TIC_INPUT + 6] = preperedData.get(i-j).isM1tm2() ? 1 : 0;
                input[j*LEN_ONE_TIC_INPUT + 7] = preperedData.get(i-j).isM2tm1() ? 1 : 0;
                input[j*LEN_ONE_TIC_INPUT + 8] = preperedData.get(i-j).isTm1m2() ? 1 : 0;
                input[j*LEN_ONE_TIC_INPUT + 9] = preperedData.get(i-j).isTm2m1() ? 1 : 0;
            }
            dataSet.addRow(input, output);
        }
        if (callback.call(train(dataSet))) {
            train(callback);
        }
    }

    public void getResultByLastData(String date, CommonCallback<Boolean, double[]> callback) throws SQLException {
        ArrayList<PreperedDataRow> preperedData = PreperedDataRow.getArrayOfLastDataFromDB(date);


        double[] input = new double[PAST_PERIODS * LEN_ONE_TIC_INPUT];
        for (int j = 0; j < PAST_PERIODS; j++) {
            input[j * LEN_ONE_TIC_INPUT + 0] = preperedData.get(PAST_PERIODS - 1 - j).getBodyPos();
            input[j * LEN_ONE_TIC_INPUT + 1] = preperedData.get(PAST_PERIODS - 1 - j).getBodyNeg();
            input[j * LEN_ONE_TIC_INPUT + 2] = preperedData.get(PAST_PERIODS - 1 - j).getTopTail();
            input[j * LEN_ONE_TIC_INPUT + 3] = preperedData.get(PAST_PERIODS - 1 - j).getBottomTail();
            input[j * LEN_ONE_TIC_INPUT + 4] = preperedData.get(PAST_PERIODS - 1 - j).isM1m2t() ? 1 : 0;
            input[j * LEN_ONE_TIC_INPUT + 5] = preperedData.get(PAST_PERIODS - 1 - j).isM2m1t() ? 1 : 0;
            input[j * LEN_ONE_TIC_INPUT + 6] = preperedData.get(PAST_PERIODS - 1 - j).isM1tm2() ? 1 : 0;
            input[j * LEN_ONE_TIC_INPUT + 7] = preperedData.get(PAST_PERIODS - 1 - j).isM2tm1() ? 1 : 0;
            input[j * LEN_ONE_TIC_INPUT + 8] = preperedData.get(PAST_PERIODS - 1 - j).isTm1m2() ? 1 : 0;
            input[j * LEN_ONE_TIC_INPUT + 9] = preperedData.get(PAST_PERIODS - 1 - j).isTm2m1() ? 1 : 0;
        }
        this.setInput(input);
        this.calculate();
        double[] output = this.getOutput();
        callback.call(output);
    }

    public void save(File file) {
        this.save(file.getAbsolutePath());
    }

    public void load(File file) {
        try {
            ANN = (MainNeuralNetwork) MultiLayerPerceptron.load(Files.newInputStream(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double train(DataSet dataSet) {
        BackPropagation backPropagation = new BackPropagation();
        //backPropagation.setMaxIterations(10000000);
        backPropagation.setMaxError(1e-9);
        this.learn(dataSet, backPropagation);
        return backPropagation.getTotalNetworkError();
    }
}
