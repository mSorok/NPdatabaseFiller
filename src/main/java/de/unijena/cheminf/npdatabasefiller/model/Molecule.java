package de.unijena.cheminf.npdatabasefiller.model;

import javax.persistence.*;


@Entity
@Table(name="molecule", indexes = {  @Index(name = "IDX1", columnList = "inChi"), @Index(name = "IDX2", columnList = "smiles") } )
public class Molecule implements IMolecule{


    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer mol_id;

    private Integer is_a_NP;

    private Float NPL_score;

    @Column(length = 2000)
    private String inChi;

    @Column(length = 2000)
    private String smiles;

    private boolean containsSugar;






    public Integer getId() {
        return mol_id;
    }

    public void setId(Integer mol_id) {
        this.mol_id = mol_id;
    }

    public Integer getIs_a_NP() {
        return is_a_NP;
    }

    public void setIs_a_NP(Integer is_a_NP) {
        this.is_a_NP = is_a_NP;
    }

    public Float getNPL_score() {
        return NPL_score;
    }

    public void setNPL_score(Float NPL_score) {
        this.NPL_score = NPL_score;
    }

    public String getInChi() {
        return this.inChi;
    }

    public void setInChi(String inChi) {
        this.inChi = inChi;
    }

    public String getSmiles() {
        return smiles;
    }

    public void setSmiles(String SMILES) {
        this.smiles = SMILES;
    }

    public boolean isContainsSugar() {
        return containsSugar;
    }

    public void setContainsSugar(boolean containsSugar) {
        this.containsSugar = containsSugar;
    }
}
