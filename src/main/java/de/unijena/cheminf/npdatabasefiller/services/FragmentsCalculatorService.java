package de.unijena.cheminf.npdatabasefiller.services;


import de.unijena.cheminf.npdatabasefiller.model.*;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.signature.AtomSignature;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class FragmentsCalculatorService {

    @Autowired
    MoleculeRepository mr;

    @Autowired
    FragmentWithSugarRepository fr;

    @Autowired
    MoleculeFragmentCpdRepository cpdRepository;


    @Autowired
    AtomContainerToMoleculeService atomContainerToMoleculeService;


    ElectronDonation model = ElectronDonation.cdk();
    CycleFinder cycles = Cycles.cdkAromaticSet();
    Aromaticity aromaticity = new Aromaticity(model, cycles);


    public void doWork(){
        System.out.println("Starting fragment calculation");

        for(Molecule molecule : mr.findAll()){

            IAtomContainer ac = atomContainerToMoleculeService.createAtomContainer(molecule);

            //TODO tag sugars differently

            for(int height=1; height<=3 ; height++  ){
                List<String> allFragments = generateAtomSignatures(ac, height);

                for(String f : allFragments){

                    FragmentWithSugar inDB = fr.findBySignatureAndHeight( f, height );

                    if ( inDB == null ) {
                        FragmentWithSugar newFragment = new FragmentWithSugar();
                        newFragment.setSignature(f);
                        newFragment.setHeight(height);

                        newFragment = fr.save(newFragment);

                        MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                        mfc.setMol_id(molecule.getId());
                        mfc.setFragment_id(newFragment.getFragment_id());
                        mfc.setHeight(height);

                        cpdRepository.save(mfc);


                    }
                    else{
                        MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                        mfc.setMol_id(molecule.getId());
                        mfc.setFragment_id(inDB.getFragment_id());
                        mfc.setHeight(height);

                        cpdRepository.save(mfc);
                    }

                    // check if already present in fragment table
                    // if yes - only add the molecule - fragment - height in molecule_fragment_cpd table
                    //else : create a new fragment + insert into molecule_fragment_cpd

                }
            }






        }

        System.out.println("All fragments calculated and injected in database!");

    }


    // for each unique molecule in the database
    // read it, transform it back to a IAtomContainer
    // create 3 Entities - Fragment (if doesn't exist yet)
    // the MoleculeFragmentCpd entity to link the molecule to the Fragment





    public List<String> generateAtomSignatures(IAtomContainer atomContainer, Integer height) {

        List<String> atomSignatures = new ArrayList<>();



        calculateAromaticity(atomContainer);

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




    private void calculateAromaticity(IAtomContainer molecule) {

            try {
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
                aromaticity.apply(molecule);
            } catch (Exception e) {
                e.printStackTrace();
            }
            molecule.setProperty("AROMATICITY_PERCEIVED", true);

    }

}
