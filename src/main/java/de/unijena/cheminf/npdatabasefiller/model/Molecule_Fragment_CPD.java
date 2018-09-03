package de.unijena.cheminf.npdatabasefiller.model;


//import javax.persistence.Entity;
//import javax.persistence.Table;

//@Entity
//@Table(name="Molecule_Fragment_CPD")
public class Molecule_Fragment_CPD {

    private Integer mol_id;

    private Integer fragment_id;

    public Integer getMol_id() {
        return mol_id;
    }

    public void setMol_id(Integer mol_id) {
        this.mol_id = mol_id;
    }

    public Integer getFragment_id() {
        return fragment_id;
    }

    public void setFragment_id(Integer fragment_id) {
        this.fragment_id = fragment_id;
    }
}
