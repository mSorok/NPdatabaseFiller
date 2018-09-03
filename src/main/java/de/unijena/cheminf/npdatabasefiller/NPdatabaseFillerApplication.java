package de.unijena.cheminf.npdatabasefiller;

//import de.unijena.cheminf.npdatabasefiller.services.IOriMoleculeService;
//import org.springframework.beans.factory.annotation.Autowired;
import de.unijena.cheminf.npdatabasefiller.model.OriMolecule;
import de.unijena.cheminf.npdatabasefiller.model.OriMoleculeRepository;
import de.unijena.cheminf.npdatabasefiller.services.AtomContainerToOriMoleculeService;
import de.unijena.cheminf.npdatabasefiller.readers.IReaderService;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.nio.file.Paths;
import java.util.ArrayList;

@SpringBootApplication(scanBasePackages={"de.unijena.cheminf.npdatabasefiller"})
public class NPdatabaseFillerApplication implements CommandLineRunner {

    String locationFile = Paths.get("").toAbsolutePath().toString() + "/molecular_file_locations.txt";


    @Autowired
    IReaderService readerService;



    @Autowired
    OriMoleculeRepository oriMoleculeRepository;

    @Autowired
    AtomContainerToOriMoleculeService ac2om;





    public static void main(String[] args){


        SpringApplication.run(NPdatabaseFillerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println(locationFile);


        readerService.readLocationFile(locationFile);
        readerService.readMolecularFiles();

        ArrayList<IAtomContainer> moleculesToUpload = readerService.returnCorrectMolecules();

        System.out.println("Molecules prepared. Starting uploading in the database");


        for(IAtomContainer ac : moleculesToUpload){
            oriMoleculeRepository.save(ac2om.createMolInstance(ac));
        }
        System.out.println("Happy exit");

    }
}
