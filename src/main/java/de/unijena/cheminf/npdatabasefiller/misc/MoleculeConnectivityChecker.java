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

package de.unijena.cheminf.npdatabasefiller.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.springframework.stereotype.Service;

/**
 * Class to discard counter ions and other small disconnected fragments
 *
 * @author kalai
 */

@Service
public class MoleculeConnectivityChecker {

    public List<IAtomContainer> checkConnectivity(IAtomContainer molecule) {

        List<IAtomContainer> curated = new ArrayList<IAtomContainer>();
        Map<Object, Object> properties = molecule.getProperties();
        IAtomContainerSet mols = ConnectivityChecker.partitionIntoMolecules(molecule);
        for (int i = 0; i < mols.getAtomContainerCount(); i++) {
            mols.getAtomContainer(i).setProperties(properties);
            curated.add(mols.getAtomContainer(i));
        }
        return curated;
    }
}
