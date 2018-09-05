package de.unijena.cheminf.npdatabasefiller.services;

/**
 * @author mSorok
 */

import de.unijena.cheminf.npdatabasefiller.model.Molecule;
import de.unijena.cheminf.npdatabasefiller.model.MoleculeRepository;
import de.unijena.cheminf.npdatabasefiller.model.OriMolecule;
import de.unijena.cheminf.npdatabasefiller.model.OriMoleculeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MoleculeUnificationService {

    @Autowired
    OriMoleculeRepository omr;

    @Autowired
    MoleculeRepository mr;

    /**
     * Redundancy checker by InChi
     */
    public void doWork(){
        System.out.println("I'm ready to do redundancy elimination work! (very long)");

        //for each inchi that has more than one occurence in the database
        // create unique Molecule entity

        //TODO remove entities with less than 6 atoms
        //TODO CurateStrangeElements

        for(Object[] obj : omr.findRedundantInChi()) {

            try{

            String uinchi = obj[0].toString();
            List<OriMolecule> oms = omr.findByInChi(uinchi);
            boolean isNP = false;
            boolean isSM = false;
            for (OriMolecule om : oms) {
                if (om.isANP()) {
                    isNP = true;
                }
                if (om.isASM()) {
                    isSM = true;
                }
            }

            if (isNP || isSM) {
                Molecule newMol = new Molecule();

                newMol.setInChi(oms.get(0).getInChi());
                newMol.setSmiles(oms.get(0).getSmiles());
                if (isNP) {
                    newMol.setIs_a_NP(1);

                } else if (isSM) {
                    newMol.setIs_a_NP(0);
                }
                newMol = mr.save(newMol);


                for (OriMolecule om : oms) {
                    om.setUnique_mol_id(newMol.getId());
                }
            }

        }catch(NullPointerException e) {
                System.out.println("Problem in retrieving redundant InChi");
        }




        }
        System.out.println("Finished checking redundancy - creating Unique molecule objects for others");


        //create Molecule entities for UniqueInChis also:
        for(Object[] obj : omr.findUniqueInChi()) {
            try {

                String uinchi = obj[0].toString();
                List<OriMolecule> oms = omr.findByInChi(uinchi);

                Molecule newMol = new Molecule();

                newMol.setInChi(oms.get(0).getInChi());
                newMol.setSmiles(oms.get(0).getSmiles());
                if (oms.get(0).getStatus() == "NP") {
                    newMol.setIs_a_NP(1);
                } else if (oms.get(0).getStatus() != "SM") {
                    newMol.setIs_a_NP(0);
                }

                newMol = mr.save(newMol);


                oms.get(0).setUnique_mol_id(newMol.getId());
            }catch(NullPointerException e){
                System.out.println("Problem in retrieving unique InChi");
            }

        }
        System.out.println("Done");


    }

}
