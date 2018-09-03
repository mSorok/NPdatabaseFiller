package de.unijena.cheminf.npdatabasefiller.services;

import de.unijena.cheminf.npdatabasefiller.model.IMolecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.springframework.stereotype.Service;

@Service
public class AtomContainerToMoleculeService implements AtomContainerToMolInstanceService {
    @Override
    public IMolecule createMolInstance(IAtomContainer ac) {
        return null;
    }

    @Override
    public IAtomContainer createAtomContainer(IMolecule m) {
        return null;
    }
}
