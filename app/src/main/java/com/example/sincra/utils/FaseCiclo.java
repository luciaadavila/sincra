package com.example.sincra.utils;

public enum FaseCiclo {
    MESTRUALE("Fase mestruale"),
    FOLLICOLARE("Fase follicolare"),
    OVULATORIA("Fase ovulatoria"),
    LUTEALE("Fase luteale");

    private final String label;

    FaseCiclo(String label){
        this.label = label;
    }

    public String getLabel(){
        return label;
    }
}
