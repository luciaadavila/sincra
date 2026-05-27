package com.example.sincra.model;

import java.util.List;

public class PredictSettimana {
    private String rango;
    private List<Boolean> periodo;
    private List<Integer> numbers;


    public PredictSettimana(String rango, List<Boolean> periodo, List<Integer> numbers) {
        this.rango = rango;
        this.periodo = periodo;
        this.numbers = numbers;
    }

    public String getRango() {
        return rango;
    }

    public List<Boolean> getPeriodo() {
        return periodo;
    }

    public void setRango(String rango) {
        this.rango = rango;
    }

    public void setPeriodo(List<Boolean> periodo) {
        this.periodo = periodo;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<Integer> numbers) {
        this.numbers = numbers;
    }
}
