package de.unijena.cheminf.npdatabasefiller.readers;



import de.unijena.cheminf.npdatabasefiller.misc.BeanUtil;
import de.unijena.cheminf.npdatabasefiller.misc.MoleculeChecker;
import de.unijena.cheminf.npdatabasefiller.model.Molecule;
import de.unijena.cheminf.npdatabasefiller.model.OriMoleculeRepository;
import de.unijena.cheminf.npdatabasefiller.services.AtomContainerToOriMoleculeService;
import org.javatuples.Pair;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;



public class SDFReader implements Reader {

    File file;
    ArrayList<IAtomContainer> listOfMolecules;

    private IteratingSDFReader reader = null;
    private SDFWriter moleculeWithScoreWriter = null;

    OriMoleculeRepository oriMoleculeRepository;

    AtomContainerToOriMoleculeService ac2om;

    MoleculeChecker moleculeChecker;


    public SDFReader(){

        this.listOfMolecules = new ArrayList<IAtomContainer>();
        oriMoleculeRepository = BeanUtil.getBean(OriMoleculeRepository.class);
        ac2om = BeanUtil.getBean(AtomContainerToOriMoleculeService.class);
        moleculeChecker = BeanUtil.getBean(MoleculeChecker.class);

    }

    @Override
    public Pair readFile(File file) {

        this.file = file;
        int count = 1;

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


                    molecule = moleculeChecker.checkMolecule(molecule);

                    if (molecule != null) {
                        listOfMolecules.add(molecule);
                    }



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
    public void readFileAndInsertInDB(File file, String source, String moleculeStatus) {
        ac2om.iAmAlive();;


        SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Absolute |
                SmiFlavor.UseAromaticSymbols);


        this.file = file;
        int count = 1;


        try {
            reader = new IteratingSDFReader(new FileInputStream(file), DefaultChemObjectBuilder.getInstance());
            System.out.println("SDF reader creation and inserting in database");
            reader.setSkip(true);




            while (reader.hasNext() && count <= 500000) {
                try {
                    IAtomContainer molecule = reader.next();

                    molecule.setProperty("MOL_NUMBER_IN_FILE", Integer.toString(count));
                    molecule.setProperty("FILE_ORIGIN", file.getName().replace(".sdf", ""));

                    molecule.setProperty("DATABASE", source);
                    molecule.setProperty("MOL_STATUS", moleculeStatus);

                    molecule = moleculeChecker.checkMolecule(molecule);

                        if (molecule != null) {

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



                } catch (Exception ex) {
                    //ex.printStackTrace();
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



    }



    @Override
    public ArrayList<IAtomContainer> returnCorrectMolecules() {

        return this.listOfMolecules;
    }
}
