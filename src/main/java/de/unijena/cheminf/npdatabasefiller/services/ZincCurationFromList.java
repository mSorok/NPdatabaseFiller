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

package de.unijena.cheminf.npdatabasefiller.services;

import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.ArrayList;


public class ZincCurationFromList {

    private ArrayList<IAtomContainer> allMol;
    private ArrayList<IAtomContainer> curatedMol;

    private ArrayList<IAtomContainer> np;
    private ArrayList<IAtomContainer> allm;
    private ArrayList<IAtomContainer> biogenic;

    public ZincCurationFromList(ArrayList<IAtomContainer> allMol){
        this.allMol = allMol;

        System.out.println("ZINC curation service started");

    }


    public ArrayList<IAtomContainer> doWork(){
        this.curatedMol = new ArrayList<IAtomContainer>();

        np = new ArrayList<IAtomContainer>();
        allm = new ArrayList<IAtomContainer>();
        biogenic = new ArrayList<IAtomContainer>();


        for(IAtomContainer mol : this.allMol){
            if(mol.getProperty("MOL_STATUS").toString().equals("NP") && mol.getProperty("DATABASE").toString().equals("ZINC")){
                np.add(mol);
            }
            else if(mol.getProperty("MOL_STATUS").toString().equals("BIOGENIC") && mol.getProperty("DATABASE").toString().equals("ZINC")){
                biogenic.add(mol);
            }
            else if (mol.getProperty("MOL_STATUS").toString().equals("ALL") && mol.getProperty("DATABASE").toString().equals("ZINC") ){
                allm.add(mol);
            }
        }

        if(!allm.isEmpty()) {

            for(IAtomContainer m : allm) {

                for(IAtomContainer n : np){

                    if( m.getProperty("INCHI").equals(n.getProperty("INCHI"))   ){
                        allm.remove(m);
                    }

                }
                for(IAtomContainer bio : biogenic){

                    if( m.getProperty("INCHI").equals(bio.getProperty("INCHI"))   ){
                        allm.remove(m);
                    }

                }

            }
        }

        for (IAtomContainer m : allm) {
            m.setProperty("MOL_STATUS", "SM");
        }
        this.curatedMol.addAll(allm);

        this.curatedMol.addAll(np);



        //in curatedMol - only molecules annotated as SM or NP
        return this.curatedMol;
    }



}
