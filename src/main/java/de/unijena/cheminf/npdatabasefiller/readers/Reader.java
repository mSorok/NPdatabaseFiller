package de.unijena.cheminf.npdatabasefiller.readers;

import org.javatuples.Pair;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.io.File;
import java.util.ArrayList;

public interface Reader {

    Pair readFile(File file);

    void readFileAndInsertInDB(File file, String source, String moleculeStatus);



    ArrayList<IAtomContainer> returnCorrectMolecules();
}
