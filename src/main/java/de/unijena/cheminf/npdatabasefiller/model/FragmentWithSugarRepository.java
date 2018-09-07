package de.unijena.cheminf.npdatabasefiller.model;

import de.unijena.cheminf.npdatabasefiller.model.FragmentWithSugar;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FragmentWithSugarRepository extends CrudRepository<FragmentWithSugar, Integer> {

    List<FragmentWithSugar> findAll();

    FragmentWithSugar findBySignatureAndHeight(String atom_signature, Integer height);

    List<FragmentWithSugar> findAllByHeight(Integer height);



}
