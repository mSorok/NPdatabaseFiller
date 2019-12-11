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


import com.google.common.collect.Lists;
import de.unijena.cheminf.npdatabasefiller.misc.BeanUtil;
import de.unijena.cheminf.npdatabasefiller.misc.FragmentCreationTask;
import de.unijena.cheminf.npdatabasefiller.misc.LinearSugars;
import de.unijena.cheminf.npdatabasefiller.model.*;
import de.unijena.cheminf.npdatabasefiller.misc.MoleculeConnectivityChecker;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.signature.AtomSignature;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.BondManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author mSorok
 */


@Service
public class FragmentsCalculatorService {

    @Autowired
    MoleculeRepository mr;

    @Autowired
    FragmentWithSugarRepository fr;

    @Autowired
    FragmentWithoutSugarRepository fro;

    @Autowired
    MoleculeFragmentCpdRepository cpdRepository;


    @Autowired
    AtomContainerToMoleculeService atomContainerToMoleculeService;

    @Autowired
    SugarRemovalService sugarRemovalService;





    ElectronDonation model = ElectronDonation.cdk();
    CycleFinder cycles = Cycles.cdkAromaticSet();
    Aromaticity aromaticity = new Aromaticity(model, cycles);


    private final LinearSugars linearSugarChains = LinearSugars.getInstance();

    private final int height = 2;




    public void doWork( ){




        System.out.println("Start computing fragments");


        List<FragmentWithSugar> uniqueFragmentsWithSugar = computeUniqueFragments(1);
        List<FragmentWithoutSugar> uniqueFragmentsWithoutSugar = computeUniqueFragments(0);



        // cleaning up the database
        fr.deleteAll();
        fro.deleteAll();
        cpdRepository.deleteAll();


        //saving computed fragments
        fr.saveAll(uniqueFragmentsWithSugar);
        fro.saveAll(uniqueFragmentsWithoutSugar);


        // computing the correspondencies between fragments and molecules with numbers of fragment occurence in each molecule
        computeCpd(1);
        computeCpd(0);


        System.out.println("Done calculating fragments");




/*


try {

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);

        List<List<Molecule>> moleculeListBatch = Lists.partition(allMolecules, 500);

        int taskcount = 0;


        List<Callable<Object>> todo = new ArrayList<Callable<Object>>(moleculeListBatch.size());

        System.out.println("Total number of tasks:" + moleculeListBatch.size());

        for(List<Molecule> molBatch : moleculeListBatch){
            FragmentCreationTask task = new FragmentCreationTask();
            task.getMoleculesToCompute(molBatch);

            taskcount++;

            System.out.println("Task "+taskcount+" created");
            task.taskid=taskcount;

            todo.add(Executors.callable(task));

            //executor.execute(task);

            //System.out.println("Task "+taskcount+" executing");

        }


            executor.invokeAll(todo);

            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




        //find non-unique sugar fragment strings
        // eliminate duplicates
        // curate molecule_fragment_cpd for ids

        for(Object[] obj : fr.findRedundantSignatures(height)){

            String signature = obj[0].toString();
            String stringListOfIds = obj[1].toString();

            String [] listOfIds = stringListOfIds.split(" ");
            ArrayList<Integer> intListOfIds = new ArrayList<>();
            for(String id : listOfIds){ intListOfIds.add(Integer.parseInt(id)); }

            int minId = Collections.min(intListOfIds);
            for(int id:intListOfIds){
                if(id != minId) {
                    //delete by id
                    fr.deleteById(id);
                    //retrieve the cpd - modify - save
                    List<MoleculeFragmentCpd> mfcList = cpdRepository.findByfragment_id(id);
                    for(MoleculeFragmentCpd mfc : mfcList ){
                        mfc.setFragment_id(minId);
                        cpdRepository.save(mfc);
                    }
                }
            }

        }


        //find non-unique sugar-free fragment strings
        // eliminate duplicates
        // curate molecule_fragment_cpd for ids


        // curation of the DB for redundant fragments - take min ID for same fingerprint (and same height) - replace in in molecule_fragment_cpd - remove from fragments all fragments without cpd to a molecule
*/


    }




/*


    public void doWork(){



        List<Molecule> allMolecules = mr.findAll();

        System.out.println("Number of molecules to process: " + allMolecules.size());
        System.out.println("Starting fragment calculation");
        System.out.print("0% ....");

        int count = 1;


        for(Molecule molecule : allMolecules) {


            IAtomContainer ac = atomContainerToMoleculeService.createAtomContainer(molecule);



            //Filling for molecules without sugar removal

            int height = 2;
            List<String> allFragments = generateAtomSignatures(ac, height);

            for (String f : allFragments) {

                List<FragmentWithSugar> inDBlist = fr.findBySignatureAndHeight(f, height);
                if(inDBlist.isEmpty()){

                    FragmentWithSugar newFragment = new FragmentWithSugar();
                    newFragment.setSignature(f);
                    newFragment.setHeight(height);

                    newFragment = fr.save(newFragment);

                    MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                    mfc.setMol_id(molecule.getId());
                    mfc.setFragment_id(newFragment.getFragment_id());
                    mfc.setHeight(height);
                    mfc.setComputed_with_sugar(1);

                    cpdRepository.save(mfc);


                } else {
                    FragmentWithSugar inDB = inDBlist.get(0);
                    MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                    mfc.setMol_id(molecule.getId());
                    mfc.setFragment_id(inDB.getFragment_id());
                    mfc.setHeight(height);
                    mfc.setComputed_with_sugar(1);


                    cpdRepository.save(mfc);
                }


            }



            */
/**
             * Filling for molecules after sugar removal
             *//*

            IAtomContainer sugarlessMolecule = removeSugars(ac);
            if (sugarlessMolecule == null) {
                molecule.setContainsSugar(0);
                molecule = mr.save(molecule);

            } else {
                // run the fragments computation on it!
                allFragments = generateAtomSignatures(sugarlessMolecule, height);

                if(allFragments != null){

                    for (String f : allFragments) {

                        List<FragmentWithoutSugar> inDBlist = fro.findBySignatureAndHeight(f, height);
                        if(inDBlist.isEmpty()){
                            FragmentWithoutSugar newFragment = new FragmentWithoutSugar();
                            newFragment.setSignature(f);
                            newFragment.setHeight(height);

                            newFragment = fro.save(newFragment);

                            MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                            mfc.setMol_id(molecule.getId());
                            mfc.setFragment_id(newFragment.getFragment_id());
                            mfc.setHeight(height);
                            mfc.setComputed_with_sugar(0);

                            cpdRepository.save(mfc);

                        } else {
                            FragmentWithoutSugar inDB = inDBlist.get(0);
                            MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                            mfc.setMol_id(molecule.getId());
                            mfc.setFragment_id(inDB.getFragment_id());
                            mfc.setHeight(height);
                            mfc.setComputed_with_sugar(0);
                            cpdRepository.save(mfc);
                        }


                    }
                }

            }

            count++;

            if(count == (int)Math.round( (double)allMolecules.size()*0.75 ) ){
                System.out.print(". 75% ....");
            }
            else if(count == (int)Math.round( (double)allMolecules.size()*0.50 )) {
                System.out.print(". 50% ....");
            }
            else if(count == (int)Math.round( (double)allMolecules.size()*0.25 )) {
                System.out.print(". 25% ....");
            }

*/
/*
            if(count%1000==0){
                System.out.println(count+" processed");
            }

*//*


        }
        System.out.print(". 100%\n");
        System.out.println("All fragments calculated and injected in database!");

    }
*/






