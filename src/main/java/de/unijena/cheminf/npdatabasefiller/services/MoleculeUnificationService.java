package de.unijena.cheminf.npdatabasefiller.services;

/**
 * @author mSorok
 */

import de.unijena.cheminf.npdatabasefiller.model.Molecule;
import de.unijena.cheminf.npdatabasefiller.model.MoleculeRepository;
import de.unijena.cheminf.npdatabasefiller.model.OriMolecule;
import de.unijena.cheminf.npdatabasefiller.model.OriMoleculeRepository;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MoleculeUnificationService {

    @Autowired
    OriMoleculeRepository omr;

    @Autowired
    MoleculeRepository mr;

    @Autowired
    AtomContainerToMoleculeService ac2m;

    /**
     * Redundancy checker by Inchikey
     */
    public void doWork(){
        System.out.println("Starting redundancy elimination work!");


        // Step 1:
        //for each Inchikey that has more than one occurence in the database
        // create unique Molecule entity
        for(Object[] obj : omr.findRedundantInchikey()) {

            try{

                String uinchi = obj[0].toString();
                List<OriMolecule> oms = omr.findAllByInchikey(uinchi); //finds all molecules that share the same InchiKey


                boolean isNP = false;
                boolean isSM = false;


                for (OriMolecule om : oms) {
                    if (om.isANP()) {
                        isNP = true;
                    }
                    if (om.isASM()) {
                        isSM = true;
                    }
                }

                if (isNP || isSM) {

                    Molecule newMol = mr.findByInchikey(uinchi);
                    if(newMol==null) {
                        //create new unique molecule instance

                        newMol = new Molecule();

                        newMol.setInchikey(oms.get(0).getInchikey());
                        newMol.setInchi(oms.get(0).getInchi());
                        newMol.setSmiles(oms.get(0).getSmiles());
                        newMol.setTotal_atom_number(oms.get(0).getTotal_atom_number());
                        newMol.setHeavy_atom_number(oms.get(0).getHeavy_atom_number());
                        if (isNP) {
                            newMol.setIs_a_NP(1);

                        } else if (isSM) {
                            newMol.setIs_a_NP(0);
                        }
                        else{
                            System.out.println("Weird molecule appeared - not SM not NP");
                            newMol.setIs_a_NP(-1);
                        }
                        newMol = mr.save(newMol);

                        for (OriMolecule om : oms) {
                            om.setUnique_mol_id(newMol.getId());
                            omr.save(om);
                        }

                    }
                    else{
                        System.out.println("In unification, multiple InChiKey :/ "+uinchi);
                    }

                }

            }catch(NullPointerException e) {
                System.out.println("Problem in retrieving redundant Inchikey");
            }




        }
        System.out.println("Finished checking redundancy - creating Unique molecule objects for non-redundant molecules");



        // Step 2:
        //create Molecule entities for Unique Inchikeykeys (encountered only once):


        for(Object[] obj : omr.findUniqueInchikey()) {
            try {

                String uinchi = obj[0].toString();

                List<OriMolecule> oms = omr.findAllByInchikey(uinchi);
                Molecule newMol = mr.findByInchikey(uinchi);
                if(newMol==null && (oms.get(0).isANP() || oms.get(0).isASM())) {

                     newMol = new Molecule();

                    newMol.setInchikey(oms.get(0).getInchikey());
                    newMol.setInchi(oms.get(0).getInchi());
                    newMol.setSmiles(oms.get(0).getSmiles());


                    newMol.setTotal_atom_number(oms.get(0).getTotal_atom_number());
                    newMol.setHeavy_atom_number(oms.get(0).getHeavy_atom_number());


                    if (oms.get(0).isANP()) {
                        newMol.setIs_a_NP(1);
                    } else if (oms.get(0).isASM()) {
                        newMol.setIs_a_NP(0);
                    }
                    newMol = mr.save(newMol);
                    oms.get(0).setUnique_mol_id(newMol.getId());
                    omr.save(oms.get(0));

                }
                else{
                    System.out.println("Inchi not unique! "+ uinchi);
                }





            }catch(NullPointerException e){
                System.out.println("Problem in retrieving unique Inchikey");
            }

        }




        System.out.println("Done");


    }



    public void computeAdditionalMolecularFeatures(){

        System.out.println("Calculating molecular parameters");


        List<Molecule> allmols = mr.findAll();

        System.out.println(allmols.size());


        for(Molecule m : allmols){

            m = computeAdditionalMolecularFeatures(m);

            mr.save(m);


        }

    }





    public void computeAdditionalMolecularFeatures(String source, String status){

        System.out.println("Calculating molecular parameters for "+source+" "+status);


        List<Molecule> allmols = mr.findAllMoleculesByStatusAndBySource(source, status);


        System.out.println(allmols.size());


        for(Molecule m : allmols){

            m = computeAdditionalMolecularFeatures(m);



            mr.save(m);


        }

    }




    public Molecule computeAdditionalMolecularFeatures(Molecule m){
        AllRingsFinder arf = new AllRingsFinder();
        MolecularFormulaManipulator mfm = new MolecularFormulaManipulator();
        AtomContainerManipulator acm = new AtomContainerManipulator();

        IAtomContainer im = ac2m.createAtomContainer(m);

        // count rings
        try {
            IRingSet rs = arf.findAllRings(im, 15);

            m.setNumberOfRings(rs.getAtomContainerCount());


        } catch (CDKException e) {
            System.out.println("Too complex: "+m.getSmiles());
        }

        //compute molecular formula
        m.setMolecularFormula(mfm.getString(mfm.getMolecularFormula(im) ));


        //compute number of carbons, of nitrogens, of oxygens
        m.setNumberOfCarbons(mfm.getElementCount(mfm.getMolecularFormula(im), "C"));

        m.setNumberOfOxygens(mfm.getElementCount(mfm.getMolecularFormula(im), "O"));

        m.setNumberOfNitrogens(mfm.getElementCount(mfm.getMolecularFormula(im), "N"));

        m.setMolecularWeight( acm.getMolecularWeight(im) );

        //ratio number carbons / size

        m.setRatioCsize(  (double)m.getNumberOfCarbons() / (double)m.getHeavy_atom_number() );


        return(m);
    }


}


