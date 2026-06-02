package com.example.sincra.utils;

import com.example.sincra.model.Ciclo;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticheCalculator {

    // METODO PRINCIPAL
    // devuelve objeto result con estadísticas generales de ciclos, por fase, sintomas y moods más frecuentes y pasos medios
    public static StatisticheResult calcola(List<CicloConRegistrazioni> cicliConRegistrazioni, List<RegistrazioneConElementi> registrazioniConElementi) {
        StatisticheResult result = new StatisticheResult();

        if (cicliConRegistrazioni == null || cicliConRegistrazioni.isEmpty()) {
            return result;
        }

        // mapa para encontrar facilmente un ciclo a partir de su id
        Map<Integer, Ciclo> cicloById = new HashMap<>();
        // mapa para guardar qué fase corresponde a cada registro
        Map<Integer, FaseCiclo> faseByRegistroId = new HashMap<>();


        // recorremos todos los ciclos
        // -> duración media ciclo + periodo, ciclo minimo y maximo, fase de cada registración, pasos por fase, notas por fase
        for (CicloConRegistrazioni cicloConReg : cicliConRegistrazioni) {
            if (cicloConReg == null || cicloConReg.getCiclo() == null) continue;
            Ciclo ciclo = cicloConReg.getCiclo();
            cicloById.put(ciclo.getCicloId(), ciclo);
            result.addCiclo(ciclo);


            List<Registrazione> registrazioni = cicloConReg.getRegistrazioni();
            if (registrazioni == null) continue;

            // dentro de cada ciclo, recorremos cada registro del ciclo
            for (Registrazione reg : registrazioni) {
                if (reg == null) continue;

                FaseCiclo fase = calcolaFaseRegistrazione(reg, ciclo);
                if (fase == null) continue;

                faseByRegistroId.put(reg.getRegistroId(), fase);

                StatisticheFase statsFase = result.getStatsFase(fase);
                statsFase.addRegistrazione(reg);
            }
        }


        // ahora recorremos las registrazione con elementi
        // así sabemos que sintomas y moods hay por registración
        if (registrazioniConElementi != null) {
            for (RegistrazioneConElementi regConElementi : registrazioniConElementi) {
                if (regConElementi == null || regConElementi.registrazione == null) continue;

                Registrazione reg = regConElementi.registrazione;

                // intentamos recuperar la fase y si no la calculamos
                FaseCiclo fase = faseByRegistroId.get(reg.getRegistroId());

                if (fase == null && reg.getCicloId() != null) {
                    Ciclo ciclo = cicloById.get(reg.getCicloId());
                    if (ciclo != null) {
                        fase = calcolaFaseRegistrazione(reg, ciclo);
                    }
                }

                if (fase == null) continue;

                // acumulador de estadísticas de esa fase
                StatisticheFase statsFase = result.getStatsFase(fase);

                if (regConElementi.elementiCatalogo == null) continue;

                // recorremos todos los elementos del registro y lo añadimos a la fase
                for (ElementoCatalogo elemento : regConElementi.elementiCatalogo) {
                    statsFase.addElemento(elemento);
                }
            }
        }

        // calculamos las medias finales => duración media ciclo + periodo + variación media ciclo
        result.calcolaFinale();

        return result;
    }

    // calcula la fase del ciclo de una registración concreta
    private static FaseCiclo calcolaFaseRegistrazione(Registrazione reg, Ciclo ciclo) {
        if (reg == null || ciclo == null) return null;

        int giornoCiclo = reg.getGiornoCiclo();
        int durataPeriodo = ciclo.getDurataPeriodo();
        int durataTotale = ciclo.getDurataTotale();

        if (durataPeriodo <= 0) durataPeriodo = 5;
        if (durataTotale <= 0) durataTotale = 28;

        return FaseCicloUtils.calcoloFase(
                giornoCiclo,
                durataPeriodo,
                durataTotale
        );
    }

    // incrementa el contador de un mapa
    // mal di testa -> 2 => 3
    private static void incrementa(Map<String, Integer> map, String key) {
        if (key == null || key.trim().isEmpty()) return;
        Integer value = map.get(key);
        if (value == null) {
            map.put(key, 1);
        } else {
            map.put(key, value + 1);
        }
    }

    // ordena los mapas por el numero para hacer bien los gráficos luego
    private static List<ElementoStat> toSortedList(Map<String, Integer> map) {
        List<ElementoStat> list = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            list.add(new ElementoStat(entry.getKey(), entry.getValue()));
        }

        Collections.sort(list, new Comparator<ElementoStat>() {
            @Override
            public int compare(ElementoStat a, ElementoStat b) {
                return Integer.compare(b.getCount(), a.getCount());
            }
        });

        return list;
    }

    /// ///////////////////////////////////////////////////
    // RESULTADO GENERAL DE TODAS LAS ESTADÍSTICAS
    // guarda estadísticas generales de ciclos + estadísitcas agrupadas por fase
    public static class StatisticheResult {

        private int numeroCicli;

        private int sommaDurataCicli;
        private int sommaDurataPeriodo;

        private float durataMediaCiclo;
        private float durataMediaPeriodo;

        private int cicloMin = Integer.MAX_VALUE;
        private int cicloMax = 0;

        private float variazioneMediaCiclo;

        private final List<Integer> durateCicli = new ArrayList<>();

        // mapa de las estadisticcas por fase
        // MESTRUAELE -> estadísticas de fase mestruale
        private final EnumMap<FaseCiclo, StatisticheFase> statsPerFase = new EnumMap<>(FaseCiclo.class);

        // para luego tener una entrada de estadísticas para cada fase
        public StatisticheResult() {
            for (FaseCiclo fase : FaseCiclo.values()) {
                statsPerFase.put(fase, new StatisticheFase(fase));
            }
        }

        // añade un ciclo al cálculo general (acumula datos solo)
        private void addCiclo(Ciclo ciclo) {
            if (ciclo == null) return;

            int durataCiclo = ciclo.getDurataTotale();
            int durataPeriodo = ciclo.getDurataPeriodo();

            if (durataCiclo <= 0) return;

            numeroCicli++;

            sommaDurataCicli += durataCiclo;
            sommaDurataPeriodo += Math.max(durataPeriodo, 0);

            cicloMin = Math.min(cicloMin, durataCiclo);
            cicloMax = Math.max(cicloMax, durataCiclo);

            durateCicli.add(durataCiclo);
        }

        // calcula las medias finales después de añadir todos los ciclos
        private void calcolaFinale() {
            if (numeroCicli == 0) {
                cicloMin = 0;
                cicloMax = 0;
                return;
            }

            durataMediaCiclo = (float) sommaDurataCicli / numeroCicli;
            durataMediaPeriodo = (float) sommaDurataPeriodo / numeroCicli;

            float sommaDeviazioni = 0;

            for (int durata : durateCicli) {
                sommaDeviazioni += Math.abs(durata - durataMediaCiclo);
            }

            variazioneMediaCiclo = sommaDeviazioni / durateCicli.size();
        }

        // estadísticas de una fase concreta
        public StatisticheFase getStatsFase(FaseCiclo fase) {
            return statsPerFase.get(fase);
        }

        public int getNumeroCicli() {
            return numeroCicli;
        }

        public float getDurataMediaCiclo() {
            return durataMediaCiclo;
        }

        public float getDurataMediaPeriodo() {
            return durataMediaPeriodo;
        }

        public int getCicloMin() {
            return cicloMin;
        }

        public int getCicloMax() {
            return cicloMax;
        }

        public float getVariazioneMediaCiclo() {
            return variazioneMediaCiclo;
        }

        public EnumMap<FaseCiclo, StatisticheFase> getStatsPerFase() {
            return statsPerFase;
        }
    }


    ////////////////////////////////////////////////////////////
    // estadísticas de una fase concreta
    public static class StatisticheFase {

        private final FaseCiclo fase;

        private int numeroRegistrazioni;
        private int totalePassi;
        private int registrazioniConPassi;
        private int registrazioniConNote;

        // contadores de síntomas y de moods
        private final Map<String, Integer> sintomiCount = new LinkedHashMap<>();
        private final Map<String, Integer> moodCount = new LinkedHashMap<>();

        public StatisticheFase(FaseCiclo fase) {
            this.fase = fase;
        }

        // añade un registro a la fase
        private void addRegistrazione(Registrazione reg) {
            if (reg == null) return;

            numeroRegistrazioni++;

            if (reg.getPasos() > 0) {
                totalePassi += reg.getPasos();
                registrazioniConPassi++;
            }

            if (reg.getNotas() != null && !reg.getNotas().trim().isEmpty()) {
                registrazioniConNote++;
            }
        }

        // añade un síntoma o mood a la fase
        private void addElemento(ElementoCatalogo elemento) {
            if (elemento == null) return;

            String nome = elemento.getNome();

            if (elemento.getTipo().equals("syntom")) {
                incrementa(sintomiCount, nome);
            } else if (elemento.getTipo().equals("mood")) {
                incrementa(moodCount, nome);
            }
        }

        public FaseCiclo getFase() {
            return fase;
        }

        public int getNumeroRegistrazioni() {
            return numeroRegistrazioni;
        }

        public int getTotalePassi() {
            return totalePassi;
        }

        public int getRegistrazioniConPassi() {
            return registrazioniConPassi;
        }

        public int getRegistrazioniConNote() {
            return registrazioniConNote;
        }

        public int getMediaPassi() {
            if (registrazioniConPassi == 0) return 0;
            return Math.round((float) totalePassi / registrazioniConPassi);
        }

        public Map<String, Integer> getSintomiCount() {
            return sintomiCount;
        }

        public Map<String, Integer> getMoodCount() {
            return moodCount;
        }

        public List<ElementoStat> getSintomiOrdinati() {
            return toSortedList(sintomiCount);
        }

        public List<ElementoStat> getMoodOrdinati() {
            return toSortedList(moodCount);
        }

        public List<ElementoStat> getTopSintomi(int limit) {
            return limita(getSintomiOrdinati(), limit);
        }

        public List<ElementoStat> getTopMood(int limit) {
            return limita(getMoodOrdinati(), limit);
        }

        // recorta la lista para devolver solo los primeros elementos
        private List<ElementoStat> limita(List<ElementoStat> list, int limit) {
            if (list == null) return new ArrayList<>();

            if (limit <= 0 || list.size() <= limit) {
                return list;
            }

            return new ArrayList<>(list.subList(0, limit));
        }
    }


    ///////////////////////////////////////////
    // sintoma/mood con su contador
    public static class ElementoStat {

        private final String nome;
        private final int count;

        public ElementoStat(String nome, int count) {
            this.nome = nome;
            this.count = count;
        }

        public String getNome() {
            return nome;
        }

        public int getCount() {
            return count;
        }
    }
}