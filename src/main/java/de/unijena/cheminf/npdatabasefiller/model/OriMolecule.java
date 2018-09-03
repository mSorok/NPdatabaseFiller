package de.unijena.cheminf.npdatabasefiller.model;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name="OriMolecule")
public class OriMolecule implements IMolecule{


    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    private String ori_mol_id;

    private String source;

    private String InChi;

    private Integer is_a_NP;

    private Integer unique_mol_id;

    private Date additionDate;



    @PrePersist
    protected void onCreate() {
        additionDate = new Date();
    }






    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOri_mol_id() {
        return ori_mol_id;
    }

    public void setOri_mol_id(String ori_mol_id) {
        this.ori_mol_id = ori_mol_id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getInChi() {
        return InChi;
    }

    public void setInChi(String inChi) {
        InChi = inChi;
    }

    public Integer getIs_a_NP() {
        return is_a_NP;
    }

    public void setIs_a_NP(Integer is_a_NP) {
        this.is_a_NP = is_a_NP;
    }

    public Integer getUnique_mol_id() {
        return unique_mol_id;
    }

    public void setUnique_mol_id(Integer unique_mol_id) {
        this.unique_mol_id = unique_mol_id;
    }

    public Date getAdditionDate() {
        return additionDate;
    }

    public void setAdditionDate(Date additionDate) {
        this.additionDate = additionDate;
    }

    @Override
    public String toString(){
        return getOri_mol_id()+"  "+getInChi()+"  "+getIs_a_NP();

    }

}
