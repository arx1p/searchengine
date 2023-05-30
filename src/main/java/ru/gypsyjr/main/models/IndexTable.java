package ru.gypsyjr.main.models;

import javax.persistence.*;

@Entity
@Table(name = "search_index")
public class IndexTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    //    @Column(name = "page_id")
    @ManyToOne
    @JoinColumn(name = "page_id")
    private Page page;
    //    @Column(name = "lemma_id")
    @ManyToOne
    @JoinColumn(name = "lemma_id")
    private Lemma lemma;
    @Column(name = "lemma_rank")
    private float lemmaRank;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Lemma getLemma() {
        return lemma;
    }

    public void setLemma(Lemma lemma) {
        this.lemma = lemma;
    }

    public float getLemmaRank() {
        return lemmaRank;
    }

    public void setLemmaRank(float lemmaRank) {
        this.lemmaRank = lemmaRank;
    }
}
