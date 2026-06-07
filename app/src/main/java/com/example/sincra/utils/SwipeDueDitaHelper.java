package com.example.sincra.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class SwipeDueDitaHelper {

    public interface OnDuaDitaSwipeListener{
        void onSwipeLeft();
        void onSwipeRight();
    }

    private float startTwoFingerX;
    private float startTwoFingerY;
    private boolean twoFingerGestureActive = false;
    private boolean twoFingerGestureDetected = false;

    private final int minSwipeDistance;
    private final OnDuaDitaSwipeListener listener;

    public SwipeDueDitaHelper(Context context, OnDuaDitaSwipeListener listener){
        // definiamo la distanza minima per considerarlo swipe
        this.minSwipeDistance = ViewConfiguration.get(context).getScaledTouchSlop() * 8;
        this.listener = listener;
    }

    // target view è la vista su cui vogliamo rilevare il gesto
    @SuppressLint("ClickableViewAccessibility")
    public void configuraSwipeDueDita(View targetView) {
        // quando l'utente tocca la vista, otteniamo informazioni da MotionEvent
        targetView.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                // si appoggia il secondo dito
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (event.getPointerCount() == 2) {
                        // salviamo la posizione iniziale media delle dita
                        startTwoFingerX = getMediaX(event);
                        startTwoFingerY = getMediaY(event);

                        // indichiamo che inizia il gesto con due dita ma senza rilevarlo ancora
                        twoFingerGestureActive = true;
                        twoFingerGestureDetected = false;
                        return true;
                    }
                    break;
                // le dita si muovono
                case MotionEvent.ACTION_MOVE:
                    // se c'erano due dita e rimangono due dita e non era ancora stato rilevato il gesto
                    if (twoFingerGestureActive && event.getPointerCount() == 2 && !twoFingerGestureDetected) {
                        // calcoliamo la nuova posizione delle dita
                        float currentX = getMediaX(event);
                        float currentY = getMediaY(event);

                        // calcoliamo la differenza tra la posizione attuale e quella iniziale
                        float diffX = currentX - startTwoFingerX;
                        float diffY = currentY - startTwoFingerY;

                        // verifichiamo che il movimento sia stato più orizzontale che verticale
                        boolean movimentoOrizzontale = Math.abs(diffX) > Math.abs(diffY);
                        // verifichiamo che la distanza sia stata sufficiente
                        boolean distanzaSufficiente = Math.abs(diffX) > minSwipeDistance;

                        // se si verificano entrambi i casi => verifichiamo se è verso sinistra o destra
                        if (movimentoOrizzontale && distanzaSufficiente) {
                            twoFingerGestureDetected = true;
                            if (diffX < 0) {
                                listener.onSwipeLeft();
                            } else {
                                listener.onSwipeRight();
                            }
                            return true;
                        }
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP: // si alza un dito
                case MotionEvent.ACTION_UP: // si alza il secondo dito
                case MotionEvent.ACTION_CANCEL: // android annulla il gesto
                    twoFingerGestureActive = false;
                    twoFingerGestureDetected = false;
                    break;
            }

            return false;
        });
    }

    private float getMediaX(MotionEvent event) {
        float suma = 0;
        for (int i = 0; i < event.getPointerCount(); i++) {
            suma += event.getX(i);
        }
        return suma / event.getPointerCount();
    }

    private float getMediaY(MotionEvent event) {
        float suma = 0;
        for (int i = 0; i < event.getPointerCount(); i++) {
            suma += event.getY(i);
        }
        return suma / event.getPointerCount();
    }

}
