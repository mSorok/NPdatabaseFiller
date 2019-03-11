package de.unijena.cheminf.npdatabasefiller.services;


import de.unijena.cheminf.npdatabasefiller.model.*;
import org.openscience.cdk.io.formats.PDBFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NPLScorer {

    @Autowired
    FragmentWithSugarRepository sugarRepository;

    @Autowired
    FragmentWithoutSugarRepository sugarFreeRepository;

    @Autowired
    MoleculeFragmentCpdRepository cpdRepository;

    @Autowired
    MoleculeRepository moleculeRepository;


    public void doWork(boolean withSugar){


        System.out.println("Computing NPLikeness scores");

        int height = 2;


        if(withSugar){

            for(Molecule molecule : moleculeRepository.findAll()){

                Double npl_sugar_score = 0.0;
                Double sml_sugar_score = 0.0;

                List<Object[]> sugarFragmentScores = cpdRepository.findAllSugarFragmentsByMolid(molecule.getId(), height); //returns fragment_id, scorenp, scoresm
                for(Object[] obj : sugarFragmentScores){

                    Integer nbFragmentsInMolecule = Integer.parseInt(obj[1].toString());

                    Double scorenp = Double.parseDouble(obj[2].toString());
                    Double scoresm = Double.parseDouble(obj[3].toString());


                    npl_sugar_score = npl_sugar_score+ (scorenp * nbFragmentsInMolecule);
                    sml_sugar_score = sml_sugar_score+ (scoresm * nbFragmentsInMolecule);
                }
                molecule.setNpl_sugar_score( npl_sugar_score/molecule.getAtom_number()  );
                molecule.setSml_sugar_score(sml_sugar_score/molecule.getAtom_number());


                if(molecule.getNpl_sugar_score().isNaN()){
                    molecule.setNpl_sugar_score(0.0);
                }

                if(molecule.getSml_sugar_score().isNaN()){
                    molecule.setSml_sugar_score(0.0);
                }


                moleculeRepository.save(molecule);
            }

        }
        else{
            for(Molecule molecule : moleculeRepository.findAll()){
                Double npl_score = 0.0;
                Double sml_score = 0.0;

                Double npl_score_noh = 0.0;

                Integer noh_mol_size = 0;


                List<Object[]> sugarfreeFragmentScores = cpdRepository.findAllSugarfreeFragmentsByMolid(molecule.getId(), height);
                for(Object[] obj : sugarfreeFragmentScores){

                    Integer nbFragmentsInMolecule = Integer.parseInt(obj[1].toString());

                    Double scorenp = Double.parseDouble(obj[2].toString());
                    Double scoresm = Double.parseDouble(obj[3].toString());



                    npl_score = npl_score + (scorenp * nbFragmentsInMolecule);
                    sml_score = sml_score + (scoresm * nbFragmentsInMolecule);


                    //computing the score without fragments centered on H
                    String signature = obj[4].toString();
                    if(!signature.startsWith("[H]")){

                        noh_mol_size = noh_mol_size +1;

                        npl_score_noh = npl_score_noh + (scorenp * nbFragmentsInMolecule);


                    }


                }
                molecule.setNpl_score(npl_score/molecule.getSugar_free_atom_number());
                molecule.setSml_score(sml_score/molecule.getSugar_free_atom_number());
                molecule.setNpl_noh_score(npl_score_noh / noh_mol_size);



                if(molecule.getNpl_score().isNaN()){
                    molecule.setNpl_score(0.0);
                }
                if(molecule.getSml_score().isNaN()){
                    molecule.setSml_score(0.0);
                }

                moleculeRepository.save(molecule);
            }

        }

    }

    public void coputeScoreForCoFrequencies(boolean withSugar) {
        //TODO compute score based on frequencies and co-frequencies
        // npl_score + (or *)

        //TODO update molecule model with this new score (sugar and no sugar)
    }
}
