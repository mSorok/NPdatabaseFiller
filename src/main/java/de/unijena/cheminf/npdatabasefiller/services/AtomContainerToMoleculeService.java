package de.unijena.cheminf.npdatabasefiller.services;

import de.unijena.cheminf.npdatabasefiller.model.IMolecule;
import de.unijena.cheminf.npdatabasefiller.model.Molecule;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.springframework.stereotype.Service;

/**
 * @author mSorok
 * Transforms a IAtomContainer in Molecule object
 */

@Service
public class AtomContainerToMoleculeService implements AtomContainerToMolInstanceService {
    @Override
    public Molecule createMolInstance(IAtomContainer ac) {
        Molecule m = new Molecule();

        m.setInchi(ac.getProperty("INCHI"));
        m.setInchikey(ac.getProperty("INCHIKEY"));
        m.setSmiles(ac.getProperty("SMILES"));
        m.setTotal_atom_number(ac.getAtomCount());
        int heavyAtomCount = 0;
        for(IAtom a : ac.atoms()){
            if(!a.getSymbol().equals("H")){
                heavyAtomCount=heavyAtomCount+1;
            }
        }
        m.setHeavy_atom_number(heavyAtomCount);
        return m;
    }


    public IAtomContainer createAtomContainer(Molecule m) {
        IAtomContainer ac = null;

        try {
            SmilesParser sp  = new SmilesParser(SilentChemObjectBuilder.getInstance());
            ac   = sp.parseSmiles( m.getSmiles() );
        } catch (InvalidSmilesException e) {
            System.err.println(e.getMessage());
        }

        return ac;
    }


    @Override
    public IAtomContainer createAtomContainer(IMolecule m) {
        return null;
    }
}
