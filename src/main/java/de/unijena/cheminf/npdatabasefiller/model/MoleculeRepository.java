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

import de.unijena.cheminf.npdatabasefiller.model.Molecule;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository

public interface MoleculeRepository extends CrudRepository<Molecule, Integer> {


    List<Molecule> findAll();

    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule WHERE mol_id= :mol_id")
    Integer findAtomCountByMolId(@Param("mol_id") Integer mol_id);

    @Query(nativeQuery = true, value = "SELECT * FROM molecule INNER JOIN ori_molecule ON(mol_id=unique_mol_id) WHERE source='OLD2012'")
    List<Molecule> findMoleculesFromOLD2012();

    Molecule findByInchikey(String inchikey);

    @Query(nativeQuery = true, value = "Select * from molecule WHERE npl_score IS NULL")
    List<Molecule> findAllMoleculesWithoutNPLS();

    @Query(nativeQuery = true, value = "Select * from molecule INNER JOIN ori_molecule ON(mol_id=unique_mol_id) WHERE npl_score IS NULL AND source= :source AND status= :status")
    List<Molecule> findAllMoleculesWithoutNPLSByStatusAndBySource(@Param("source") String source, @Param("status") String status);

    @Query(nativeQuery = true, value = "Select * from molecule INNER JOIN ori_molecule ON(mol_id=unique_mol_id) WHERE source= :source AND status= :status")
    List<Molecule> findAllMoleculesByStatusAndBySource(@Param("source") String source, @Param("status") String status);


}
