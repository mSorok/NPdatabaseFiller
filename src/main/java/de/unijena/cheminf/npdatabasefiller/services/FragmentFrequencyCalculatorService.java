package de.unijena.cheminf.npdatabasefiller.services;


import de.unijena.cheminf.npdatabasefiller.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class FragmentFrequencyCalculatorService {

    @Autowired
    FragmentWithSugarRepository sugarRepository;

    @Autowired
    FragmentWithoutSugarRepository sugarFreeRepository;

    @Autowired
    MoleculeFragmentCpdRepository cpdRepository;

    public void doWork(){

        System.out.println("Started computing fragment frequencies ");


        Object obj1 = cpdRepository.countTotalNPMolecules().get(0);
        Integer totalNumberNP = Integer.parseInt(obj1.toString());

        Object obj2 = cpdRepository.countTotalSMMolecules().get(0);
        Integer totalNumberSM = Integer.parseInt(obj2.toString());



        double tot_sm_np = (double)totalNumberSM/totalNumberNP;
        double tot_np_sm = (double)totalNumberNP/totalNumberSM;


        System.out.println("tot_sm_np "+tot_sm_np);






        int height = 2;

        System.out.println("Computing frequencies for fragments from complete molecules (with sugar)");
        for (FragmentWithSugar sugarFragment : sugarRepository.findAllByHeight(height)){


            Object obj3 = cpdRepository.countTotalOccurenciesInNPMoleculesByFragmentIdAndHeightAndComputedWithSugar( sugarFragment.getFragment_id(), height, 1 ).get(0);
            Object obj4 = cpdRepository.countTotalOccurenciesInSMMoleculesByFragmentIdAndHeightAndComputedWithSugar( sugarFragment.getFragment_id(), height, 1 ).get(0);

            if(obj3 != null && obj4 != null) {
                Integer npWithFragment = Integer.parseInt(obj3.toString());
                Integer smWithFragment = Integer.parseInt(obj4.toString());
                if (npWithFragment == 0 || smWithFragment == 0) {

                    // the fragment is only in NP or in SM


                    if(npWithFragment==0){
                        //the fragment is only in SM
                        sugarFragment.setScoreNP(0.0);
                        sugarFragment.setScoreSM(1.0);

                    }
                    else if(smWithFragment==0){
                        //the fragment is only in NP
                        sugarFragment.setScoreNP(1.0);
                        sugarFragment.setScoreSM(0.0);
                    }

                }
                else{

                    double scoreNPf = Math.log(((double) npWithFragment / (double)smWithFragment) * tot_sm_np);
                    double scoreSMf = Math.log(((double) smWithFragment / (double)npWithFragment) * tot_np_sm);
                    if (!Double.isNaN(scoreNPf) && !Double.isInfinite(scoreNPf)) {
                        sugarFragment.setScoreNP(scoreNPf);
                    } else {
                        sugarFragment.setScoreNP(0.0);
                    }

                    if (!Double.isNaN(scoreSMf) && !Double.isInfinite(scoreSMf)) {
                        sugarFragment.setScoreSM(scoreSMf);
                    } else {
                        sugarFragment.setScoreSM(0.0);
                    }
                }

            }
            else{

                if(obj3 == null){
                    // not in NP
                    sugarFragment.setScoreNP(0.0);
                    sugarFragment.setScoreSM(1.0);
                }
                else if(obj4 == null) {
                    // not in SM
                    sugarFragment.setScoreNP(1.0);
                    sugarFragment.setScoreSM(0.0);
                }
                else{
                    sugarFragment.setScoreNP(0.0);
                    sugarFragment.setScoreSM(0.0);
                }

            }




            sugarRepository.save(sugarFragment);




        }


        System.out.println("Computing frequencies for fragments from sugar-free molecules");
        for (FragmentWithoutSugar sugarFreeFragment : sugarFreeRepository.findAllByHeight(height)){


            Object obj5 = cpdRepository.countTotalOccurenciesInNPMoleculesByFragmentIdAndHeightAndComputedWithSugar( sugarFreeFragment.getFragment_id(), height, 0 ).get(0);
            Object obj6 = cpdRepository.countTotalOccurenciesInSMMoleculesByFragmentIdAndHeightAndComputedWithSugar( sugarFreeFragment.getFragment_id(), height, 0 ).get(0);


            if(obj5 != null && obj6 != null) {
                Integer npWithFragment = Integer.parseInt(obj5.toString());
                Integer smWithFragment = Integer.parseInt(obj6.toString());

                if (npWithFragment == 0 || smWithFragment == 0) {
                    // the fragment is only in NP or in SM


                    //npWithFragment = npWithFragment + 1;
                    //smWithFragment = smWithFragment + 1;

                    if (npWithFragment == 0) {
                        //the fragment is only in SM
                        sugarFreeFragment.setScoreNP(0.0);
                        sugarFreeFragment.setScoreSM(1.0);

                    } else if (smWithFragment == 0) {
                        //the fragment is only in NP
                        sugarFreeFragment.setScoreNP(1.0);
                        sugarFreeFragment.setScoreSM(0.0);
                    }
                } else {

                    double scoreNPf = Math.log(((double) npWithFragment / smWithFragment) * tot_sm_np);
                    double scoreSMf = Math.log(((double) smWithFragment / npWithFragment) * tot_np_sm);

                    if (!Double.isNaN(scoreNPf) && !Double.isInfinite(scoreNPf)) {
                        sugarFreeFragment.setScoreNP(scoreNPf);
                    } else {
                        sugarFreeFragment.setScoreNP(0.0);
                    }

                    if (!Double.isNaN(scoreSMf) && !Double.isInfinite(scoreSMf)) {
                        sugarFreeFragment.setScoreSM(scoreSMf);
                    } else {
                        sugarFreeFragment.setScoreSM(0.0);
                    }
                }
            }

            else{
                if(obj5 == null){
                    // not in NP
                    sugarFreeFragment.setScoreNP(0.0);
                    sugarFreeFragment.setScoreSM(1.0);
                }
                else if(obj6 == null) {
                    // not in SM
                    sugarFreeFragment.setScoreNP(1.0);
                    sugarFreeFragment.setScoreSM(0.0);
                }
                else{
                    sugarFreeFragment.setScoreNP(0.0);
                    sugarFreeFragment.setScoreSM(0.0);
                }

            }





            sugarFreeRepository.save(sugarFreeFragment);

        }







    }
}
