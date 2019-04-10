package de.unijena.cheminf.npdatabasefiller.services;

import org.paukov.combinatorics3.Generator;
import org.springframework.stereotype.Service;


@Service
public class DependingFragmentFrequencyCalculatorService {
    /**
     * Unfinished service
     * @param withSugar
     */

    public void doWork(boolean withSugar){

        if(withSugar){
            //TODO fragments with sugar

            Generator.combination("red", "black", "white", "green", "blue")
                    .simple(2)
                    .stream()
                    .forEach(System.out::println);
        }
        else{
            //TODO - sugar free fragmments
        }




    }
}
