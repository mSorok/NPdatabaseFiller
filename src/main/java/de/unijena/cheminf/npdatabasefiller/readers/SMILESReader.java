package de.unijena.cheminf.npdatabasefiller.readers;


import de.unijena.cheminf.npdatabasefiller.misc.BeanUtil;
import de.unijena.cheminf.npdatabasefiller.misc.MoleculeChecker;
import de.unijena.cheminf.npdatabasefiller.model.OriMoleculeRepository;
import de.unijena.cheminf.npdatabasefiller.services.AtomContainerToOriMoleculeService;
import org.javatuples.Pair;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

public class SMILESReader implements Reader {

    File file;
    ArrayList<IAtomContainer> listOfMolecules;

    private LineNumberReader smilesReader;

    MoleculeChecker moleculeChecker;



    public SMILESReader(){
        this.listOfMolecules = new ArrayList<IAtomContainer>();
        oriMoleculeRepository = BeanUtil.getBean(OriMoleculeRepository.class);
        ac2om = BeanUtil.getBean(AtomContainerToOriMoleculeService.class);
        moleculeChecker = BeanUtil.getBean(MoleculeChecker.class);

    }

    OriMoleculeRepository oriMoleculeRepository;

    AtomContainerToOriMoleculeService ac2om;



    @Override
    public Pair readFile(File file) {

        this.file = file;
        int count = 1;
        String line;

        //System.out.println("\n\n Working on: "+this.file.getName() + "\n\n");
        //System.out.println("\n\n Working on: "+this.file.getAbsolutePath() + "\n\n");



        try {
            smilesReader = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
            System.out.println("SMILES reader creation");


            while ((line = smilesReader.readLine()) != null) {
                String smiles_names = line;
                if(!line.contains("smiles")) {
                    try {
                        String[] splitted = smiles_names.split("\\s+"); //splitting the canonical smiles format: SMILES \s mol name
                        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

                        IAtomContainer molecule = null;
                        try {
                            molecule = sp.parseSmiles(splitted[0]);
                            Map properties = molecule.getProperties();

                            //List<IAtomContainer> fragments = score(molecule);

                            molecule.setProperty("MOL_NUMBER_IN_FILE", Integer.toString(count));
                            molecule.setProperty("ID", splitted[1]);
                            molecule.setID(splitted[1]);
                            molecule = moleculeChecker.checkMolecule(molecule);

                            if (molecule != null){


                                listOfMolecules.add(molecule);

                            }
                        } catch (InvalidSmilesException e) {
                            e.printStackTrace();
                            smilesReader.skip(count - 1);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    count++;
                }


            }
            smilesReader.close();



        } catch (IOException ex) {
            System.out.println("Oops ! File not found. Please check if the -in file or -out directory is correct");
            ex.printStackTrace();
            System.exit(0);
        }
        return new Pair<Integer, Integer>(count-1, listOfMolecules.size());

    }

    @Override
    public void readFileAndInsertInDB(File file, String source, String moleculeStatus) {

        SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Absolute |
                SmiFlavor.UseAromaticSymbols);


        this.file = file;
        int count = 1;
        String line;

        try {
            smilesReader = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
            System.out.println("SMILES reader creation");


            while ((line = smilesReader.readLine()) != null  && count <= 500000) {
                String smiles_names = line;
                if(!line.contains("smiles")) {
                    try {
                        String[] splitted = smiles_names.split("\\s+"); //splitting the canonical smiles format: SMILES \s mol name
                        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

                        IAtomContainer molecule = null;
                        try {
                            molecule = sp.parseSmiles(splitted[0]);


                            molecule.setProperty("MOL_NUMBER_IN_FILE", Integer.toString(count));
                            molecule.setProperty("ID", splitted[1]);
                            molecule.setID(splitted[1]);

                            molecule.setProperty("FILE_ORIGIN", file.getName().replace(".sdf", ""));

                            molecule.setProperty("DATABASE", source);
                            molecule.setProperty("MOL_STATUS", moleculeStatus);
                            molecule = moleculeChecker.checkMolecule(molecule);

                            if (molecule != null){
                                try {
                                    InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule);

                                    molecule.setProperty("INCHI", gen.getInchi());
                                    molecule.setProperty("INCHIKEY", gen.getInchiKey());
                                } catch (CDKException e) {
                                    Integer totalBonds = molecule.getBondCount();
                                    Integer ib = 0;
                                    while (ib < totalBonds) {

                                        IBond b = molecule.getBond(ib);
                                        if (b.getOrder() == IBond.Order.UNSET) {
                                            //System.out.println(b.getOrder());
                                            b.setOrder(IBond.Order.SINGLE);

                                            //System.out.println(b.getOrder());

                                            //System.out.println(molecule.getBond(ib).getOrder());
                                        }

                                        ib++;
                                    }
                                    InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule);

                                    molecule.setProperty("INCHI", gen.getInchi());
                                    molecule.setProperty("INCHIKEY", gen.getInchiKey());
                                }

                                molecule.setProperty("SMILES", smilesGenerator.create(molecule));


                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                                LocalDate localDate = LocalDate.now();

                                molecule.setProperty("ACQUISITION_DATE", dtf.format(localDate));


                                oriMoleculeRepository.save(ac2om.createMolInstance(molecule));
                            }




                        } catch (InvalidSmilesException e) {
                            e.printStackTrace();
                            smilesReader.skip(count - 1);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    count++;

                    if(count%50000==0){
                        System.out.println("Molecules read: "+count);
                    }
                }


            }
            smilesReader.close();



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
