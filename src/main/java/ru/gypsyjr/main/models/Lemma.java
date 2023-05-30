package ru.gypsyjr.main.models;


import javax.persistence.*;

@Entity
@Table(name = "lemma")
public class Lemma implements Comparable<Lemma> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "lemma", nullable = false)
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private int frequency;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    @Override
    public int compareTo(Lemma o) {
        if (frequency > o.getFrequency()) {
            return 1;
        } else if (frequency < o.getFrequency()) {
            return -1;
        }

        return 0;
    }

}
