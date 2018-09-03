package de.unijena.cheminf.npdatabasefiller.model;

import javax.persistence.*;

@Entity
@Table(name="Fragment")
public class Fragment {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer fragment_id;


    private String string_fragment;

    private Float scoreNP;

    private Float scoreSM;

    public Integer getFragment_id() {
        return fragment_id;
    }

    public void setFragment_id(Integer fragment_id) {
        this.fragment_id = fragment_id;
    }

    public String getString_fragment() {
        return string_fragment;
    }

    public void setString_fragment(String string_fragment) {
        this.string_fragment = string_fragment;
    }

    public Float getScoreNP() {
        return scoreNP;
    }

    public void setScoreNP(Float scoreNP) {
        this.scoreNP = scoreNP;
    }

    public Float getScoreSM() {
        return scoreSM;
    }

    public void setScoreSM(Float scoreSM) {
        this.scoreSM = scoreSM;
    }
}
