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






    public static void main(String[] args){



        SpringApplication.run(NPdatabaseFillerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println("Code version from 18 december 2018");

        if (args.length > 0) {


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





            System.out.println("Happy exit");


        }
        else{
            System.out.println("Please, specify the path to file containing directions to molecular files!");
        }

        exit(0);

    }
}
