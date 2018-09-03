package de.unijena.cheminf.npdatabasefiller.model;


//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;


import de.unijena.cheminf.npdatabasefiller.model.OriMolecule;
import org.springframework.stereotype.Repository;


// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository

//@Repository
//@EnableFooRepositories
public interface OriMoleculeRepository extends CrudRepository<OriMolecule, Integer> {

}
