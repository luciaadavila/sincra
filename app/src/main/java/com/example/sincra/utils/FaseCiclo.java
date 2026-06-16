package com.example.sincra.utils;

import com.example.sincra.R;

public enum FaseCiclo {
    MESTRUALE(R.string.fase_mestruale),
    FOLLICOLARE(R.string.fase_follicolare),
    OVULATORIA(R.string.fase_ovulatoria),
    LUTEALE(R.string.fase_luteale);

    private final int resId;

    FaseCiclo(int resId){
        this.resId = resId;
    }

    public int getResId(){
        return resId;
    }
}
