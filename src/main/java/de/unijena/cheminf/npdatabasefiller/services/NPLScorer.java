package de.unijena.cheminf.npdatabasefiller.services;


import de.unijena.cheminf.npdatabasefiller.model.*;
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


    public void doWork(){

        System.out.println("Computing NPLikeness scores");

        int height = 2;


        // for each molecule compute the four scores
        for(Molecule molecule : moleculeRepository.findAll()){

            //retrieve the molecule fragments and their scores
            // 2 sets - sugar and not sugar

            Double npl_sugar_score = 0.0;
            Double sml_sugar_score = 0.0;
            Double npl_score = 0.0;
            Double sml_score = 0.0;

            List<Object[]> sugarFragmentScores = cpdRepository.findAllSugarFragmentsByMolid(molecule.getId(), height); //returns fragment_id, scorenp, scoresm
            for(Object[] obj : sugarFragmentScores){

                Double scorenp = Double.parseDouble(obj[1].toString());
                Double scoresm = Double.parseDouble(obj[2].toString());


                npl_sugar_score = npl_sugar_score+scorenp;
                sml_sugar_score = sml_sugar_score+scoresm;
            }
            molecule.setNpl_sugar_score( npl_sugar_score/molecule.getAtom_number()  );
            molecule.setSml_sugar_score(sml_sugar_score/molecule.getAtom_number());



            List<Object[]> sugarfreeFragmentScores = cpdRepository.findAllSugarfreeFragmentsByMolid(molecule.getId(), height);
            for(Object[] obj : sugarfreeFragmentScores){

                Double scorenp = Double.parseDouble(obj[1].toString());
                Double scoresm = Double.parseDouble(obj[2].toString());

                npl_score = npl_score+scorenp;
                sml_score = sml_score+scoresm;
            }
            molecule.setNpl_score(npl_score/molecule.getAtom_number());
            molecule.setSml_score(sml_score/molecule.getAtom_number());


            moleculeRepository.save(molecule);


        }


    }
}
