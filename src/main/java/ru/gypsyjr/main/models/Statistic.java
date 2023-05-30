package ru.gypsyjr.main.models;

import java.util.ArrayList;
import java.util.List;

public class Statistic {
    private Total total;
    private List<Site> detailed;

    public Statistic() {
        detailed = new ArrayList<>();
    }

    public Total getTotal() {
        return total;
    }

    public void setTotal(Total total) {
        this.total = total;
    }

    public List<Site> getDetailed() {
        return detailed;
    }

    public void setDetailed(List<Site> detailed) {
        this.detailed = detailed;
    }

    public void addDetailed(Site site) {
        detailed.add(site);
    }
}
