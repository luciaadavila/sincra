package com.example.sincra.utils;

public class FaseCicloUtils {
    public static FaseCiclo calcoloFase(int giornoCiclo, int durataPeriodo, int durataTotale){
        if (giornoCiclo <= 0 || durataTotale <= 0){
            return null;
        }

        if (giornoCiclo <= durataPeriodo){
            return FaseCiclo.MESTRUALE;
        }

        int giornoOvulazione = durataTotale-14;

        if (giornoOvulazione < durataPeriodo + 1){
            giornoOvulazione = durataPeriodo + 1;
        }

        int inizioFaseOvulatoria = giornoOvulazione - 2;
        int fineFaseOvulatoria = giornoOvulazione + 1;

        if (giornoCiclo >= inizioFaseOvulatoria && giornoCiclo <= fineFaseOvulatoria){
            return FaseCiclo.OVULATORIA;
        }

        if (giornoCiclo < inizioFaseOvulatoria){
            return FaseCiclo.FOLLICOLARE;
        }

        return FaseCiclo.LUTEALE;
    }
}
