package de.unijena.cheminf.npdatabasefiller.model;

import javax.persistence.*;

@Entity
@Table(name="fragment_without_sugar", indexes = {  @Index(name = "IDX1", columnList = "signature", unique = true) } )
public class FragmentWithoutSugar {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer fragment_id;

    @Column(length = 1200)
    private String signature;

    private Integer height;

    private Float scoreNP;

    private Float scoreSM;

    public Integer getFragment_id() {
        return fragment_id;
    }

    public void setFragment_id(Integer fragment_id) {
        this.fragment_id = fragment_id;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String atom_signature) {
        this.signature = atom_signature;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
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
