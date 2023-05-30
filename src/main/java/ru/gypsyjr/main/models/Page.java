package ru.gypsyjr.main.models;


import javax.persistence.*;

@Entity
@Table(name = "page", indexes = @Index(name = "pathPage", columnList = "path"))
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "path", nullable = false, unique = true, columnDefinition = "VARCHAR(256)")
    private String path;

    @Column(name = "code", nullable = false)
    private int code;

    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @OneToOne
    @JoinColumn(name = "site_id")
    private Site site;

    public Page() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
}
