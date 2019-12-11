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
