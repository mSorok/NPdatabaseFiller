package de.unijena.cheminf.npdatabasefiller.model;

import de.unijena.cheminf.npdatabasefiller.model.Molecule;
import org.springframework.data.repository.CrudRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository

public interface MoleculeRepository extends CrudRepository<Molecule, Long> {
}
