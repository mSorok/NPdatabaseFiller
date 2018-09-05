package de.unijena.cheminf.npdatabasefiller.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FragmentWithoutSugarRepository  extends CrudRepository<FragmentWithoutSugar, Integer> {

    List<FragmentWithoutSugar> findAll();

    FragmentWithoutSugar findBySignatureAndHeight(String atom_signature, Integer height);
}
