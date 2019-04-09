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

package de.unijena.cheminf.npdatabasefiller;


import de.unijena.cheminf.npdatabasefiller.readers.IReaderService;
import de.unijena.cheminf.npdatabasefiller.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashSet;

import static java.lang.System.exit;

/**
 * @author mSorok
 *
 * Launches reading of molecules from multiple sources
 * Fills the MySQL database
 * Trains the NP-likeness scorer on the data
 * Computes the NP-likeness score for the whole dataset
 */


@SpringBootApplication(scanBasePackages={"de.unijena.cheminf.npdatabasefiller"})
public class NPdatabaseFillerApplication implements CommandLineRunner {

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

    @Autowired
    NPLScorer nplScorer;

    @Autowired
    DataSetUploaderService dataSetUploaderService;


    public static void main(String[] args){
        SpringApplication.run(NPdatabaseFillerApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {

        System.out.println("Code version from 09 April 2019");

        if (args.length > 0) {

            if(args[args.length-1].equals("fromScratch") || (args[args.length-1].equals(args[0]) && !args[0].equals("updateScores")) ){


                String locationFile = args[0];
                System.out.println("Reading information from "+locationFile);
                readerService.readLocationFile(locationFile);
                HashSet<String> sources = readerService.readMolecularFilesAndInsertInDatabase();

                if (sources.contains("ZINC")) {
                    zincCurationService.doWork();
                }

                moleculeUnificationService.doWork();

                fragmentsCalculatorService.doWork();

                fragmentFrequencyCalculatorService.doWork();

                nplScorer.doWork(true);
                nplScorer.doWork(false);

                moleculeUnificationService.computeAdditionalMolecularFeatures();


            }
            else if(args[args.length-1].equals("addNewData")){
                //adds new data from source in args[0]
                dataSetUploaderService.readNewDataset(args[0], args[1], args[2]);

                dataSetUploaderService.addNewDatasetToMolecule();

                dataSetUploaderService.calculateFragmentsForNewMolecules();

                dataSetUploaderService.calculateNplsForNewMolecules();

                moleculeUnificationService.computeAdditionalMolecularFeatures(args[1], args[2]);

            }
            else if(args[args.length-1].equals("updateScores")){
                //updates the NPLS scores of all molecules in the database

                fragmentFrequencyCalculatorService.doWork();

                nplScorer.doWork(true);
                nplScorer.doWork(false);

                moleculeUnificationService.computeAdditionalMolecularFeatures();
            }
            System.out.println("Exit");
        }
        else{
            System.out.println("Please, specify the path to file containing directions to molecular files!");
        }
        exit(0);

    }
}
