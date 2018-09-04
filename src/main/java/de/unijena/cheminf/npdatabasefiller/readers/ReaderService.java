package de.unijena.cheminf.npdatabasefiller.readers;

import de.unijena.cheminf.npdatabasefiller.services.ZincCurationFromList;
import de.unijena.cheminf.npdatabasefiller.services.ZincCurationService;
import org.javatuples.Triplet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

import static java.lang.System.exit;


@Service
public class ReaderService implements IReaderService {

    private ArrayList <Triplet< String, String, String>> listOfMolecularFiles ;
    public ArrayList<IAtomContainer> totalMolecules;



    @Override
    public void readLocationFile(String fileName) {

        this.listOfMolecularFiles = new ArrayList <Triplet< String, String, String>>();



        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            String line;

            while ((line = br.readLine()) != null) {

                String [] tab = line.split("\t");
                this.listOfMolecularFiles. add( new Triplet<String, String, String>(tab[0], tab[1], tab[2]) );


            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(this.listOfMolecularFiles.isEmpty()){
            System.out.println("No molecular files found");
            exit(0);
        }
        System.out.println(this.listOfMolecularFiles);
    }

    @Override
    public void readMolecularFiles() {


        this.totalMolecules = new ArrayList<IAtomContainer>();

        HashSet<String> totalDatabases = new HashSet<String>();
        ArrayList<IAtomContainer> dbMol = null;

        ArrayList<IAtomContainer> zincMol = null;





        for(Triplet<String, String, String> fi : this.listOfMolecularFiles){

            // fi.getValue0 = filename
            String database = fi.getValue1();
            String moleculeStatus = fi.getValue2();
            totalDatabases.add(database);

            ReadWorker rw = new ReadWorker(fi.getValue0());

            boolean start = rw.startWorker();

             if(start){
                 dbMol =  rw.doWork();
                 for(IAtomContainer mol : dbMol){
                     //System.out.println(mol.toString());
                     mol.setProperty("DATABASE", database);
                     mol.setProperty("MOL_STATUS", moleculeStatus);
                     //System.out.println(mol.getProperties());
                     //System.out.println(mol.getID());

                     //compute InChi here
                     try {
                         InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(mol);

                         mol.setProperty("INCHI", gen.getInchi());
                     } catch (CDKException e) {
                         e.printStackTrace();
                     }


                 }
             }

        }



        if(totalDatabases.contains("ZINC")){

            ZincCurationFromList zc = new ZincCurationFromList(dbMol);

            //start ZincCurationService
            // pass to it all the molecules

            ArrayList<IAtomContainer> zincMols = zc.doWork();
            if(!zincMols.isEmpty()) {
                this.totalMolecules.addAll( zincMols);
                // it will return an updated list of molecules (with SM and NP from ZINC only)
            }

            // put them in "totalMolecules"
        }

        // put all "non-ZINC" molecules in "totalMolecules"
        for(IAtomContainer mol : dbMol){
            if(!mol.getProperty("DATABASE").equals("ZINC")){
                this.totalMolecules.add(mol);
            }
        }



    }

    @Override
    public HashSet<String> ReadMolecularFilesAndInsertInDatabase(){

        HashSet<String> totalDatabases = new HashSet<String>();

        for(Triplet<String, String, String> fi : this.listOfMolecularFiles){


            String database = fi.getValue1();
            String moleculeStatus = fi.getValue2();
            totalDatabases.add(database);


            ReadWorker rw = new ReadWorker(fi.getValue0(), fi.getValue1(), fi.getValue2());

            boolean start = rw.startWorker();

            if(start){
                rw.doWorkWithInsertionInDB();
            }
        }

        return totalDatabases;
    }






    @Override
    public ArrayList<IAtomContainer> returnCorrectMolecules() {
        return this.totalMolecules;
    }

    @Override
    public void iAmAlive() {
        System.out.println("I am alive!");
    }
}