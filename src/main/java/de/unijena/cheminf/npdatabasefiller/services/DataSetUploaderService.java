package de.unijena.cheminf.npdatabasefiller.services;

import de.unijena.cheminf.npdatabasefiller.misc.LinearSugars;
import de.unijena.cheminf.npdatabasefiller.model.*;
import de.unijena.cheminf.npdatabasefiller.readers.ReadWorker;
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Service
public class DataSetUploaderService {

    /**
     * uploads data in OriMolecule
     * adds to molecule if needed
     * computes the fragments only for the new molecules
     * compute the scores from the fragments only from the new molecules
     * updates the scores in the molecule table
     *
     *  alternative:
     *  compute the scores for all molecules (update the scores)
     */


    private final LinearSugars linearSugarChains = LinearSugars.getInstance();
    private final int height = 2;
    @Autowired
    OriMoleculeRepository omr;
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
    ElectronDonation model = ElectronDonation.cdk();
    CycleFinder cycles = Cycles.cdkAromaticSet();
    Aromaticity aromaticity = new Aromaticity(model, cycles);
    private String fileName = null;
    private String source = null;
    private String status = null;

    /**
     * @param fileName
     * @param source
     * @param status
     */
    public void readNewDataset(String fileName, String source, String status){

        this.fileName = fileName;
        this.source=source;
        this.status=status;

        ReadWorker rw = new ReadWorker(fileName, source, status);

        boolean start = rw.startWorker();

        if(start){
            rw.doWorkWithInsertionInDB();
        }
        else{
            System.out.println("Incorrect file format");
        }

    }

    /**
     * adds the newly read dataset to Molecule table
     */
    public void addNewDatasetToMolecule(){

        List<OriMolecule> oriMolecules = omr.findBySourceAndStatus(this.source, this.status);

        for(OriMolecule oriMolecule : oriMolecules){

            // test if already in Molecule
            if( mr.findByInchikey(oriMolecule.getInchikey()) != null ){
                //already in molecule
                Molecule molecule = mr.findByInchikey(oriMolecule.getInchikey());
                oriMolecule.setUnique_mol_id(molecule.getId());
                omr.save(oriMolecule);
            }
            else{
                //create new molecule
                Molecule molecule = new Molecule();
                molecule.setInchikey(oriMolecule.getInchikey());
                molecule.setInchi(oriMolecule.getInchi());
                molecule.setSmiles(oriMolecule.getSmiles());
                molecule.setHeavy_atom_number(oriMolecule.getHeavy_atom_number());
                molecule.setTotal_atom_number(oriMolecule.getTotal_atom_number());


                if(this.status.equals("NP")){
                    molecule.setIs_a_NP(1);
                }
                else if(this.status.equals("SM")){
                    molecule.setIs_a_NP(0);
                }
                else if(this.status.equals("BIOGENIC")){
                    molecule.setIs_a_NP(-1);
                }
                else{
                    molecule.setIs_a_NP(-1);
                }
                molecule = mr.save(molecule);
                oriMolecule.setUnique_mol_id(molecule.getId());
                omr.save(oriMolecule);
            }
        }
    }


