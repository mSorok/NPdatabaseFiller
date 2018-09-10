package de.unijena.cheminf.npdatabasefiller;


import de.unijena.cheminf.npdatabasefiller.services.*;
import de.unijena.cheminf.npdatabasefiller.readers.IReaderService;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

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

    @Autowired NPLScorer nplScorer;






    public static void main(String[] args){


        SpringApplication.run(NPdatabaseFillerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        if (args.length > 0) {
            String locationFile = args[0];


            System.out.println(locationFile);
            readerService.iAmAlive();
            readerService.readLocationFile(locationFile);


            HashSet<String> sources = readerService.ReadMolecularFilesAndInsertInDatabase();


            if (sources.contains("ZINC")) {
                zincCurationService.doWork();
            }


            // on molecules from OriMolecule table, launch the molecule unifier service

            moleculeUnificationService.doWork();


            fragmentsCalculatorService.doWork(10);


            fragmentFrequencyCalculatorService.doWork();


            nplScorer.doWork();


            System.out.println("Happy exit");



        }
        else{
            System.out.println("Please, specify the path to file containing directions to molecular files!");
        }

        //exit(0);

    }
}
