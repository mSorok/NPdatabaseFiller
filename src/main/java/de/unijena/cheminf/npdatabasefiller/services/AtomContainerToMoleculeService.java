package de.unijena.cheminf.npdatabasefiller.services;

import de.unijena.cheminf.npdatabasefiller.model.IMolecule;
import de.unijena.cheminf.npdatabasefiller.model.Molecule;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.springframework.stereotype.Service;

@Service
public class AtomContainerToMoleculeService implements AtomContainerToMolInstanceService {
    @Override
    public Molecule createMolInstance(IAtomContainer ac) {
        Molecule m = new Molecule();

        m.setInChi(ac.getProperty("INCHI"));
        m.setSmiles(ac.getProperty("SMILES"));

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
