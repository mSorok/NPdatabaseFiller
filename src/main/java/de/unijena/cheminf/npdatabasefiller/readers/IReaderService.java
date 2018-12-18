package de.unijena.cheminf.npdatabasefiller.readers;

import org.javatuples.Pair;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public interface IReaderService {

    void readLocationFile(String fileName);

    void readMolecularFiles();

    //Pair readMolecularFile(File file);

    //IAtomContainer checkMolecule(IAtomContainer molecule);

    HashSet<String> readMolecularFilesAndInsertInDatabase();


    ArrayList<IAtomContainer> returnCorrectMolecules();

    void iAmAlive();

}
