/*
 * Copyright (c) 2019.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.unijena.cheminf.npdatabasefiller.readers;



import de.unijena.cheminf.npdatabasefiller.misc.BeanUtil;
import de.unijena.cheminf.npdatabasefiller.misc.MoleculeChecker;
import de.unijena.cheminf.npdatabasefiller.model.Molecule;
import de.unijena.cheminf.npdatabasefiller.model.OriMoleculeRepository;
import de.unijena.cheminf.npdatabasefiller.services.AtomContainerToOriMoleculeService;
import net.sf.jniinchi.INCHI_OPTION;
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
import java.util.List;
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


        SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Unique); //Unique - canonical SMILES string, different atom ordering produces the same* SMILES. No isotope or stereochemistry encoded.


        this.file = file;
        int count = 1;


        try {
            reader = new IteratingSDFReader(new FileInputStream(file), DefaultChemObjectBuilder.getInstance());
            System.out.println("SDF reader creation and inserting in database");
            reader.setSkip(true);




            while (reader.hasNext() && count <= 400000) {
                try {
                    IAtomContainer molecule = reader.next();

                    molecule.setProperty("MOL_NUMBER_IN_FILE", Integer.toString(count));
                    molecule.setProperty("FILE_ORIGIN", file.getName().replace(".sdf", ""));

                    molecule.setProperty("DATABASE", source);
                    molecule.setProperty("MOL_STATUS", moleculeStatus);

                    molecule = moleculeChecker.checkMolecule(molecule);

                        if (molecule != null) {

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
                                        //System.out.println(b.getOrder());
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


                            if(!moleculeChecker.isForbiddenMolecule(molecule)){
                                oriMoleculeRepository.save(ac2om.createMolInstance(molecule));
                            }
                        }



                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
                count++;
                //System.out.println(count);

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
