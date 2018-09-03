package de.unijena.cheminf.npdatabasefiller.misc;

import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

public class MoleculeChecker {

    IAtomContainer molecule;

    public MoleculeChecker(IAtomContainer molecule){
        this.molecule = molecule;

    }


    public IAtomContainer checkMolecule(){


        // check ID

        if(this.molecule.getID()=="" || this.molecule.getID()==null){
            for(Object p :  this.molecule.getProperties().keySet() ) {

                if ( p.toString().toLowerCase().contains("id") ) {
                    this.molecule.setID(molecule.getProperty(p.toString()) );

                }

            }
            if(this.molecule.getID()=="" || this.molecule.getID()==null) {
                this.molecule.setID(this.molecule.getProperty("MOL_NUMBER_IN_FILE"));
                //this.molecule.setProperty("ID", this.molecule.getProperty("MOL_NUMBER_IN_FILE"));
            }


        }




        ElectronDonation model       = ElectronDonation.cdk();
        CycleFinder cycles      = Cycles.cdkAromaticSet();
        Aromaticity aromaticity = new Aromaticity(model, cycles);



        //Adding aromaticity to molecules when needed
/*        try {
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(this.molecule);
            AtomContainerManipulator.percieveAtomTypesAndConfigureUnsetProperties(this.molecule);
            aromaticity.apply(this.molecule);
        } catch (CDKException e) {
            e.printStackTrace();
        }
*/

        //Homogenize pseudo atoms - all pseudo atoms (PA) as a "*"
        for(int u=1; u< this.molecule.getAtomCount(); u++){
            if(this.molecule.getAtom(u) instanceof IPseudoAtom){

                this.molecule.getAtom(u).setSymbol("*");
                this.molecule.getAtom(u).setAtomTypeName("X");
                ((IPseudoAtom) this.molecule.getAtom(u)).setLabel("*");

            }
        }


        // Addition of implicit hydrogens & atom typer
        CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(this.molecule.getBuilder());
        for (int j=0; j< this.molecule.getAtomCount();j++) {
            IAtom atom = this.molecule.getAtom(j);
            IAtomType type = null;
            try {
                type = matcher.findMatchingAtomType(this.molecule, atom);
            } catch (CDKException e) {
                e.printStackTrace();
            }
            AtomTypeManipulator.configure(atom, type);
        }
        CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(this.molecule.getBuilder());

        try {
            adder.addImplicitHydrogens(this.molecule);
        } catch (CDKException e) {
            e.printStackTrace();
        }

        AtomContainerManipulator.convertImplicitToExplicitHydrogens(this.molecule);


        //Fixing molecular bonds
        try {
            Kekulization.kekulize(this.molecule);

        } catch (CDKException e1) {
            //e1.printStackTrace();
        } catch ( IllegalArgumentException e){
            //System.out.println("Could not kekulize molecule "+ this.molecule.getID());
        }






        return this.molecule;
    }


}
