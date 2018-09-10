package de.unijena.cheminf.npdatabasefiller.services;


import com.google.common.collect.HashBasedTable;
import de.unijena.cheminf.npdatabasefiller.model.OriMolecule;
import de.unijena.cheminf.npdatabasefiller.model.OriMoleculeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Hashtable;
import java.util.List;

/**
 * @author mSorok
 */

@Service
@Transactional
public class ZincCurationService {

    @Autowired
    OriMoleculeRepository omr;


    public void doWork(){
        System.out.println("ZINC curation started ");

        //eliminating NPs first
        for(Object obj : omr.findNPInchikeyInZinc()) {
            try{
                String uinchi = obj.toString();
                omr.deleteAllByInchikeyAndStatusAndSource(uinchi, "SM", "ZINC");
                omr.deleteAllByInchikeyAndStatusAndSource(uinchi, "BIOGENIC", "ZINC");
            }
            catch(NullPointerException e){
                System.out.println("Failed to catch one ZINC Inchikey!");
            }

        }



        //eliminating biogenic next
        for(Object obj : omr.findBIOGENICInchikeyInZinc()) {
            try{
                String uinchi = obj.toString();
                omr.deleteAllByInchikeyAndStatusAndSource(uinchi, "SM", "ZINC");
            }
            catch(NullPointerException e){
                System.out.println("Failed to catch one ZINC Inchikey!");
            }

        }


        System.out.println("ZINC curation finished");
    }


    public void doWork_by_all_in_chi(){

        System.out.println("ZINC curation started ");



        for(Object[] obj : omr.findRedundantInchikeyInZinc()) {


            try{


                String uinchi = obj[0].toString();


                List<OriMolecule> oms = omr.findByInchikeyAndSource(uinchi, "ZINC");

                OriMolecule omToSave = null;

                OriMolecule lastBiogenic = null;

                OriMolecule lastSM = null;

                for (OriMolecule om : oms) {

                    if (om.isANP()) {
                        omToSave = om;

                    } else if (om.isBIOGENIC()) {
                        lastBiogenic = om;
                    } else {
                        lastSM = om;
                    }
                }

                if (omToSave != null) {
                    for (OriMolecule om : oms) {
                        if (om != omToSave) {
                            omr.delete(om);
                        }
                    }

                } else if (lastBiogenic != null && omToSave == null) {
                    for (OriMolecule om : oms) {
                        if (om != lastBiogenic) {
                            omr.delete(om);
                        }
                    }
                } else {
                    for (OriMolecule om : oms) {
                        if (om != lastSM) {
                            omr.delete(om);

                        }


                    }
                }
            }
            catch(NullPointerException e){
                System.out.println("Failed to catch one ZINC Inchikey!");
            }
        }

        /*
        System.out.println("Updating ZINC molecule status");
        for(OriMolecule omAll : omr.findBySourceAndStatus("ZINC", "ALL")){
            omAll.setStatus("SM");
            omr.save(omAll);
        }
        */


        System.out.println("ZINC curation finished");

    }


    public void doWorkOldWay(){


        System.out.println("ZINC curation started (long)");


        // removing the NPs from ALL by Inchikey
        for(OriMolecule omNP : omr.findBySourceAndStatus("ZINC", "NP") ){

            for(OriMolecule candidate : omr.findAllByInchikey( omNP.getInchikey() )){
                if( candidate.getId() != omNP.getId() ){
                    omr.delete(candidate);
                }

            }
        }


        // removing the NPs from ALL by Inchikey
        for(OriMolecule omBIO : omr.findBySourceAndStatus("ZINC", "BIOGENIC") ){

            for(OriMolecule candidate : omr.findAllByInchikey( omBIO.getInchikey() )){
                if( candidate.getId() != omBIO.getId() ){
                    omr.delete(candidate);
                }

            }
        }


        for(OriMolecule omAll : omr.findBySourceAndStatus("ZINC", "SM")){
            omAll.setStatus("SM");
            omr.save(omAll);
        }

        System.out.println("ZINC curation finished");


    }








}