    /**
     * calculates fragments (atom signatures) for newly read molecules and inserts in the molecule_fragment_cpd table
     */
    public void calculateFragmentsForNewMolecules(){
        System.out.println("Start computing fragments for new molecules");
        List<Molecule> moleculesToFragment = mr.findAllMoleculesWithoutNPLSByStatusAndBySource(this.source, this.status);
        for(Molecule molecule : moleculesToFragment) {
            IAtomContainer ac = atomContainerToMoleculeService.createAtomContainer(molecule);

            IAtomContainer acNoSugar = removeSugars(ac);

            if (acNoSugar != null) {
                molecule.setContainsSugar(1);
                molecule.setSugar_free_total_atom_number(acNoSugar.getAtomCount());
                int noSugarHeavyAtomCount = 0;
                for(IAtom a: acNoSugar.atoms()){
                    if(!a.getSymbol().equals("H")){
                        noSugarHeavyAtomCount=noSugarHeavyAtomCount+1;
                    }
                }
                molecule.setSugar_free_heavy_atom_number(noSugarHeavyAtomCount);
            } else {
                molecule.setContainsSugar(0);
                molecule.setSugar_free_heavy_atom_number(0);
                molecule.setSugar_free_total_atom_number(0);
            }

            molecule = mr.save(molecule);

            Hashtable<String, Integer> countedFragments = generateCountedAtomSignatures(ac, height);
            for (String fragment : countedFragments.keySet()) {
                //this is for the fragment with sugar repository: fr
                List<FragmentWithSugar> fragmentsWithSugar = fr.findBySignatureAndHeight(fragment, height);
                if (fragmentsWithSugar == null || fragmentsWithSugar.isEmpty()) {
                    //new fragment!
                    FragmentWithSugar newFragment = new FragmentWithSugar();
                    newFragment.setHeight(2);
                    newFragment.setSignature(fragment);
                    newFragment.setScoreNP(1.0);

                    newFragment = fr.save(newFragment);

                    MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                    mfc.setFragment_id(newFragment.getFragment_id());
                    mfc.setMol_id(molecule.getId());
                    mfc.setHeight(height);
                    mfc.setNbfragmentinmolecule(countedFragments.get(fragment));
                    mfc.setComputed_with_sugar(1);

                    cpdRepository.save(mfc);

                } else {
                    MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                    mfc.setFragment_id(fragmentsWithSugar.get(0).getFragment_id());
                    mfc.setMol_id(molecule.getId());
                    mfc.setHeight(height);
                    mfc.setNbfragmentinmolecule(countedFragments.get(fragment));
                    mfc.setComputed_with_sugar(1);
                    cpdRepository.save(mfc);
                }

            }

            Hashtable<String, Integer> countedFragmentsNoSugar = generateCountedAtomSignatures(acNoSugar, height);
            if(countedFragmentsNoSugar != null){
                for (String fragmentNoSugar : countedFragmentsNoSugar.keySet()) {
                    //this is for the fragment with NO sugar repository: fro

                    List<FragmentWithoutSugar> fragmentsNoSugar = fro.findBySignatureAndHeight(fragmentNoSugar, height);
                    if (fragmentsNoSugar == null || fragmentsNoSugar.isEmpty()) {
                        //new fragment!
                        FragmentWithoutSugar newFragment = new FragmentWithoutSugar();
                        newFragment.setHeight(2);
                        newFragment.setSignature(fragmentNoSugar);
                        newFragment.setScoreNP(1.0);

                        newFragment = fro.save(newFragment);

                        MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                        mfc.setFragment_id(newFragment.getFragment_id());
                        mfc.setMol_id(molecule.getId());
                        mfc.setHeight(height);
                        mfc.setNbfragmentinmolecule(countedFragmentsNoSugar.get(fragmentNoSugar));
                        mfc.setComputed_with_sugar(0);

                        cpdRepository.save(mfc);

                    } else {
                        MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                        mfc.setFragment_id(fragmentsNoSugar.get(0).getFragment_id());
                        mfc.setMol_id(molecule.getId());
                        mfc.setHeight(height);
                        mfc.setNbfragmentinmolecule(countedFragments.get(fragmentNoSugar));
                        mfc.setComputed_with_sugar(0);

                        cpdRepository.save(mfc);


                    }

                }
            }


        }
    }


    /**
     * Calculates NP-likeness score for newly read molecules
     */
    public void calculateNplsForNewMolecules(){
        System.out.println("Start computing NPLS for new molecules");


        List<Molecule> moleculesToCompute = mr.findAllMoleculesWithoutNPLSByStatusAndBySource(this.source, this.status);
        for(Molecule molecule : moleculesToCompute) {

            // With sugar
            Double npl_sugar_score = 0.0;
            List<Object[]> sugarFragmentScores = cpdRepository.findAllSugarFragmentsByMolid(molecule.getId(), height); //returns fragment_id, scorenp, scoresm
            for(Object[] obj : sugarFragmentScores){

                Integer nbFragmentsInMolecule = Integer.parseInt(obj[1].toString());

                Double scorenp = Double.parseDouble(obj[2].toString());
                npl_sugar_score = npl_sugar_score+ (scorenp * nbFragmentsInMolecule);
            }
            molecule.setNpl_sugar_score( npl_sugar_score/(double)molecule.getTotal_atom_number()  );


            if(molecule.getNpl_sugar_score().isNaN()){
                molecule.setNpl_sugar_score(0.0);
            }


            /********* WITHOUT SUGAR ********/
            Double npl_score = 0.0;
            Double npl_score_noh = 0.0;

            List<Object[]> sugarfreeFragmentScores = cpdRepository.findAllSugarfreeFragmentsByMolid(molecule.getId(), height);
            for(Object[] obj : sugarfreeFragmentScores){

                Integer nbFragmentsInMolecule = Integer.parseInt(obj[1].toString());

                Double scorenp = Double.parseDouble(obj[2].toString());

                npl_score = npl_score + (scorenp * nbFragmentsInMolecule);

                //computing the score without fragments centered on H
                String signature = obj[4].toString();
                if(!signature.startsWith("[H]")){

                    npl_score_noh = npl_score_noh + (scorenp * nbFragmentsInMolecule);
                }
            }


            molecule.setNpl_score(npl_score/(double)molecule.getSugar_free_total_atom_number() );
            molecule.setNpl_noh_score(npl_score_noh / (double)molecule.getSugar_free_heavy_atom_number());

            if(molecule.getNpl_score().isNaN()){
                molecule.setNpl_score(0.0);
            }
            if(molecule.getNpl_noh_score().isNaN()){
                molecule.setNpl_noh_score(0.0);
            }

            mr.save(molecule);


        }

        //retrieve molecules without NPLS

        //calculate the score for them
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



}
