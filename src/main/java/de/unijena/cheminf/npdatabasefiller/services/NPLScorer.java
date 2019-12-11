/*
 * Copyright (c) 2019.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

                List<Object[]> sugarFragmentScores = cpdRepository.findAllSugarFragmentsByMolid(molecule.getId(), height); //returns fragment_id, scorenp, scoresm
                for(Object[] obj : sugarFragmentScores){

                    Integer nbFragmentsInMolecule = Integer.parseInt(obj[1].toString());

                    Double scorenp = Double.parseDouble(obj[2].toString());


                    npl_sugar_score = npl_sugar_score+ (scorenp * nbFragmentsInMolecule);
                }
                molecule.setNpl_sugar_score( npl_sugar_score/(double)molecule.getTotal_atom_number() );


                if(molecule.getNpl_sugar_score().isNaN()){
                    molecule.setNpl_sugar_score(0.0);
                }

                moleculeRepository.save(molecule);
            }

        }
        else{
            for(Molecule molecule : moleculeRepository.findAll()){
                Double npl_score = 0.0;
                Double npl_score_noh = 0.0;


                List<Object[]> sugarfreeFragmentScores = cpdRepository.findAllSugarfreeFragmentsByMolid(molecule.getId(), height);
                for(Object[] obj : sugarfreeFragmentScores) {



                    Integer nbFragmentsInMolecule = Integer.parseInt(obj[1].toString());

                    Double scorenp = Double.parseDouble(obj[2].toString());

                    npl_score = npl_score + (scorenp * nbFragmentsInMolecule);


                    //computing the score without fragments centered on H
                    String signature = obj[3].toString();
                    if (!signature.startsWith("[H]")) {
                        npl_score_noh = npl_score_noh + (scorenp * nbFragmentsInMolecule);
                    }

                }
                molecule.setNpl_score(npl_score/ (double)molecule.getSugar_free_total_atom_number() );
                molecule.setNpl_noh_score(npl_score_noh / (double)molecule.getSugar_free_heavy_atom_number());



                if(molecule.getNpl_score().isNaN()){
                    molecule.setNpl_score(0.0);
                }

                if(molecule.getNpl_noh_score().isNaN()){
                    molecule.setNpl_noh_score(0.0);
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
