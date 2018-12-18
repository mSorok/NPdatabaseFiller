package de.unijena.cheminf.npdatabasefiller.readers;


import de.unijena.cheminf.npdatabasefiller.misc.BeanUtil;
import de.unijena.cheminf.npdatabasefiller.model.OriMoleculeRepository;
import de.unijena.cheminf.npdatabasefiller.services.AtomContainerToOriMoleculeService;
import net.sf.jniinchi.INCHI_OPTION;
import org.javatuples.Pair;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import de.unijena.cheminf.npdatabasefiller.misc.MoleculeChecker;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.springframework.beans.factory.annotation.Autowired;


public class MOLReader implements Reader {

    File file;
    ArrayList<IAtomContainer> listOfMolecules;

    private IteratingSDFReader reader = null;
    private SDFWriter moleculeWithScoreWriter = null;

    OriMoleculeRepository oriMoleculeRepository;

    AtomContainerToOriMoleculeService ac2om;

    MoleculeChecker moleculeChecker;



    public MOLReader(){
        this.listOfMolecules = new ArrayList<IAtomContainer>();
        oriMoleculeRepository = BeanUtil.getBean(OriMoleculeRepository.class);
        ac2om = BeanUtil.getBean(AtomContainerToOriMoleculeService.class);
        moleculeChecker = BeanUtil.getBean(MoleculeChecker.class);

    }




    @Override
    public Pair readFile(File file) {

        this.file = file;
        int count = 1;

        System.out.println("\n\n Working on the MOL file: "+this.file.getName() + "\n\n");

        //Read the only molecule here

        try {
            reader = new IteratingSDFReader(new FileInputStream(file), DefaultChemObjectBuilder.getInstance());
            System.out.println("MOL reader creation");
            reader.setSkip(true);


            //even for only one molecule needs the iterator
            while (reader.hasNext()) {
                try {
                    IAtomContainer molecule = reader.next();

                    molecule.setProperty("MOL_NUMBER_IN_FILE",  Integer.toString(count) );
                    molecule.setProperty("FILE_ORIGIN", file.getName().replace(".mol", ""));


                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                    LocalDate localDate = LocalDate.now();

                    molecule.setProperty("ACQUISITION_DATE", dtf.format(localDate));

                    molecule = moleculeChecker.checkMolecule(molecule);
                    if(molecule != null) {

                        listOfMolecules.add(molecule);
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                count++;
            }



        } catch (IOException ex) {
            System.out.println("Oops ! File not found. Please check if the -in file or -out directory is correct");
            ex.printStackTrace();
            System.exit(0);
        }
        return new Pair<Integer, Integer>(count-1, listOfMolecules.size());

    }

    @Override
    public void readFileAndInsertInDB(File file, String source, String moleculeStatus) {
        this.file = file;


        SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Unique );

        System.out.println("\n\n Working on the MOL file: "+this.file.getName() + "\n\n");

        //Read the only molecule here

        try {
            reader = new IteratingSDFReader(new FileInputStream(file), DefaultChemObjectBuilder.getInstance());
            System.out.println("MOL reader creation");
            reader.setSkip(true);


            //even for only one molecule needs the iterator
            while (reader.hasNext()) {
                try {
                    IAtomContainer molecule = reader.next();

                    molecule.setProperty("MOL_NUMBER_IN_FILE", Integer.toString(1));
                    molecule.setProperty("FILE_ORIGIN", file.getName().replace(".mol", ""));

                    molecule.setID(file.getName().replace(".mol", ""));

                    molecule.setProperty("DATABASE", source);
                    molecule.setProperty("MOL_STATUS", moleculeStatus);

                    molecule = moleculeChecker.checkMolecule(molecule);
                    if(molecule != null) {


                        try {
                            List options = new ArrayList();
                            options.add(INCHI_OPTION.SNon);
                            options.add(INCHI_OPTION.ChiralFlagOFF);
                            options.add(INCHI_OPTION.AuxNone);
                            InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule, options );

                            molecule.setProperty("INCHI", gen.getInchi());

                            molecule.setProperty("INCHIKEY", gen.getInchiKey());



                        } catch (CDKException e) {
                            Integer totalBonds = molecule.getBondCount();
                            Integer ib = 0;
                            while (ib < totalBonds) {

                                IBond b = molecule.getBond(ib);
                                if (b.getOrder() == IBond.Order.UNSET) {
                                    b.setOrder(IBond.Order.SINGLE);

                                }

                                ib++;
                            }
                            List options = new ArrayList();
                            options.add(INCHI_OPTION.SNon);
                            options.add(INCHI_OPTION.ChiralFlagOFF);
                            options.add(INCHI_OPTION.AuxNone);
                            InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule, options );

                            molecule.setProperty("INCHI", gen.getInchi());
                            molecule.setProperty("INCHIKEY", gen.getInchiKey());
                        }

                        molecule.setProperty("SMILES", smilesGenerator.create(molecule));

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                        LocalDate localDate = LocalDate.now();

                        molecule.setProperty("ACQUISITION_DATE", dtf.format(localDate));


                        oriMoleculeRepository.save(ac2om.createMolInstance(molecule));
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }



        } catch (IOException ex) {
            System.out.println("Oops ! File not found. Please check if the -in file or -out directory is correct");
            ex.printStackTrace();
            System.exit(0);
        }
    }



    @Override
    public ArrayList<IAtomContainer> returnCorrectMolecules() {
        return this.listOfMolecules;
    }
}
