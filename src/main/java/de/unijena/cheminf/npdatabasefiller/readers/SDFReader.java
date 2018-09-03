package de.unijena.cheminf.npdatabasefiller.readers;



import org.javatuples.Pair;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;


import de.unijena.cheminf.npdatabasefiller.misc.MoleculeChecker;


public class SDFReader implements Reader {

    File file;
    ArrayList<IAtomContainer> listOfMolecules;

    private IteratingSDFReader reader = null;
    private SDFWriter moleculeWithScoreWriter = null;


    public SDFReader(){
        this.listOfMolecules = new ArrayList<IAtomContainer>();
    }

    @Override
    public Pair readFile(File file) {

        this.file = file;
        int count = 1;

        //System.out.println("\n\n Working on: "+this.file.getName() + "\n\n");
        //System.out.println("\n\n Working on: "+this.file.getAbsolutePath() + "\n\n");


        //Read and store molecules here




        try {
            reader = new IteratingSDFReader(new FileInputStream(file), DefaultChemObjectBuilder.getInstance());
            System.out.println("SDF reader creation");
            reader.setSkip(true);


            // *********************



            //System.out.println("I am iterating in SDF file - 1");
            //System.out.println(reader);

            while (reader.hasNext()) {
                //System.out.println("I am iterating in SDF file - 2");
                try {
                    IAtomContainer molecule = reader.next();

                    Map properties = molecule.getProperties();


                    molecule.setProperty("MOL_NUMBER_IN_FILE",  Integer.toString(count) );
                    molecule.setProperty("FILE_ORIGIN", file.getName().replace(".sdf", ""));


                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                    LocalDate localDate = LocalDate.now();

                    molecule.setProperty("ACQUISITION_DATE", dtf.format(localDate));



                    molecule = checkMolecule(molecule);


                    //System.out.println(molecule.getProperties());


                    listOfMolecules.add(molecule);


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                count++;

                if(count%50000==0){
                    System.out.println("Molecules read: "+count);
                }
            }



        } catch (IOException ex) {
            System.out.println("Oops ! File not found. Please check if the -in file or -out directory is correct");
            ex.printStackTrace();
            System.exit(0);
        }
        return new Pair<Integer, Integer>(count-1, listOfMolecules.size());

    }

    @Override
    public IAtomContainer checkMolecule(IAtomContainer molecule) {

        MoleculeChecker mc = new MoleculeChecker(molecule);
        molecule = mc.checkMolecule();


        return molecule;
    }

    @Override
    public ArrayList<IAtomContainer> returnCorrectMolecules() {

        return this.listOfMolecules;
    }
}
