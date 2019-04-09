package de.unijena.cheminf.npdatabasefiller.services;

import de.unijena.cheminf.npdatabasefiller.model.IMolecule;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * @author mSorok
 */
public interface AtomContainerToMolInstanceService {
    IMolecule createMolInstance(IAtomContainer ac);
    IAtomContainer createAtomContainer(IMolecule m);
}
