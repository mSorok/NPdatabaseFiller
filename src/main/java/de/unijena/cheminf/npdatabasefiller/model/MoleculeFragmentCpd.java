/*
 * Copyright (c) 2019.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.unijena.cheminf.npdatabasefiller.model;


import javax.persistence.*;

@Entity
@Table(name="molecule_fragment_cpd" ,
        indexes = {
        @Index(name = "IDX1", columnList = "mol_id, fragment_id" ) ,
                @Index(name="IDX2", columnList ="mol_id"),
                @Index(name="IDX3", columnList="fragment_id"),
        @Index(name="IDX4", columnList = "height"),
        @Index(name="IDX5", columnList = "computed_with_sugar")})
public class MoleculeFragmentCpd {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="mfc_id", nullable = true)
    private Integer mfc_id;


    private Integer mol_id;

    private Integer fragment_id;

    private Integer height;

    private Integer nbfragmentinmolecule;

    private Integer computed_with_sugar;




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

    public Integer getMfc_id() {
        return mfc_id;
    }

    public void setMfc_id(Integer mfc_id) {
        this.mfc_id = mfc_id;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer isComputed_with_sugar() {
        return computed_with_sugar;
    }

    public void setComputed_with_sugar(Integer computed_with_sugar) {
        this.computed_with_sugar = computed_with_sugar;
    }

    public Integer getNbfragmentinmolecule() {
        return nbfragmentinmolecule;
    }

    public void setNbfragmentinmolecule(Integer nbfragmentinmolecule) {
        this.nbfragmentinmolecule = nbfragmentinmolecule;
    }

    public Integer getComputed_with_sugar() {
        return computed_with_sugar;
    }
}
