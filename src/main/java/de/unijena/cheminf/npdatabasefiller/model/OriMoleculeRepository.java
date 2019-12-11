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


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Hashtable;
import java.util.List;


public interface OriMoleculeRepository extends CrudRepository<OriMolecule, Integer> {
    //implement search by id
    // search by source
    // search by is a NP



    List<OriMolecule> findBySource(String source);


    List<OriMolecule> findAllByInchikey(String inchikey);

    List<OriMolecule> findByInchikeyAndSource(String inchikey, String source);

    List<OriMolecule> findBySourceAndStatus(String source, String status);


    @Query(nativeQuery = true, value = "SELECT DISTINCT inchikey FROM ori_molecule")
    List<String> findDistinctInchikey();


    @Query(nativeQuery = true, value="SELECT COUNT(DISTINCT id) FROM ori_molecule WHERE inchikey= :inchikey")
    Integer countDistinctByInchikey(@Param("inchikey") String inchikey);


    @Query(nativeQuery = true, value="SELECT inchikey, COUNT(DISTINCT id) count FROM ori_molecule WHERE status = 'NP' OR status='SM' GROUP BY inchikey HAVING count >1")
    List<Object[]> findRedundantInchikey();

    @Query(nativeQuery = true, value="SELECT inchikey, COUNT(DISTINCT id) count FROM ori_molecule WHERE status = 'NP' OR status='SM' GROUP BY inchikey HAVING count =1")
    List<Object[]> findUniqueInchikey();



    @Query(nativeQuery = true, value = "SELECT inchikey, COUNT(DISTINCT id) count FROM ori_molecule WHERE source = 'ZINC' GROUP BY inchikey HAVING count >1")
    List<Object[]> findRedundantInchikeyInZinc();


    void deleteAllByInchikeyAndStatusAndSource( String inchikey, String status, String source  );

    @Query(nativeQuery = true, value = "SELECT inchikey FROM ori_molecule WHERE source = 'ZINC' AND status= 'NP' GROUP BY inchikey")
    List<Object > findNPInchikeyInZinc();

    @Query(nativeQuery = true, value = "SELECT inchikey FROM ori_molecule WHERE source = 'ZINC' AND status= 'BIOGENIC' GROUP BY inchikey")
    List<Object> findBIOGENICInchikeyInZinc();






}
