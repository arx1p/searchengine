package ru.gypsyjr.main.models;

import javax.persistence.*;

import java.util.Date;

@Entity
@Table(name = "site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    @Column(name = "status_time", nullable = false)
    private Date statusTime;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "name", nullable = false)
    private String name;

    private Integer pages;
    private Integer lemmas;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
