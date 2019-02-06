package de.unijena.cheminf.npdatabasefiller;


import de.unijena.cheminf.npdatabasefiller.services.*;
import de.unijena.cheminf.npdatabasefiller.readers.IReaderService;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import static java.lang.System.exit;

@SpringBootApplication(scanBasePackages={"de.unijena.cheminf.npdatabasefiller"})
public class NPdatabaseFillerApplication implements CommandLineRunner {

    //String locationFile = Paths.get("").toAbsolutePath().toString() + "/molecular_file_locations.txt";


    @Autowired
    IReaderService readerService;


    @Autowired
    ZincCurationService zincCurationService;

    @Autowired
    MoleculeUnificationService moleculeUnificationService;


    @Autowired
    FragmentsCalculatorService fragmentsCalculatorService;


    @Autowired
    FragmentFrequencyCalculatorService fragmentFrequencyCalculatorService;

    @Autowired
    DependingFragmentFrequencyCalculatorService dependingFragmentFrequencyCalculatorService;

    @Autowired NPLScorer nplScorer;

    @Autowired DataSetUploaderService dataSetUploaderService;






    public static void main(String[] args){



        SpringApplication.run(NPdatabaseFillerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println("Code version from 06 February 2019");

        if (args.length > 0) {

            if(args[args.length-1].equals("fromScratch") || args[args.length-1].equals(args[0])) {


                String locationFile = args[0];
                System.out.println(locationFile);
                readerService.readLocationFile(locationFile);
                HashSet<String> sources = readerService.readMolecularFilesAndInsertInDatabase();


                if (sources.contains("ZINC")) {
                    zincCurationService.doWork();
                }

                moleculeUnificationService.doWork();

                fragmentsCalculatorService.doWork();

                fragmentFrequencyCalculatorService.doWork();

                //dependingFragmentFrequencyCalculatorService.doWork(true);
                //dependingFragmentFrequencyCalculatorService.doWork(false);

                nplScorer.doWork(true);
                nplScorer.doWork(false);

                //nplScorer.coputeScoreForCoFrequencies(true);
                //nplScorer.coputeScoreForCoFrequencies(false);

                moleculeUnificationService.computeAdditionalMolecularFeatures();


            }
            else if(args[args.length-1].equals("addNewData")){
                dataSetUploaderService.readNewDataset(args[0], args[1], args[2]);

                dataSetUploaderService.addNewDatasetToMolecule();

                dataSetUploaderService.calculateFragmentsForNewMolecules();

                dataSetUploaderService.calculateNplsForNewMolecules();


                moleculeUnificationService.computeAdditionalMolecularFeatures(args[1], args[2]);

            }
            else if(args[args.length-1].equals("updateScores")){
                //updates the NPLS scores of fragments regarding new frequencies (after adding data)

                //updates the NPLS scores of molecules accordingly

                fragmentFrequencyCalculatorService.doWork();

                //dependingFragmentFrequencyCalculatorService.doWork(true);
                //dependingFragmentFrequencyCalculatorService.doWork(false);

                nplScorer.doWork(true);
                nplScorer.doWork(false);


            }



            System.out.println("Happy exit");


        }
        else{
            System.out.println("Please, specify the path to file containing directions to molecular files!");
        }

        exit(0);

    }
}
