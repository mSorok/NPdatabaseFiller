package de.unijena.cheminf.npdatabasefiller.services;

import de.unijena.cheminf.npdatabasefiller.model.IMolecule;
import de.unijena.cheminf.npdatabasefiller.model.OriMolecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.springframework.stereotype.Service;


@Service
public class AtomContainerToOriMoleculeService implements AtomContainerToMolInstanceService{


    //public AtomContainerToOriMoleculeService(){
    //    System.out.println("Transforming IAtom containers to injectable OriMolecules");
    //}

    @Override
    public OriMolecule createMolInstance(IAtomContainer ac) {

        OriMolecule om = new OriMolecule();
        om.setOri_mol_id( ac.getID() );
        om.setSource(ac.getProperty("DATABASE"));

        om.setInChi(ac.getProperty("INCHI"));

        if( ac.getProperty("MOL_STATUS").toString().equals("NP") ){
            om.setIs_a_NP(1);
        }
        else{
            om.setIs_a_NP(0);
        }





        return om;
    }

    @Override
    public IAtomContainer createAtomContainer(IMolecule m) {
        return null;
    }
}
