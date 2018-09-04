package de.unijena.cheminf.npdatabasefiller;

//import de.unijena.cheminf.npdatabasefiller.services.IOriMoleculeService;
//import org.springframework.beans.factory.annotation.Autowired;
import de.unijena.cheminf.npdatabasefiller.model.OriMolecule;
import de.unijena.cheminf.npdatabasefiller.model.OriMoleculeRepository;
import de.unijena.cheminf.npdatabasefiller.services.AtomContainerToOriMoleculeService;
import de.unijena.cheminf.npdatabasefiller.readers.IReaderService;
import de.unijena.cheminf.npdatabasefiller.services.MoleculeUnificationService;
import de.unijena.cheminf.npdatabasefiller.services.ZincCurationService;
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

    String locationFile = Paths.get("").toAbsolutePath().toString() + "/molecular_file_locations.txt";


    @Autowired
    IReaderService readerService;


    @Autowired
    ZincCurationService zincCurationService;

    @Autowired
    MoleculeUnificationService moleculeUnificationService;






    public static void main(String[] args){


        SpringApplication.run(NPdatabaseFillerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println(locationFile);


        readerService.readLocationFile(locationFile);

        HashSet<String> sources =  readerService.ReadMolecularFilesAndInsertInDatabase();


        if(sources.contains("ZINC")){
            //run a ZINC curation service on database
            zincCurationService.doWork();
        }


        // on molecules from OriMolecule table, launch the molecule unifier service

        moleculeUnificationService.doWork();





        // OLD
        //readerService.readMolecularFiles();

        //ArrayList<IAtomContainer> moleculesToUpload = readerService.returnCorrectMolecules();

        //System.out.println("Molecules prepared. Starting uploading in the database");


        //for(IAtomContainer ac : moleculesToUpload){
           // oriMoleculeRepository.save(ac2om.createMolInstance(ac));
        //}
        System.out.println("Happy exit");


        exit(0);

    }
}
