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

import de.unijena.cheminf.npdatabasefiller.model.IMolecule;
import de.unijena.cheminf.npdatabasefiller.model.Molecule;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.springframework.stereotype.Service;

/**
 * @author mSorok
 * Transforms a IAtomContainer in Molecule object
 */

@Service
public class AtomContainerToMoleculeService implements AtomContainerToMolInstanceService {
    @Override
    public Molecule createMolInstance(IAtomContainer ac) {
        Molecule m = new Molecule();

        m.setInchi(ac.getProperty("INCHI"));
        m.setInchikey(ac.getProperty("INCHIKEY"));
        m.setSmiles(ac.getProperty("SMILES"));
        m.setTotal_atom_number(ac.getAtomCount());
        int heavyAtomCount = 0;
        for(IAtom a : ac.atoms()){
            if(!a.getSymbol().equals("H")){
                heavyAtomCount=heavyAtomCount+1;
            }
        }
        m.setHeavy_atom_number(heavyAtomCount);
        return m;
    }


    public IAtomContainer createAtomContainer(Molecule m) {
        IAtomContainer ac = null;

        try {
            SmilesParser sp  = new SmilesParser(SilentChemObjectBuilder.getInstance());
            ac   = sp.parseSmiles( m.getSmiles() );
        } catch (InvalidSmilesException e) {
            System.err.println(e.getMessage());
        }

        return ac;
    }


    @Override
    public IAtomContainer createAtomContainer(IMolecule m) {
        return null;
    }
}
