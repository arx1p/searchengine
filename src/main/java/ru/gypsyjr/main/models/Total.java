package ru.gypsyjr.main.models;

public class Total {
    private Integer sites;
    private Integer pages;
    private Integer lemmas;
    private boolean isIndexing;

    public Integer getSites() {
        return sites;
    }

    public void setSites(Integer sites) {
        this.sites = sites;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getLemmas() {
        return lemmas;
    }

    public void setLemmas(Integer lemmas) {
        this.lemmas = lemmas;
    }

    public boolean isIndexing() {
        return isIndexing;
    }

    public void setIndexing(boolean indexing) {
        this.isIndexing = indexing;
    }
}