    public List<String> generateAtomSignatures(IAtomContainer atomContainer, Integer height) {

        List<String> atomSignatures = new ArrayList<>();



        //atomContainer = calculateAromaticity(atomContainer);

        if( atomContainer != null  && !atomContainer.isEmpty()) {

            for (IAtom atom : atomContainer.atoms()) {
                try {
                    AtomSignature atomSignature = new AtomSignature(atom, height, atomContainer);
                    atomSignatures.add(atomSignature.toCanonicalString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return atomSignatures;
        }
        else{
            return null;
        }
    }


    public Hashtable<String, Integer> generateCountedAtomSignatures(IAtomContainer atomContainer, Integer height) {

        List<String> atomSignatures = new ArrayList<>();

        Hashtable<String, Integer> countedAtomSignatures = new Hashtable<>();



        //atomContainer = calculateAromaticity(atomContainer);

        if(atomContainer !=null && !atomContainer.isEmpty()) {

            for (IAtom atom : atomContainer.atoms()) {
                try {
                    AtomSignature atomSignature = new AtomSignature(atom, height, atomContainer);
                    atomSignatures.add(atomSignature.toCanonicalString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for(String signature : atomSignatures){
                if(countedAtomSignatures.containsKey(signature)){
                    countedAtomSignatures.put(signature, countedAtomSignatures.get(signature)+1);
                }
                else{
                    countedAtomSignatures.put(signature,1);
                }

            }


            return countedAtomSignatures;
        }
        else{
            return null;
        }
    }




    private IAtomContainer calculateAromaticity(IAtomContainer molecule) {

        try {
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
            aromaticity.apply(molecule);
            molecule.setProperty("AROMATICITY_PERCEIVED", true);
        } catch (CDKException | NullPointerException e) {
            e.printStackTrace();
        }


        return molecule;

    }








    List computeUniqueFragments(int withSugar){
        List fragmentsToReunite = null;
        HashSet<String> uniqueFragments = new HashSet<>();

        List<Molecule> allMolecules = mr.findAll();

        for(Molecule molecule : allMolecules) {

            IAtomContainer ac = atomContainerToMoleculeService.createAtomContainer(molecule);

            if(withSugar==0){
                ac = sugarRemovalService.removeSugars(ac);
                if(ac != null) {

                    molecule.setSugar_free_total_atom_number(ac.getAtomCount());
                    int countSugarFreeHeavyAtoms = 0;
                    for(IAtom a : ac.atoms()){
                        if(!a.getSymbol().equals("H")){
                            countSugarFreeHeavyAtoms=countSugarFreeHeavyAtoms+1;
                        }
                    }
                    molecule.setSugar_free_heavy_atom_number(countSugarFreeHeavyAtoms);
                }
                else{
                    molecule.setSugar_free_total_atom_number(0);
                    molecule.setSugar_free_heavy_atom_number(0);
                }
            }

            List<String> allFragments = generateAtomSignatures(ac, height);

            if(allFragments != null) {

                uniqueFragments.addAll(allFragments);
            }
        }

        if(withSugar==0){
            mr.saveAll(allMolecules);
        }




        if(withSugar==1){
            fragmentsToReunite = new ArrayList<FragmentWithSugar>();


            for(String uf : uniqueFragments){
                FragmentWithSugar newFragment = new FragmentWithSugar();
                newFragment.setSignature(uf);
                newFragment.setHeight(height);

                fragmentsToReunite.add(newFragment);
            }

        }
        else{
            fragmentsToReunite = new ArrayList<FragmentWithoutSugar>();

            for(String uf : uniqueFragments){
                FragmentWithoutSugar newFragment = new FragmentWithoutSugar();
                newFragment.setSignature(uf);
                newFragment.setHeight(height);

                fragmentsToReunite.add(newFragment);
            }
        }

        return fragmentsToReunite;
    }



    void computeCpd(int withSugar){



        Hashtable<String, Integer> fragments = new Hashtable<>();

        if(withSugar==1){
            List<FragmentWithSugar> fragmentlist = fr.findAll();
            for(FragmentWithSugar f : fragmentlist){
                fragments.put(f.getSignature(), f.getFragment_id());
            }


        }
        else{
            List<FragmentWithoutSugar> fragmentlist = fro.findAll();
            for(FragmentWithoutSugar f : fragmentlist){
                fragments.put(f.getSignature(), f.getFragment_id());
            }
        }




        List<Molecule> allMolecules = mr.findAll();

        for(Molecule molecule : allMolecules) {
            ArrayList<MoleculeFragmentCpd> newCpdList = new ArrayList<>();
            int numberRepeatedFragments = 0;

            IAtomContainer ac = atomContainerToMoleculeService.createAtomContainer(molecule);

            if (withSugar == 0) {
                ac = sugarRemovalService.removeSugars(ac);

                if(ac != null && !ac.isEmpty() && molecule != null && molecule.getTotal_atom_number() != null && ac.getAtomCount() == molecule.getTotal_atom_number()){
                    molecule.setContainsSugar(0);
                }
                else{
                    molecule.setContainsSugar(1);
                }
                molecule = mr.save(molecule);
            }

            Hashtable<String, Integer> countedFragments = generateCountedAtomSignatures(ac, height);

            if (countedFragments != null){

                for (String f : countedFragments.keySet()) {

                    if (withSugar == 1) {

                        //with sugar

                        //List<FragmentWithSugar> frag = fr.findBySignatureAndHeight(f, height);

                        MoleculeFragmentCpd mfcUnit = new MoleculeFragmentCpd();
                        mfcUnit.setMol_id(molecule.getId());
                        mfcUnit.setComputed_with_sugar(1);
                        mfcUnit.setHeight(height);
                        mfcUnit.setNbfragmentinmolecule(countedFragments.get(f));
                        mfcUnit.setFragment_id(fragments.get(f));

                        newCpdList.add(mfcUnit);

                        if(countedFragments.get(f)>1 && !f.startsWith("[H]")){
                            numberRepeatedFragments++;
                        }

                    } else {

                        //without sugar

                        //List<FragmentWithoutSugar> frag = fro.findBySignatureAndHeight(f, height);

                        MoleculeFragmentCpd mfcUnit = new MoleculeFragmentCpd();
                        mfcUnit.setMol_id(molecule.getId());
                        mfcUnit.setComputed_with_sugar(0);
                        mfcUnit.setHeight(height);
                        mfcUnit.setNbfragmentinmolecule(countedFragments.get(f));
                        mfcUnit.setFragment_id(fragments.get(f));
                        newCpdList.add(mfcUnit);

                    }


                }


        }

            cpdRepository.saveAll(newCpdList);

            if(withSugar==1){
                molecule.setNumberRepeatedFragments(numberRepeatedFragments);
                mr.save(molecule);
            }

        }



    }


}
