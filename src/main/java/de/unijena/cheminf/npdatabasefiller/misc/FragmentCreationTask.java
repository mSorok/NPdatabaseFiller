package de.unijena.cheminf.npdatabasefiller.misc;

import de.unijena.cheminf.npdatabasefiller.model.*;
import de.unijena.cheminf.npdatabasefiller.services.AtomContainerToMoleculeService;
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
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public class FragmentCreationTask implements Runnable {

    @Autowired
    MoleculeRepository mr;

    @Autowired
    @Transient
    FragmentWithSugarRepository fr;

    @Autowired
    @Transient
    FragmentWithoutSugarRepository fro;

    @Autowired
    @Transient
    MoleculeFragmentCpdRepository cpdRepository;

    @Autowired
    AtomContainerToMoleculeService atomContainerToMoleculeService;

    ElectronDonation model = ElectronDonation.cdk();
    CycleFinder cycles = Cycles.cdkAromaticSet();
    Aromaticity aromaticity = new Aromaticity(model, cycles);


    final LinearSugars linearSugarChains = LinearSugars.getInstance();


    List<Molecule> moleculesToCompute;

    public int taskid = 0;



    @Override
    public void run() {

        this.mr = BeanUtil.getBean(MoleculeRepository.class);
        this.fr =  BeanUtil.getBean(FragmentWithSugarRepository.class);
        this.fro = BeanUtil.getBean(FragmentWithoutSugarRepository.class);
        this.cpdRepository = BeanUtil.getBean(MoleculeFragmentCpdRepository.class);
        this.atomContainerToMoleculeService = BeanUtil.getBean(AtomContainerToMoleculeService.class);



        try{
            for(Molecule molecule : this.moleculesToCompute) {

                //System.out.println(cpdRepository);


                IAtomContainer ac = atomContainerToMoleculeService.createAtomContainer(molecule);


                /**
                 * Filling for molecules without sugar removal
                 */
                int height = 2;
                List<String> allFragments = generateAtomSignatures(ac, height);

                for (String f : allFragments) {
                    List<FragmentWithSugar> inDBlist = fr.findBySignatureAndHeight(f, height);
                    if(inDBlist.isEmpty()){ //if the fragment is not already present in the database



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






                /**
                 * Filling for molecules after sugar removal
                 */
                IAtomContainer sugarlessMolecule = removeSugars(ac);
                if (sugarlessMolecule == null) {
                    molecule.setContainsSugar(0);
                    molecule = mr.save(molecule);

                } else {

                    // run the fragments computation on it!
                    allFragments = generateAtomSignatures(sugarlessMolecule, height);

                    if(allFragments != null) {

                        for (String f : allFragments) {

                            List<FragmentWithoutSugar> inDBlist = fro.findBySignatureAndHeight(f, height);
                            if(inDBlist.isEmpty()) {


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
                            }
                            else {
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
                molecule.setContainsSugar(1);
                mr.save(molecule);

            }


        }catch(Exception e){
            e.printStackTrace();
        }


        System.out.println("Task "+taskid+" finished");



    }


    public void getMoleculesToCompute(List<Molecule> mols){
        this.moleculesToCompute = mols;

    }



    public List<String> generateAtomSignatures(IAtomContainer atomContainer, Integer height) {

        List<String> atomSignatures = new ArrayList<>();



        //atomContainer = calculateAromaticity(atomContainer);

        if(!atomContainer.isEmpty()) {

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




    private IAtomContainer removeSugars(IAtomContainer molecule){

        try {

            IRingSet ringset = Cycles.sssr(molecule).toRingSet();

            // RING SUGARS
            for (IAtomContainer one_ring : ringset.atomContainers()) {
                try {
                    IMolecularFormula molecularFormula = MolecularFormulaManipulator.getMolecularFormula(one_ring);
                    String formula = MolecularFormulaManipulator.getString(molecularFormula);
                    IBond.Order bondorder = AtomContainerManipulator.getMaximumBondOrder(one_ring);

                    if (formula.equals("C5O") | formula.equals("C4O") | formula.equals("C6O")) {
                        if (IBond.Order.SINGLE.equals(bondorder)) {
                            if (shouldRemoveRing(one_ring, molecule, ringset) == true) {
                                for (IAtom atom : one_ring.atoms()) {
                                    {

                                        molecule.removeAtom(atom);
                                    }
                                }
                            }

                        }
                    }
                }catch(NullPointerException e){
                    return null;
                }
            }
            Map<Object, Object> properties = molecule.getProperties();
            IAtomContainerSet molset = ConnectivityChecker.partitionIntoMolecules(molecule);
            for (int i = 0; i < molset.getAtomContainerCount(); i++) {
                molset.getAtomContainer(i).setProperties(properties);
                int size = molset.getAtomContainer(i).getBondCount();
                if (size >= 5) {
                    if (!linearSugarChains.hasSugarChains(molset.getAtomContainer(i), ringset.getAtomContainerCount())) {

                        return (IAtomContainer) molset.getAtomContainer(i);
                    }
                }
            }
            //
        } catch (NullPointerException e) {
        } catch (CDKException e) {
        }
        return null;

    }





    private boolean shouldRemoveRing(IAtomContainer possibleSugarRing, IAtomContainer molecule, IRingSet sugarRingsSet) {

        boolean shouldRemoveRing = false;
        List<IAtom> allConnectedAtoms = new ArrayList<IAtom>();
        List<IBond> bonds = new ArrayList<IBond>();
        int oxygenAtomCount = 0;

        IRingSet connectedRings = sugarRingsSet.getConnectedRings((IRing) possibleSugarRing);

        /*
         * get bonds to check for bond order of connected atoms in a sugar ring
         *
         */
        for (IAtom atom : possibleSugarRing.atoms()) {
            bonds.addAll(molecule.getConnectedBondsList(atom));
        }

        if (IBond.Order.SINGLE.equals(BondManipulator.getMaximumBondOrder(bonds))
                && connectedRings.getAtomContainerCount() == 0) {

            /*
             * get connected atoms of all atoms in sugar ring to check for glycoside bond
             */
            for (IAtom atom : possibleSugarRing.atoms()) {
                List<IAtom> connectedAtoms = molecule.getConnectedAtomsList(atom);
                allConnectedAtoms.addAll(connectedAtoms);
            }

            for (IAtom connected_atom : allConnectedAtoms) {
                if (!possibleSugarRing.contains(connected_atom)) {
                    if (connected_atom.getSymbol().matches("O")) {
                        oxygenAtomCount++;
                    }
                }
            }
            if (oxygenAtomCount > 0) {
                return true;
            }
        }
        return shouldRemoveRing;
    }




}
