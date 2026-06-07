package com.example.sincra.utils;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatisticheCalculator {

    public static StatisticheResult calcola(List<CicloConRegistrazioni> cicliConRegistrazioni, List<RegistrazioneConElementi> registrazioniConElementi) {
        StatisticheResult result = new StatisticheResult();

        if (cicliConRegistrazioni == null || cicliConRegistrazioni.isEmpty()) {
            return result;
        }

        // 1. Mappiamo i cicli per ID per un accesso rapido
        Map<Integer, Ciclo> cicloById = new HashMap<>();
        for (CicloConRegistrazioni ccr : cicliConRegistrazioni) {
            if (ccr.getCiclo() != null) {
                cicloById.put(ccr.getCiclo().getCicloId(), ccr.getCiclo());
                result.addCiclo(ccr.getCiclo());
            }
        }

        Set<Integer> processedRegIds = new HashSet<>();

        // 2. Elaboriamo le registrazioni con elementi (sintomi/mood)
        if (registrazioniConElementi != null) {
            for (RegistrazioneConElementi rce : registrazioniConElementi) {
                Registrazione reg = rce.registrazione;
                if (reg == null) continue;

                // Cerchiamo il ciclo (per ID o per data se orfana)
                Ciclo ciclo = (reg.getCicloId() != null) ? cicloById.get(reg.getCicloId()) : trovaCicloPerData(reg, cicloById);
                if (ciclo == null) continue;

                FaseCiclo fase = calcolaFaseRegistrazione(reg, ciclo);
                if (fase == null) continue;

                StatisticheFase statsFase = result.getStatsFase(fase);
                
                if (!processedRegIds.contains(reg.getRegistroId())) {
                    statsFase.addRegistrazione(reg);
                    processedRegIds.add(reg.getRegistroId());
                }

                if (rce.elementiCatalogo != null) {
                    for (ElementoCatalogo elemento : rce.elementiCatalogo) {
                        statsFase.addElemento(elemento);
                    }
                }
            }
        }

        // 3. Elaboriamo eventuali registrazioni mancanti dai cicli
        for (CicloConRegistrazioni ccr : cicliConRegistrazioni) {
            if (ccr.getRegistrazioni() != null) {
                for (Registrazione reg : ccr.getRegistrazioni()) {
                    if (!processedRegIds.contains(reg.getRegistroId())) {
                        FaseCiclo fase = calcolaFaseRegistrazione(reg, ccr.getCiclo());
                        if (fase != null) {
                            result.getStatsFase(fase).addRegistrazione(reg);
                            processedRegIds.add(reg.getRegistroId());
                        }
                    }
                }
            }
        }

        result.calcolaFinale();
        return result;
    }

    private static Ciclo trovaCicloPerData(Registrazione reg, Map<Integer, Ciclo> cicloById) {
        if (reg.getData() == null) return null;
        long time = CicloRepository.truncarFecha(reg.getData()).getTime();

        Ciclo migliorMatch = null;
        long maxInizio = -1;

        for (Ciclo c : cicloById.values()) {
            Date inizioDate = CicloRepository.truncarFecha(c.getDataInizio());
            if (inizioDate == null) continue;
            long inizio = inizioDate.getTime();

            if (time >= inizio) {
                if (c.getDataFine() != null) {
                    long fine = CicloRepository.truncarFecha(c.getDataFine()).getTime();
                    if (time <= fine) return c;
                } else if (inizio > maxInizio) {
                    maxInizio = inizio;
                    migliorMatch = c;
                }
            }
        }
        return migliorMatch;
    }

    private static FaseCiclo calcolaFaseRegistrazione(Registrazione reg, Ciclo ciclo) {
        if (reg == null || ciclo == null || reg.getData() == null || ciclo.getDataInizio() == null) return null;

        int giornoCiclo = CicloRepository.difDays(ciclo.getDataInizio(), reg.getData());
        if (giornoCiclo <= 0) return null;

        int durataPeriodo = ciclo.getDurataPeriodo();
        int durataTotale = ciclo.getDurataTotale();

        if (durataPeriodo <= 0) durataPeriodo = 5;
        if (durataTotale <= 0) durataTotale = 28;

        return FaseCicloUtils.calcoloFase(giornoCiclo, durataPeriodo, durataTotale);
    }

    private static void incrementa(Map<String, Integer> map, String key) {
        if (key == null || key.trim().isEmpty()) return;
        Integer value = map.get(key);
        map.put(key, (value == null) ? 1 : value + 1);
    }

    private static List<ElementoStat> toSortedList(Map<String, Integer> map) {
        List<ElementoStat> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            list.add(new ElementoStat(entry.getKey(), entry.getValue()));
        }
        Collections.sort(list, (a, b) -> Integer.compare(b.getCount(), a.getCount()));
        return list;
    }

    public static class StatisticheResult {
        private int numeroCicli;
        private int sommaDurataCicli;
        private int sommaDurataPeriodo;
        private int durataMediaCiclo;
        private int durataMediaPeriodo;
        private int cicloMin = Integer.MAX_VALUE;
        private int cicloMax = 0;
        private int variazioneMediaCiclo;
        private final List<Integer> durateCicli = new ArrayList<>();
        private final EnumMap<FaseCiclo, StatisticheFase> statsPerFase = new EnumMap<>(FaseCiclo.class);

        public StatisticheResult() {
            for (FaseCiclo fase : FaseCiclo.values()) {
                statsPerFase.put(fase, new StatisticheFase(fase));
            }
        }

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

        private void calcolaFinale() {
            if (numeroCicli == 0) {
                cicloMin = 0;
                cicloMax = 0;
                return;
            }
            durataMediaCiclo = Math.round( (float) sommaDurataCicli / numeroCicli);
            durataMediaPeriodo = Math.round((float) sommaDurataPeriodo / numeroCicli);
            float sommaDeviazioni = 0;
            for (int durata : durateCicli) {
                sommaDeviazioni += Math.abs(durata - durataMediaCiclo);
            }
            variazioneMediaCiclo = Math.round((float) sommaDeviazioni / durateCicli.size());
        }

        public StatisticheFase getStatsFase(FaseCiclo fase) {
            return statsPerFase.get(fase);
        }

        public int getNumeroCicli() { return numeroCicli; }
        public int getDurataMediaCiclo() { return durataMediaCiclo; }
        public int getDurataMediaPeriodo() { return durataMediaPeriodo; }
        public int getCicloMin() { return cicloMin; }
        public int getCicloMax() { return cicloMax; }
        public int getVariazioneMediaCiclo() { return variazioneMediaCiclo; }
    }

    public static class StatisticheFase {
        private final FaseCiclo fase;
        private int numeroRegistrazioni;
        private int totalePassi;
        private int registrazioniConPassi;
        private final Map<String, Integer> sintomiCount = new LinkedHashMap<>();
        private final Map<String, Integer> moodCount = new LinkedHashMap<>();

        public StatisticheFase(FaseCiclo fase) { this.fase = fase; }

        public void addRegistrazione(Registrazione reg) {
            if (reg == null) return;
            numeroRegistrazioni++;
            if (reg.getPassi() > 0) {
                totalePassi += reg.getPassi();
                registrazioniConPassi++;
            }
        }

        public void addElemento(ElementoCatalogo elemento) {
            if (elemento == null) return;
            String nome = elemento.getNome();
            if ("symptom".equals(elemento.getTipo())) {
                incrementa(sintomiCount, nome);
            } else if ("mood".equals(elemento.getTipo())) {
                incrementa(moodCount, nome);
            }
        }

        public FaseCiclo getFase() { return fase; }
        public int getNumeroRegistrazioni() { return numeroRegistrazioni; }
        public int getMediaPassi() {
            if (registrazioniConPassi == 0) return 0;
            return Math.round((float) totalePassi / registrazioniConPassi);
        }
        public List<ElementoStat> getTopSintomi(int limit) { return limita(toSortedList(sintomiCount), limit); }
        public List<ElementoStat> getTopMood(int limit) { return limita(toSortedList(moodCount), limit); }

        private List<ElementoStat> limita(List<ElementoStat> list, int limit) {
            if (list == null) return new ArrayList<>();
            if (limit <= 0 || list.size() <= limit) return list;
            return new ArrayList<>(list.subList(0, limit));
        }
    }

    public static class ElementoStat {
        private final String nome;
        private final int count;
        public ElementoStat(String nome, int count) { this.nome = nome; this.count = count; }
        public String getNome() { return nome; }
        public int getCount() { return count; }
    }
}
