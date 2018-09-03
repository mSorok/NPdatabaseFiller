package de.unijena.cheminf.npdatabasefiller.services;

import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.ArrayList;

public class ZincCurationService {

    private ArrayList<IAtomContainer> allMol;
    private ArrayList<IAtomContainer> curatedMol;

    private ArrayList<IAtomContainer> np;
    private ArrayList<IAtomContainer> allm;
    private ArrayList<IAtomContainer> biogenic;

    public ZincCurationService(ArrayList<IAtomContainer> allMol){
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
