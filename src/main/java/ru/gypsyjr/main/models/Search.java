package ru.gypsyjr.main.models;

import java.util.Set;
import java.util.TreeSet;

public class Search {
    private boolean result = true;
    private int count = 0;
    private Set<SearchResult> data = new TreeSet<>();

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Set<SearchResult> getData() {
        return data;
    }

    public void setData(Set<SearchResult> data) {
        this.data = data;
        setCount(data.size());
    }
}
