package ru.gypsyjr.main.models;

public class ApiStatistics {
    private boolean result;
    private Statistic statistics;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Statistic getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistic statistics) {
        this.statistics = statistics;
    }
}
