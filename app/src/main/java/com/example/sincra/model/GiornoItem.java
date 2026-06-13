package com.example.sincra.model;

public class GiornoItem {
    private final int numGiorno;
    private boolean selected;

    public GiornoItem(int numGiorno, boolean selected) {
        this.numGiorno = numGiorno;
        this.selected = selected;
    }


    public int getNumGiorno() {
        return numGiorno;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
