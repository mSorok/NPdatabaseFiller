package de.unijena.cheminf.npdatabasefiller.model;

import javax.persistence.*;


@Entity
@Table(name="Molecule")
public class Molecule implements IMolecule{


    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer mol_id;

    private Integer is_a_NP;

    private Float NPL_score;

    @Column(length = 1000)
    private String InChi;

    @Column(length = 1000)
    private String SMILES;






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
        return InChi;
    }

    public void setInChi(String inChi) {
        InChi = inChi;
    }

    public String getSMILES() {
        return SMILES;
    }

    public void setSMILES(String SMILES) {
        this.SMILES = SMILES;
    }

}
