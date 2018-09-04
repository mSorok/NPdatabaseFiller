package de.unijena.cheminf.npdatabasefiller.services;


import de.unijena.cheminf.npdatabasefiller.model.MoleculeRepository;
import de.unijena.cheminf.npdatabasefiller.model.OriMoleculeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MoleculeUnificationService {

    @Autowired
    OriMoleculeRepository omr;

    @Autowired
    MoleculeRepository mr;


    public void doWork(){
        System.out.println("I'm ready to do work!");
        //TODO

        // look at the old NP scorer to see how they unify the molecules
    }

}
