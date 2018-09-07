package de.unijena.cheminf.npdatabasefiller.model;

import javax.persistence.*;


@Entity
@Table(name="molecule", indexes = {  @Index(name = "IDX1", columnList = "inchikey", unique = true)} )
public class Molecule implements IMolecule{


    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer mol_id;

    private Integer is_a_NP;

    private Double NPL_score;

    private Double SML_score;

    @Column(length = 1200)
    private String inchi;

    @Column(length=30)
    private String inchikey;

    @Column(length = 1200)
    private String smiles;

    private Integer atom_number;

    private Integer containsSugar;






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

    public Double getNPL_score() {
        return NPL_score;
    }

    public void setNPL_score(Double NPL_score) {
        this.NPL_score = NPL_score;
    }

    public Double getSML_score() {
        return SML_score;
    }

    public void setSML_score(Double SML_score) {
        this.SML_score = SML_score;
    }

    public String getInchi() {
        return this.inchi;
    }

    public void setInchi(String inchi) {
        this.inchi = inchi;
    }

    public String getInchikey() {
        return inchikey;
    }

    public void setInchikey(String inchikey) {
        this.inchikey = inchikey;
    }

    public String getSmiles() {
        return smiles;
    }

    public void setSmiles(String SMILES) {
        this.smiles = SMILES;
    }

    public Integer isContainsSugar() {
        return containsSugar;
    }

    public void setContainsSugar(Integer containsSugar) {
        this.containsSugar = containsSugar;
    }

    public Integer getAtom_number() {
        return atom_number;
    }

    public void setAtom_number(Integer atom_number) {
        this.atom_number = atom_number;
    }

    public Integer getContainsSugar() {
        return containsSugar;
    }
}
