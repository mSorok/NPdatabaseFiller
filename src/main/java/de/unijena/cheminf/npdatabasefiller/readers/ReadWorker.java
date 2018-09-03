package de.unijena.cheminf.npdatabasefiller.readers;


import org.javatuples.Pair;
import org.openscience.cdk.interfaces.IAtomContainer;


import java.io.File;
import java.util.ArrayList;

public class ReadWorker {

    private File fileToRead;
    private boolean acceptFileFormat = false;
    private String submittedFileFormat ;


    private ArrayList<IAtomContainer> molecules ;



    private Pair<Integer, Integer> fileStats ; //0=total number of molecules in file, 1=number of valid molecules to be integrated




    public ReadWorker(String fileName){

        this.fileToRead = new File(fileName);

        //System.out.println("\n\n Working on: "+fileToRead.getName() + "\n\n");
        System.out.println("\n\n Working on: "+fileToRead.getAbsolutePath() + "\n\n");


        acceptFileFormat = acceptFile(fileName);


    }


    public boolean startWorker(){


        if(acceptFileFormat){
            //this.molecules = doWork();
            return true;
        }
        else{
            return false;
        }

    }







    private boolean acceptFile(String filename) {
        filename = filename.toLowerCase();
        if (filename.endsWith("sdf") || filename.toLowerCase().contains("sdf".toLowerCase())) {
            this.submittedFileFormat="sdf";
            return true;
        } else if (filename.endsWith("smi")  ||
                filename.toLowerCase().contains("smi".toLowerCase()) ||
                filename.toLowerCase().contains("smiles".toLowerCase()) ||
                filename.toLowerCase().contains("smile".toLowerCase())) {
            this.submittedFileFormat="smi";
            return true;
        } else if (filename.endsWith("json")) {
            return false;
        }
        else if (filename.endsWith("mol")  ||
                filename.toLowerCase().contains("mol".toLowerCase())
                || filename.toLowerCase().contains("molfile".toLowerCase())) {
            this.submittedFileFormat="mol";
            return true;
        }


        return false;
    }



    public ArrayList<IAtomContainer> doWork(){

        Reader reader = null ;

        if(this.submittedFileFormat.equals("mol")){
            reader = new MOLReader();
        }
        else if(this.submittedFileFormat.equals("sdf")){
            reader = new SDFReader();
        }
        else if(this.submittedFileFormat.equals("smi")){
            reader = new SMILESReader();
        }

        this.fileStats = reader.readFile(this.fileToRead);
        this.molecules = reader.returnCorrectMolecules();

        return molecules;
    }



    public Pair<Integer, Integer> getFileStats() {
        return fileStats;
    }




}
