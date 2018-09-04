package de.unijena.cheminf.npdatabasefiller.services;


import de.unijena.cheminf.npdatabasefiller.model.OriMolecule;
import de.unijena.cheminf.npdatabasefiller.model.OriMoleculeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZincCurationService {

    @Autowired
    OriMoleculeRepository omr;


    public void doWork(){


        // removing the NPs from ALL by InChi
        for(OriMolecule omNP : omr.findBySourceAndStatus("ZINC", "NP") ){

            for(OriMolecule candidate : omr.findByInChi( omNP.getInChi() )){
                if( candidate.getId() != omNP.getId() ){
                    omr.delete(candidate);
                }

            }
        }


        // removing the NPs from ALL by InChi
        for(OriMolecule omBIO : omr.findBySourceAndStatus("ZINC", "BIOGENIC") ){

            for(OriMolecule candidate : omr.findByInChi( omBIO.getInChi() )){
                if( candidate.getId() != omBIO.getId() ){
                    omr.delete(candidate);
                }

            }
        }


        for(OriMolecule omAll : omr.findBySourceAndStatus("ZINC", "ALL")){
            omAll.setStatus("SM");
            omr.save(omAll);
        }


    }








}
