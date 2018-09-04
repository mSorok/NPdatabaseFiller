package de.unijena.cheminf.npdatabasefiller.model;


import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface OriMoleculeRepository extends CrudRepository<OriMolecule, Integer> {
    //implement search by id
    // search by source
    // search by is a NP



    List<OriMolecule> findBySource(String source);

    List<OriMolecule> findByInChi(String InChi);

    List<OriMolecule> findBySourceAndStatus(String source, String status);



}
