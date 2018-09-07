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
     * Redundancy checker by Inchikey
     */
    public void doWork(){
        System.out.println("Starting redundancy elimination work!");

        //for each Inchikey that has more than one occurence in the database
        // create unique Molecule entity



        for(Object[] obj : omr.findRedundantInchikey()) {

            try{

                String uinchi = obj[0].toString();
                List<OriMolecule> oms = omr.findAllByInchikey(uinchi);
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

                    newMol.setInchikey(oms.get(0).getInchikey());
                    newMol.setSmiles(oms.get(0).getSmiles());
                    newMol.setAtom_number(oms.get(0).getAtom_number());
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
                System.out.println("Problem in retrieving redundant Inchikey");
            }




        }
        System.out.println("Finished checking redundancy - creating Unique molecule objects for non-redundant molecules");


        //create Molecule entities for UniqueInchikeykeys also:





        for(Object[] obj : omr.findUniqueInchikey()) {
            try {

                String uinchi = obj[0].toString();

                List<OriMolecule> oms = omr.findAllByInchikey(uinchi);

                Molecule newMol = new Molecule();

                newMol.setInchikey(oms.get(0).getInchikey());
                newMol.setSmiles(oms.get(0).getSmiles());
                newMol.setAtom_number(oms.get(0).getAtom_number());
                if (oms.get(0).isANP()) {
                    newMol.setIs_a_NP(1);
                } else if (oms.get(0).isASM()) {
                    newMol.setIs_a_NP(0);
                }

                newMol = mr.save(newMol);
                oms.get(0).setUnique_mol_id(newMol.getId());
                omr.save(oms.get(0));



            }catch(NullPointerException e){
                System.out.println("Problem in retrieving unique Inchikey");
            }

        }

        System.out.println("Done");


    }

}
