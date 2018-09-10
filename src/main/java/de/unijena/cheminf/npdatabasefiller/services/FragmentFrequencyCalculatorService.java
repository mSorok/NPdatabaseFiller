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





        int height = 2;

            System.out.println("Computing frequencies for fragments from complete molecules");
            for (FragmentWithSugar sugarFragment : sugarRepository.findAllByHeight(height)){


                Object obj3 = cpdRepository.countDistinctNPMoleculesByFragmentIdAndHeightAndAndComputedWithSugar( sugarFragment.getFragment_id(), height, 1 ).get(0);
                Integer npWithFragment = Integer.parseInt(obj3.toString());

                Object obj4 = cpdRepository.countDistinctSMMoleculesByFragmentIdAndHeightAndAndComputedWithSugar( sugarFragment.getFragment_id(), height, 1 ).get(0);
                Integer smWithFragment = Integer.parseInt(obj4.toString());

                double scoreNPf = Math.log( ((double)npWithFragment/ smWithFragment  ) * tot_sm_np   );


                double scoreSMf = Math.log( ((double)smWithFragment/ npWithFragment  ) * tot_np_sm   );



                if( !Double.isNaN(scoreNPf) && !Double.isInfinite(scoreNPf) ){
                    sugarFragment.setScoreNP( scoreNPf);
                }
                else{
                    sugarFragment.setScoreNP(0.0);
                }

                if( !Double.isNaN(scoreSMf) && !Double.isInfinite(scoreSMf)){
                    sugarFragment.setScoreSM( scoreSMf);
                }
                else{
                    sugarFragment.setScoreSM(0.0);
                }

                sugarRepository.save(sugarFragment);




            }


            System.out.println("Computing frequencies for fragments from sugar-free molecules");
            for (FragmentWithoutSugar sugarFreeFragment : sugarFreeRepository.findAllByHeight(height)){


                Object obj5 = cpdRepository.countDistinctNPMoleculesByFragmentIdAndHeightAndAndComputedWithSugar( sugarFreeFragment.getFragment_id(), height, 0 ).get(0);
                Integer npWithFragment =Integer.parseInt(obj5.toString()) ;


                Object obj6 = cpdRepository.countDistinctSMMoleculesByFragmentIdAndHeightAndAndComputedWithSugar( sugarFreeFragment.getFragment_id(), height, 0 ).get(0);
                Integer smWithFragment = Integer.parseInt(obj6.toString());

                double scoreNPf = Math.log( ((double)npWithFragment/ smWithFragment  ) * tot_sm_np   );
                double scoreSMf = Math.log( ((double)smWithFragment/ npWithFragment  ) * tot_np_sm   );

                if( !Double.isNaN(scoreNPf) && !Double.isInfinite(scoreNPf) ){
                    sugarFreeFragment.setScoreNP( scoreNPf);
                }
                else{
                    sugarFreeFragment.setScoreNP(0.0);
                }

                if( !Double.isNaN(scoreSMf)  && !Double.isInfinite(scoreSMf )){
                    sugarFreeFragment.setScoreSM( scoreSMf);
                }
                else{
                    sugarFreeFragment.setScoreSM(0.0);
                }

                sugarFreeRepository.save(sugarFreeFragment);

            }







    }
}
