package sample;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class PreperedDataRow {

    private static final int MA1 = 24;
    private static final int MA2 = 120;
    private static  final int FUTURE = 6;
    private double open, high, low, close, topTail, bottomTail, bodyPos, bodyNeg, ma1, ma2;
    private boolean tm1m2, tm2m1, m1tm2, m1m2t, m2tm1, m2m1t;
    private boolean fullData;
    private boolean futureUp, futureDown, futureSame;
    private Timestamp date;

    private PreperedDataRow(Timestamp date, double open, double high, double low, double close) {
        this.date = date;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        double range = high - low;
        if (range == 0) {
            range = 0.00001;
        }
        this.topTail = (high-Math.max(open, close))/range;
        this.bottomTail = (Math.min(open, close)-low)/range;
        double body = close - open;
        if (body > 0) {
            this.bodyPos = body/range;
            this.bodyNeg = 0;
        } else {
            this.bodyPos = 0;
            this.bodyNeg = body/range;
        }
    }

    private PreperedDataRow(ResultSet resultSet) {
        try {
            this.date = resultSet.getTimestamp("_date");
            this.open = resultSet.getDouble("_open");
            this.close = resultSet.getDouble("_close");
            this.low = resultSet.getDouble("_low");
            this.high = resultSet.getDouble("_high");
            this.bodyNeg = resultSet.getDouble("_body_neg");
            this.bodyPos = resultSet.getDouble("_body_pos");
            this.topTail = resultSet.getDouble("_top_tail");
            this.bottomTail = resultSet.getDouble("_bottom_tail");
            this.ma1 = resultSet.getDouble("_ma1");
            this.ma2 = resultSet.getDouble("_ma2");
            this.tm1m2 = resultSet.getBoolean("_tm1m2");
            this.tm2m1 = resultSet.getBoolean("_tm2m1");
            this.m1tm2 = resultSet.getBoolean("_m1tm2");
            this.m1m2t = resultSet.getBoolean("_m1m2t");
            this.m2tm1 = resultSet.getBoolean("_m2tm1");
            this.m2m1t = resultSet.getBoolean("_m2m1t");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<PreperedDataRow> generateArrayOfData(ResultSet resultSet) throws SQLException {
        ArrayList<PreperedDataRow> result = new ArrayList<PreperedDataRow>();
        while (resultSet.next()) {
            result.add(new PreperedDataRow(resultSet.getTimestamp("_date"), resultSet.getDouble("_open"), resultSet.getDouble("_high"),
                    resultSet.getDouble("_low"), resultSet.getDouble("_close")));

        }

        int sumMA1 = sumMA1();
        int sunMA2 = sumMA2();
        int arrSize = result.size();
        for (int i = 0; i < arrSize; i++) {
            PreperedDataRow currentRow = result.get(i);
            if (i > MA2-1) {
                double ma1 = 0;
                for (int j = 0; j < MA1; j++) {
                    ma1 += result.get(i-j).open*(MA1-j);
                }
                currentRow.ma1 = ma1/sumMA1;

                double ma2 = 0;
                for (int j = 0; j < MA2; j++) {
                    ma2 += result.get(i-j).open*(MA2-j);
                }
                currentRow.ma2 = ma2/sunMA2;

                if (currentRow.open > currentRow.ma1 && currentRow.ma1 > currentRow.ma2) {
                    currentRow.tm1m2 = true;
                } else if (currentRow.open > currentRow.ma2 && currentRow.ma2 > currentRow.ma1) {
                    currentRow.tm2m1 = true;
                } else if (currentRow.ma2 > currentRow.ma1 && currentRow.ma1 > currentRow.open) {
                    currentRow.m2m1t = true;
                } else if (currentRow.ma2 > currentRow.open && currentRow.open > currentRow.ma1) {
                    currentRow.m2tm1 = true;
                } else if (currentRow.ma1 > currentRow.ma2 && currentRow.ma2 > currentRow.open) {
                    currentRow.m1m2t = true;
                } else if (currentRow.ma1 > currentRow.open && currentRow.open < currentRow.ma2) {
                    currentRow.m1tm2 = true;
                }
            }

            if (i < arrSize - FUTURE - 1 && i > MA2-1) {
                if (1.03*currentRow.open < result.get(i+FUTURE).open) {
                    currentRow.futureUp = true;
                } else if (0.97*currentRow.open > result.get(i+FUTURE).open) {
                    currentRow.futureDown = true;
                } else {
                    currentRow.futureSame = true;
                }
                currentRow.fullData = true;
            }

        }
        return result;
    }

    public static ArrayList<PreperedDataRow> getArrayOfDataFromDB() throws SQLException {
        ResultSet resultSet = ConnectorSQL.getDataDB().getDataForTraining();
        ArrayList<PreperedDataRow> result = new ArrayList<PreperedDataRow>();
        while (resultSet.next()) {
            result.add(new PreperedDataRow(resultSet));
        }
        return result;
    }

    public static ArrayList<PreperedDataRow> getArrayOfLastDataFromDB() throws SQLException {
        ResultSet resultSet = ConnectorSQL.getDataDB().getLastData();
        ArrayList<PreperedDataRow> result = new ArrayList<PreperedDataRow>();
        while (resultSet.next()) {
            result.add(new PreperedDataRow(resultSet));
        }
        return result;
    }

    private static int sumMA1() {
        int sum = 0;
        for(int i = 1; i < MA1+1; i++) {
            sum += i;
        }
        return sum;
    }

    private static int sumMA2() {
        int sum = 0;
        for(int i = 1; i < MA2+1; i++) {
            sum += i;
        }
        return sum;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public double getTopTail() {
        return topTail;
    }

    public double getBottomTail() {
        return bottomTail;
    }

    public double getBodyPos() {
        return bodyPos;
    }

    public double getBodyNeg() {
        return bodyNeg;
    }

    public double getMa1() {
        return ma1;
    }

    public double getMa2() {
        return ma2;
    }

    public boolean isTm1m2() {
        return tm1m2;
    }

    public boolean isTm2m1() {
        return tm2m1;
    }

    public boolean isM2tm1() {
        return m2tm1;
    }

    public boolean isM2m1t() {
        return m2m1t;
    }

    public boolean isFutureUp() {
        return futureUp;
    }

    public boolean isFutureDown() {
        return futureDown;
    }

    public boolean isFutureSame() {
        return futureSame;
    }

    public boolean isM1tm2() {
        return m1tm2;
    }

    public boolean isM1m2t() {
        return m1m2t;
    }

    public Timestamp getDate() {
        return date;
    }

    public boolean isFullData() {
        return fullData;
    }
}
