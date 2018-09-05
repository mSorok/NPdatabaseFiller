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

    List<OriMolecule> findByInChi(String inChi);

    List<OriMolecule> findByInChiAndSource(String inChi, String source);

    List<OriMolecule> findBySourceAndStatus(String source, String status);


    @Query(nativeQuery = true, value = "SELECT DISTINCT in_chi FROM ori_molecule")
    List<String> findDistinctInChi();


    @Query(nativeQuery = true, value="SELECT COUNT(DISTINCT id) FROM ori_molecule WHERE in_chi= :in_chi")
    Integer countDistinctByInChi(@Param("in_chi") String in_chi);


    @Query(nativeQuery = true, value="SELECT in_chi, COUNT(DISTINCT id) count FROM ori_molecule GROUP BY in_chi HAVING count >1")
    List<Object[]> findRedundantInChi();

    @Query(nativeQuery = true, value="SELECT in_chi, COUNT(DISTINCT id) count FROM ori_molecule GROUP BY in_chi HAVING count =1")
    List<Object[]> findUniqueInChi();


    @Query(nativeQuery = true, value = "SELECT in_chi, COUNT(DISTINCT id) count FROM ori_molecule WHERE source = 'ZINC' GROUP BY in_chi HAVING count >1")
    List<Object[]> findRedundantInChiInZinc();




}
