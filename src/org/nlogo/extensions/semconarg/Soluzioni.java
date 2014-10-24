package org.nlogo.extensions.semconarg;

import JaCoP.constraints.*;
import JaCoP.core.*;
import JaCoP.search.*;
import JaCoP.set.constraints.AinB;
import JaCoP.set.core.BoundSetDomain;
import JaCoP.set.core.SetVar;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.nlogo.extensions.semconarg.SemConArg.SemiringType;

/**
 * @author Francesco Santini
 */
public class Soluzioni {

    DirectedOrderedSparseMultigraph<Integer, MyEdge> grafo;
    int nconstraints;

    public Soluzioni(DirectedOrderedSparseMultigraph<Integer, MyEdge> inp_grafo) {
        grafo = inp_grafo;
    }

    private void graph2intvar(DirectedOrderedSparseMultigraph<Integer, MyEdge> grafo, Store store, ArrayList<IntVar> nodi) {
        Iterator iterNodi = grafo.getVertices().iterator();
        int c = 0;
        while (iterNodi.hasNext()) {
            iterNodi.next();
            nodi.add(new IntVar(store, "" + c, 0, 1));
            c++;
        }
    }

    
    private void graph2intvarCoalition(DirectedOrderedSparseMultigraph<Integer, MyEdge> grafo, Store store, ArrayList<IntVar> nodi, ArrayList<IntVar> nodi2) {
        Iterator iterNodi = grafo.getVertices().iterator();
        int c = 0;
        while (iterNodi.hasNext()) {
            iterNodi.next();
            nodi.add(new IntVar(store, "" + c, 0, SemConArg.numNodi - 1));
            nodi2.add(new IntVar(store, "" + c, 0, SemConArg.numNodi + SemConArg.numNodi - 1));
            c++;
        }
    }

    private int[][] searchSolution(Store store, ArrayList<IntVar> vettore) {
        if (!store.consistency()) {
            System.err.println("ERRORE!!!!");
        }

        int[][] soluzione = null;
        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        IntVar variables[] = new IntVar[vettore.size()];
        variables = vettore.toArray(variables);
        SelectChoicePoint<IntVar> select =
                new InputOrderSelect<IntVar>(store, variables,
                new IndomainMin<IntVar>());
        search.setTimeOut(180);
        search.getSolutionListener().searchAll(true);
        search.getSolutionListener().recordSolutions(true);
        search.setAssignSolution(false);
        boolean result = search.labeling(store, select);
        if (result) {
            //search.printAllSolutions();
            soluzione = new int[search.getSolutionListener().solutionsNo()][vettore.size()];
            for (int i = 1; i <= search.getSolutionListener().solutionsNo(); i++) {
                for (int j = 0; j < search.getSolution(i).length; j++) {
                    soluzione[i - 1][j] = Integer.valueOf(search.getSolution(i)[j].toString());
                }
            }
        }
        return soluzione;
    }

    private int[][] searchMinSolutionWithCSPOptimization(Store store, ArrayList<IntVar> vettore) {
        if (!store.consistency()) {
            System.err.println("ERRORE!!!!");
        }

        int[][] soluzione = null;
        IntVar costo = new IntVar(store, 0, vettore.size());
        store.impose(new Sum(vettore, costo));
        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        IntVar variables[] = new IntVar[vettore.size()];
        variables = vettore.toArray(variables);
        SelectChoicePoint<IntVar> select =
                new InputOrderSelect<IntVar>(store, variables,
                new IndomainMin<IntVar>());
        search.setTimeOut(180);
        search.getSolutionListener().searchAll(true);
        search.getSolutionListener().recordSolutions(true);
        search.setAssignSolution(false);
        boolean result = search.labeling(store, select, costo);
        if (result) {
            Domain[] lastSol = search.getSolution(search.getSolutionListener().solutionsNo());
            int sommaBest = 0;
            for (int i = 0; i < lastSol.length; i++) {
                sommaBest += Integer.valueOf(lastSol[i].toString());
            }

            store.impose(new XeqC(costo, sommaBest));

            return searchSolution(store, vettore);
        }
        return soluzione;
    }

    private int[][] searchCoalitionSolution(Store store, ArrayList<IntVar> vettore) {
        int[][] soluzione = null;
        // CREDIT SEARCH
        IntVar variables[] = new IntVar[vettore.size()];
        variables = vettore.toArray(variables);
//        SelectChoicePoint select = new SimpleSelect(variables,
//                new MostConstrainedStatic(),
//                new IndomainMiddle());
//
//        int credits = 5000, backtracks = 80000, maxDepth = 100;
//        CreditCalculator credit = new CreditCalculator(credits,
//                backtracks,
//                maxDepth);
//        Search label = new DepthFirstSearch();
//        //label.getSolutionListener().searchAll(true);
//        //label.getSolutionListener().recordSolutions(true);
//        label.setConsistencyListener(credit);
//        label.setExitChildListener(credit);
//        label.setTimeOutListener(credit);
//        boolean result = label.labeling(store, select);
        Search label = new DepthFirstSearch();
        SelectChoicePoint select = new SimpleSelect(variables,
                new MostConstrainedStatic(),
                new IndomainMiddle());
        label.getSolutionListener().searchAll(true);
        label.setTimeOut(180);
        label.getSolutionListener().recordSolutions(true);
        boolean result = label.labeling(store, select);
        if (result) {
            //label.printAllSolutions();
            soluzione = new int[label.getSolutionListener().solutionsNo()][vettore.size()];
            for (int i = 1; i <= label.getSolutionListener().solutionsNo(); i++) {
                for (int j = 0; j < label.getSolution(i).length; j++) {
                    soluzione[i - 1][j] = Integer.valueOf(label.getSolution(i)[j].toString());
                }
            }
        }
        return soluzione;
    }

    private int[][] searchMaxSolution(Store store, ArrayList<IntVar> vettore) {
        //Calcolo massimo delle ammissibili
        int[][] soluzioniAmmissibili = searchSolutionAdmissible();
        int contatore = 0;
        int massimo = 0;
        for (int i = 0; i < soluzioniAmmissibili.length; i++) {
            for (int j = 0; j < vettore.size(); j++) {
                if (soluzioniAmmissibili[i][j] == 1) {
                    contatore++;
                }
            }
            if ((contatore > massimo)) {
                massimo = contatore;
            }
            contatore = 0;
        }
        //Calcolo massimo delle ammissibili
        int[][] soluzione = null;
        //Vincolo per trovare tutte le soluzioni massime
        IntVar somma = new IntVar(store, "sommaA", 0, massimo);
        store.impose(new Sum(vettore, somma));
        store.impose(new XneqC(somma, 0));
        store.impose(new XeqC(somma, massimo));
        vettore.add(somma);
        //Vincolo per trovare tutte le soluzioni massime
        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        IntVar variables[] = new IntVar[vettore.size()];
        variables = vettore.toArray(variables);
        SelectChoicePoint<IntVar> select =
                new InputOrderSelect<IntVar>(store, variables,
                new IndomainMax<IntVar>());
        search.getSolutionListener().searchAll(true);
        search.setTimeOut(180);
        search.getSolutionListener().recordSolutions(true);
        boolean result = search.labeling(store, select);
        if (result) {
            soluzione = new int[search.getSolutionListener().solutionsNo()][vettore.size()];
            for (int i = 1; i <= search.getSolutionListener().solutionsNo(); i++) {
                for (int j = 0; j < search.getSolution(i).length; j++) {
                    soluzione[i - 1][j] = Integer.valueOf(search.getSolution(i)[j].toString());
                }
            }
        }
        return soluzione;
    }

    /*
     * VERSIONE PER TROVARE IL MASSIMO CORRETTA (Capezzali, Mancini, Mignogna)
     */
    private int[][] searchMaxSolutionWithCSPOptimization(Store store, ArrayList<IntVar> vettore) {
        
        /*
         * Jacop offre la possibilità di ottimizzare una funzione obiettivo
         * definendola come una semplice variabile rafforzata da vincoli. Il
         * "problema" è che Jacop tende a minimizzare la funzione costo, quindi
         * noi, dovendo massimizzare, risolviamo il problema negando la funzione
         * obiettivo. Quindi se il nodo è nella soluzione, varrà -1, altrimenti
         * 0. Jacop restituisce UNA sola soluzione ottima, quindi dovremo di
         * nuovo far partire un'altra ricerca imponendo di ritornare tutte
         * quelle soluzioni che hanno il costo della soluzione ottima trovata
         * precedentemente.
         */

        //Calcolo massimo delle ammissibili
        int[][] soluzione = null;

        IntVar costo = new IntVar(store, "costoSol", -1 * vettore.size(), 0);
        ArrayList<Integer> pesi = new ArrayList<Integer>();
        for (int i = 0; i < vettore.size(); i++) {
            pesi.add(new Integer(-1));
        }
        store.impose(new SumWeight(vettore, pesi, costo));

        //Vincolo per trovare tutte le soluzioni massime
        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        IntVar variables[] = new IntVar[vettore.size()];
        variables = vettore.toArray(variables);
        SelectChoicePoint<IntVar> select =
                new InputOrderSelect<IntVar>(store, variables,
                new IndomainMax<IntVar>());

        search.getSolutionListener().searchAll(true);
        search.setTimeOut(180);
        search.getSolutionListener().recordSolutions(true);
        search.setAssignSolution(false);

        boolean result = search.labeling(store, select, costo);
        if (result) {
            Domain[] lastSol = search.getSolution(search.getSolutionListener().solutionsNo());
            int sommaBest = 0;
            for (int i = 0; i < lastSol.length; i++) {
                sommaBest -= Integer.valueOf(lastSol[i].toString());
            }

            store.impose(new XeqC(costo, sommaBest));

            return searchSolution(store, vettore);
        }
        return soluzione;
    }

    private int[][] searchMaximalInclusionSolution(Store store, ArrayList<IntVar> vettore) {
        int[][] soluzioniRange = searchSolution(store, vettore);
        int[][] soluzioniMaximalInclusion = searchMaximalInclusion(soluzioniRange);
        int righe = 0;
        int colonne = 0;
        if (soluzioniMaximalInclusion != null) {
            righe = soluzioniMaximalInclusion.length;
            colonne = soluzioniMaximalInclusion[0].length / 2;
        }
        int[][] soluzione = new int[righe][colonne];
        for (int i = 0; i < soluzione.length; i++) {
            for (int j = 0; j < soluzione[i].length; j++) {
                soluzione[i][j] = soluzioniMaximalInclusion[i][j];
            }
        }
        return soluzione;
    }

    private int[][] searchMaxIdealSolution(Store store, ArrayList<IntVar> vettore) {

        int idealconstraint = 0;
        //Trovo le soluzioni ammissibili
        int[][] soluzioniAmmissibili = searchSolution(store, vettore);
        //Trovo le soluzioni ammissibili massimali rispetto all'inclusione
        int[][] soluzioniPreferred = searchMaximalInclusion(soluzioniAmmissibili);

        Store nuovoStore = new Store();
        ArrayList<IntVar> variabili = new ArrayList<IntVar>();
        graph2intvar(grafo, nuovoStore, variabili);

        //Calcolo l'intersezione delle soluzioni preferred (quindi significa che l'elemento che
        //appartiene a tutte le preferred è ovviamente un loro sottisieme).
        for (int j = 0; j < soluzioniPreferred[0].length; j++) {
            ArrayList listAND = new ArrayList();
            for (int i = 0; i < soluzioniPreferred.length; i++) {
                IntVar var = new IntVar(nuovoStore, "S" + i + "N" + j, soluzioniPreferred[i][j], soluzioniPreferred[i][j]);
                listAND.add(new XeqC(var, 1));
            }
            
            nuovoStore.impose(new IfThen(new Not(new And(listAND)), new XeqC(variabili.get(j), 0)));
            idealconstraint++;
            nconstraints++;
        }

        //Queste intersezioni devono essere ammissibili
        imposeConstraintConflictFree(nuovoStore, variabili, grafo);
        imposeConstraintAdmissibleSet(nuovoStore, variabili, grafo);

        //Cerco queste soluzioni
        int[][] soluzioniIntersecateConPreferredAmmissibili = searchSolution(nuovoStore, variabili);
        //... e queste soluzioni devono essere massimali rispetto all'inclusione
        int[][] soluzioniIdeal = searchMaximalInclusion(soluzioniIntersecateConPreferredAmmissibili);
        
        //System.out.println("Ideal constraints: " + idealconstraint);
        return soluzioniIdeal;

    }
    
    /*
     * VERSIONE IMPLEMENTATA (Capezzali, Mancini, Mignogna)
     */
    private int[][] searchSolutionPreferred(Store store, ArrayList<IntVar> vettore) {

        int[][] soluzioniAmmissibili = searchSolution(store, vettore);
        int[][] soluzioniPreferred = searchMaximalInclusion(soluzioniAmmissibili);        
        return soluzioniPreferred;
    }
    
    
    /*
     * Santini
     */
    private int[][] searchSolutionAlphaPreferred(Store store, ArrayList<IntVar> vettore) {

        int[][] soluzioniAlphaAmmissibili = searchSolution(store, vettore);
        int[][] soluzioniAlphaPreferred = searchMaximalInclusion(soluzioniAlphaAmmissibili);        
        return soluzioniAlphaPreferred;
    }

    /*
     * VERSIONE CORRETTA (Capezzali, Mancini, Mignogna)
     */
    private int[][] searchSolutionGroundedSet(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph) {
        int groundedconstraints = 0;
        //Calcolo tutte le soluzioni complente
        int[][] soluzioniComplete = searchMinSolutionWithCSPOptimization(store, v);
        
        Store nuovoStore = new Store();
        ArrayList<IntVar> variabili = new ArrayList<IntVar>();
        graph2intvar(grafo, nuovoStore, variabili);

        //Calcolo l'intersezione
        for (int j = 0; j < soluzioniComplete[0].length; j++) {
            ArrayList listAND = new ArrayList();
            for (int i = 0; i < soluzioniComplete.length; i++) {
                IntVar var = new IntVar(nuovoStore, "S" + i + "N" + j, soluzioniComplete[i][j], soluzioniComplete[i][j]);
                listAND.add(new XeqC(var, 1));
            }
            
            nuovoStore.impose(new IfThen(new Not(new And(listAND)), new XeqC(variabili.get(j), 0)));
            groundedconstraints++;
            nconstraints++;
        }
        
        //Impongo che le intersezioni siano complete
        imposeConstraintConflictFree(nuovoStore, variabili, grafo);
        imposeConstraintAdmissibleSet(nuovoStore, variabili, grafo);
        imposeConstraintCompleteExtensions(nuovoStore, variabili, grafo);
        
        int[][] soluzioniIntersecate = searchSolution(nuovoStore, variabili);
        int[][] soluzioniMinimal = searchMinimalInclusion(soluzioniIntersecate);
        return soluzioniMinimal;
        
        
//        int contatore = 0;
//        int minimo = v.size();
//        for (int i = 0; i < soluzioniComplete.length; i++) {
//            for (int j = 0; j < v.size(); j++) {
//                if (soluzioniComplete[i][j] == 1) {
//                    contatore++;
//                }
//            }
//            if ((contatore < minimo) && (contatore != 0)) {
//                minimo = contatore;
//            }
//            contatore = 0;
//        }
//        //CALCOLO IL MINIMO NELLE COMPLETE
//        imposeConstraintCompleteExtensions(store, v, graph);
//        int[][] soluzione = null;
//        IntVar somma = new IntVar(store, "sommaG", 0, graph.getVertexCount());
//        store.impose(new Sum(v, somma));
//        store.impose(new XneqC(somma, 0));
//
//        //VINCOLO PER TROVARE LA GROUNDED "COMPLETE DI COSTO MINIMO"
//        store.impose(new XeqC(somma, minimo));
//        v.add(somma);
//        IntVar variables[] = new IntVar[v.size()];
//        variables = v.toArray(variables);
//        Search<IntVar> search = new DepthFirstSearch<IntVar>();
//        SelectChoicePoint<IntVar> select =
//                new SimpleSelect<IntVar>(variables,
//                new SmallestMin<IntVar>(),
//                new SmallestDomain<IntVar>(),
//                new IndomainMin<IntVar>());
//        boolean result = search.labeling(store, select);
//        if (result) {
//            soluzione = new int[search.getSolutionListener().solutionsNo()][v.size()];
//            for (int i = 1; i <= search.getSolutionListener().solutionsNo(); i++) {
//                for (int j = 0; j < search.getSolution(i).length; j++) {
//                    soluzione[i - 1][j] = Integer.valueOf(search.getSolution(i)[j].toString());
//                }
//            }
//        }
//        int[][] soluzioneComplete = searchSolutionCompleteSomma(somma);
//        if (soluzioneComplete != null && soluzioneComplete.length > 1) {
//            int[][] soluzioneNew = new int[1][SemConArg.numNodi];
//            for (int i = 0; i < SemConArg.numNodi; i++) {
//                soluzioneNew[0][i] = 0;
//            }
//
//            return soluzioneNew;
//        }
//        return soluzione;
    }
    
    
    
    private int[][] searchSolutionAlphaGroundedSet(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph, int alpha) {
        int alphagroundedconstraints = 0;
        //Calcolo tutte le soluzioni complente
        int[][] soluzioniAlphaComplete = searchMinSolutionWithCSPOptimization(store, v);
        
        Store nuovoStore = new Store();
        ArrayList<IntVar> variabili = new ArrayList<IntVar>();
        graph2intvar(grafo, nuovoStore, variabili);


        //Calcolo l'intersezione
        for (int j = 0; j < (soluzioniAlphaComplete[0].length - 1); j++) {
            ArrayList listAND = new ArrayList();
            for (int i = 0; i < soluzioniAlphaComplete.length; i++) {
                IntVar var = new IntVar(nuovoStore, "S" + i + "N" + j, soluzioniAlphaComplete[i][j], soluzioniAlphaComplete[i][j]);
                listAND.add(new XeqC(var, 1));
            }
            
            nuovoStore.impose(new IfThen(new Not(new And(listAND)), new XeqC(variabili.get(j), 0)));
            alphagroundedconstraints++;
            nconstraints++;
        }
        
        //Impongo che le intersezioni siano complete
        imposeConstraintAlphaConflictFree(nuovoStore, variabili, grafo, alpha);
        imposeConstraintAlphaAdmissibleSet(nuovoStore, variabili, grafo);
        imposeConstraintCompleteExtensions(nuovoStore, variabili, grafo);
        
        int[][] soluzioniIntersecate = searchSolution(nuovoStore, variabili);
        int[][] soluzioniMinimal = searchMinimalInclusion(soluzioniIntersecate);
        return soluzioniMinimal;
        
    }
    

    private boolean searchSolutionBoolean(Store store, ArrayList<IntVar> vettore) {
        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        IntVar variables[] = new IntVar[vettore.size()];
        variables = vettore.toArray(variables);
        SelectChoicePoint<IntVar> select =
                new InputOrderSelect<IntVar>(store, variables,
                new IndomainMax<IntVar>());
        search.getSolutionListener().setSolutionLimit(1);
        boolean result = search.labeling(store, select);
        if (result) {
            return true;
        }
        return false;       //non è preferred extensions
    }

    // *-*-*-* SOLUZIONI *-*-*-*
    public int[][] searchSolutionConflictFree() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintConflictFree(store, vettore, grafo);
        int[][] soluzioneConflictFree = searchSolution(store, vettore);
        return soluzioneConflictFree;
    }

    public int[][] searchSolutionAdmissible() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintConflictFree(store, vettore, grafo);
        imposeConstraintAdmissibleSet(store, vettore, grafo);
        int[][] soluzioneAdmissible = searchSolution(store, vettore);
        return soluzioneAdmissible;
    }

    public int[][] searchSolutionComplete() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintConflictFree(store, vettore, grafo);
        imposeConstraintAdmissibleSet(store, vettore, grafo);
        imposeConstraintCompleteExtensions(store, vettore, grafo);
        int[][] soluzioneComplete = searchSolution(store, vettore);
        return soluzioneComplete;
    }

    //PER LA GROUNDED
    public int[][] searchSolutionCompleteSomma(IntVar somma) {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintConflictFree(store, vettore, grafo);
        imposeConstraintAdmissibleSet(store, vettore, grafo);
        imposeConstraintCompleteExtensions(store, vettore, grafo);
        IntVar somma2 = new IntVar(store, "somma2", 0, vettore.size());
        store.impose(new Sum(vettore, somma2));
        for (int j = 0; j < vettore.size(); j++) {
            store.impose(new XeqY(somma2, somma));
        }
        int[][] soluzioneComplete = searchSolution(store, vettore);
        return soluzioneComplete;
    }

    public int[][] searchSolutionStable() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintConflictFree(store, vettore, grafo);
        imposeConstraintStableExtensions(store, vettore, grafo);
        int[][] soluzioneStable = searchSolution(store, vettore);
        return soluzioneStable;
    }

    public boolean searchSolutionPreferred(int[] input) {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintConflictFree(store, vettore, grafo);
        imposeConstraintAdmissibleSet(store, vettore, grafo);
        imposeConstraintEqInput(store, vettore, input);
        if (store.consistency()) {
            Store nuovo = new Store();
            vettore.clear();
            graph2intvar(grafo, nuovo, vettore);
            imposeConstraintConflictFree(nuovo, vettore, grafo);
            imposeConstraintAdmissibleSet(nuovo, vettore, grafo);
            imposeConstraintPreferredSetSecond(nuovo, input, vettore);
            return searchSolutionBoolean(nuovo, vettore);
        }
        return true;

    }
    
    /* **** */
    public int[][] searchSolutionPreferred() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintConflictFree(store, vettore, grafo);
        imposeConstraintAdmissibleSet(store, vettore, grafo);
        int[][] soluzioniPreferred = searchSolutionPreferred(store, vettore);
        return soluzioniPreferred;
    }
    
    
    
    /* Santini */ 
    public int[][] searchSolutionAlphaPreferred(int alpha) {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        //imposeConstraintConflictFree(store, vettore, grafo);
        //imposeConstraintAdmissibleSet(store, vettore, grafo);
        imposeConstraintAlphaConflictFree(store, vettore, grafo, alpha);
        imposeConstraintAlphaAdmissibleSet(store, vettore, grafo);
        int[][] soluzioniAlphaPreferred = searchSolutionPreferred(store, vettore);
        return soluzioniAlphaPreferred;
    }
    
    /* Santini */
    public int[][] searchSolutionAlphaGrounded(int alpha) {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintAlphaConflictFree(store, vettore, grafo, alpha);
        imposeConstraintAlphaAdmissibleSet(store, vettore, grafo);
        imposeConstraintCompleteExtensions(store, vettore, grafo);
        return searchSolutionAlphaGroundedSet(store, vettore, grafo, alpha);
    }


    public int[][] searchSolutionMaxPreferred() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintConflictFree(store, vettore, grafo);
        imposeConstraintAdmissibleSet(store, vettore, grafo);
        int[][] solutionMaxPreferred = searchMaxSolutionWithCSPOptimization(store, vettore);
        return solutionMaxPreferred;
    }

    public int[][] searchSolutionGrounded() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintConflictFree(store, vettore, grafo);
        imposeConstraintAdmissibleSet(store, vettore, grafo);
        imposeConstraintCompleteExtensions(store, vettore, grafo);
        return searchSolutionGroundedSet(store, vettore, grafo);
    }

    public int[][] searchSolutionAlphaComplete(int budget) {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintAlphaConflictFree(store, vettore, grafo, budget);
        imposeConstraintAlphaAdmissibleSet(store, vettore, grafo);
        imposeConstraintCompleteExtensions(store, vettore, grafo);
        int[][] soluzioneComplete = searchSolution(store, vettore);
        return soluzioneComplete;
    }

    public boolean searchSolutionAlphaGrounded_Exists(int[] input, int budget) {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintAlphaConflictFree(store, vettore, grafo, budget);
        imposeConstraintAdmissibleSet(store, vettore, grafo);
        imposeConstraintCompleteExtensions(store, vettore, grafo);
        imposeConstraintExistsGrounded(store, vettore, grafo, input);
        return searchSolutionBoolean(store, vettore);
    }

    public boolean searchSolutionAlphaGrounded_ForAll(int[] input, int budget) {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintAlphaConflictFree(store, vettore, grafo, budget);
        imposeConstraintAdmissibleSet(store, vettore, grafo);
        imposeConstraintCompleteExtensions(store, vettore, grafo);
        imposeConstraintExistsGroundedFA(store, vettore, grafo, input);
        return !searchSolutionBoolean(store, vettore);
    }

    public boolean searchSolutionAlphaMinimalSubset(int[] input, int budget) {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintAlphaConflictFreeMinimal(store, vettore, grafo, budget);
        imposeConstraintAdmissibleSet(store, vettore, grafo);
        imposeConstraintCompleteExtensions(store, vettore, grafo);
        imposeConstraintEqInput(store, vettore, input);
        if (store.consistency()) {
            Store nuovo = new Store();
            vettore.clear();
            graph2intvar(grafo, nuovo, vettore);
            imposeConstraintAlphaConflictFreeMinimal_lw(nuovo, vettore, grafo, budget);
            imposeConstraintAdmissibleSet(nuovo, vettore, grafo);
            imposeConstraintCompleteExtensions(nuovo, vettore, grafo);
            imposeConstraintEqInput2(nuovo, vettore, input);
            return !searchSolutionBoolean(nuovo, vettore);
        }
        return false;
    }

    /*
     * Un insieme B è alpha-conflict free se e solo se la produttoria (considerando il
     * semiring) degli attacchi in B deve essere migliore della soglia alpha
     */
    public int[][] searchSolutionAlphaConflictFree(int alpha) {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintAlphaConflictFree(store, vettore, grafo, alpha);
        int[][] soluzioneComplete = searchSolution(store, vettore);
        return soluzioneComplete;
    }

    /*
     * Un insieme B è alpha-stable se è alpha-conflict free e, per ogni
     * argomento c che non appartiene a B, questo viene attaccato da almeno un
     * elemento in B
     */
    public int[][] searchSolutionAlphaStable(int alpha) {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintAlphaConflictFree(store, vettore, grafo, alpha);
        //imposeConstraintStableExtensions(store, vettore, grafo);
        // Nuova Versione con attacco a quelli esterni maggiore di alpha
        imposeConstraintAlphaStableSet(store, vettore, grafo, alpha);
        int[][] soluzioneStable = searchSolution(store, vettore);
        return soluzioneStable;
    }

    /*
     * Un insieme B è alpha-ammissibile se è alpha-conflict free e se e solo se
     * ogni argomento in B è difeso da B. Quindi la somma degli attacchi dei
     * nonni deve essere peggiore dell'attacco del padre del nodo considerato
     */
    public int[][] searchSolutionAlphaAdmissible(int alpha) {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintAlphaConflictFree(store, vettore, grafo, alpha);
        imposeConstraintAlphaAdmissibleSet(store, vettore, grafo);
        int[][] soluzioneComplete = searchSolution(store, vettore);
        return soluzioneComplete;
    }

    /*
     * Un insieme B è alpha-complete se è alpha-ammissibile e tutti i nodi
     * difesi da B sono in B, eccetto quei nodi che sono attaccati da B stesso.
     */
    public int[][] searchSolutionAlphaCompleteConstraints(int alpha) {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintAlphaConflictFree(store, vettore, grafo, alpha);
        imposeConstraintAlphaAdmissibleSet(store, vettore, grafo);
        imposeConstraintCompleteExtensions(store, vettore, grafo);
        int[][] soluzioneComplete = searchSolution(store, vettore);
        return soluzioneComplete;
    }

    public int[][] searchSolutionCoalitionConflictFree() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        //Vettore supporto per il symmetry breaking
        ArrayList<IntVar> vettore2 = new ArrayList<IntVar>();
        graph2intvarCoalition(grafo, store, vettore, vettore2);
        imposeSymmetryBreaking(grafo, store, vettore, vettore2);
        imposeConstraintCoalitionConflictFree(store, vettore, grafo);
        int[][] soluzioneComplete = searchCoalitionSolution(store, vettore);
        return soluzioneComplete;
    }

    public int[][] searchSolutionCoalitionStable() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        //Vettore supporto per il symmetry breaking
        ArrayList<IntVar> vettore2 = new ArrayList<IntVar>();
        graph2intvarCoalition(grafo, store, vettore, vettore2);
        imposeSymmetryBreaking(grafo, store, vettore, vettore2);
        imposeConstraintCoalitionConflictFree(store, vettore, grafo);
        imposeConstraintCoalitionStable(store, vettore, grafo);
        int[][] soluzioneComplete = searchCoalitionSolution(store, vettore);
        return soluzioneComplete;
    }

    public int[][] searchSolutionCoalitionAdmissible() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        //Vettore supporto per il symmetry breaking
        ArrayList<IntVar> vettore2 = new ArrayList<IntVar>();
        graph2intvarCoalition(grafo, store, vettore, vettore2);
        imposeSymmetryBreaking(grafo, store, vettore, vettore2);
        imposeConstraintCoalitionConflictFree(store, vettore, grafo);
        imposeConstraintCoalitionAdmissible(store, vettore, grafo);
        int[][] soluzioneComplete = searchCoalitionSolution(store, vettore);
        return soluzioneComplete;
    }

    public int[][] searchSolutionCoalitionComplete() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        //Vettore supporto per il symmetry breaking
        ArrayList<IntVar> vettore2 = new ArrayList<IntVar>();
        graph2intvarCoalition(grafo, store, vettore, vettore2);
        imposeSymmetryBreaking(grafo, store, vettore, vettore2);
        imposeConstraintCoalitionConflictFree(store, vettore, grafo);
        //imposeConstraintCoalitionAdmissible(store, vettore, grafo);
        imposeConstraintCoalitionComplete(store, vettore, grafo);
        int[][] soluzioneComplete = searchCoalitionSolution(store, vettore);
        return soluzioneComplete;
    }

    /*
     * Dato un insieme E, questo è stage se E (conflict-free) unito E+ (definito come l'insieme
     * dei nodi attaccati da E) è massimale rispetto all'inclusione
     */
    public int[][] searchSolutionStage() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintConflictFree(store, vettore, grafo);
        imposeConstraintRange(store, vettore, grafo);
        int[][] soluzioneStage = searchMaximalInclusionSolution(store, vettore);
        return soluzioneStage;
    }

    /*
     * Dato un insieme E, questo è stage se E (complete) unito E+ (definito come l'insieme
     * dei nodi attaccati da E) è massimale rispetto all'inclusione
     */
    public int[][] searchSolutionSemiStable() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintConflictFree(store, vettore, grafo);
        imposeConstraintAdmissibleSet(store, vettore, grafo);
        imposeConstraintCompleteExtensions(store, vettore, grafo);
        imposeConstraintRange(store, vettore, grafo);
        int[][] soluzioneStage = searchMaximalInclusionSolution(store, vettore);
        return soluzioneStage;
    }

    /*
     * Dobbiamo trovare quell'insieme E, massimale rispetto all'inclusione,
     * ammissibile e che sia sottoinsieme di OGNI preferred
     */
    int[][] searchSolutionIdealSemantic() {
        Store store = new Store();
        ArrayList<IntVar> vettore = new ArrayList<IntVar>();
        graph2intvar(grafo, store, vettore);
        imposeConstraintConflictFree(store, vettore, grafo);
        imposeConstraintAdmissibleSet(store, vettore, grafo);
        int[][] solutionIdealSemantic = searchMaxIdealSolution(store, vettore);
        return solutionIdealSemantic;
    }

    private int[][] searchMaximalInclusion(int[][] soluzioni) {
        int maxinclusionconstraints = 0;
        /*
         * La massima inclusione sono il minimo numero di insiemi che 
         * non si includono uno negli altri.
         */
        Store store = new Store();
        ArrayList<IntVar> variabili = new ArrayList<IntVar>();

        /*
         * Ogni soluzione ha una serie di 0 e 1 che indicano un dato nodo non preso
         * o preso. Per ogni soluzione, quindi, creo una variabile SetVar con
         * dominio bucato equivalente agli 1 della soluzione.
         */
        ArrayList<SetVar> insiemi = new ArrayList<SetVar>();
        for (int i = 0; i < soluzioni.length; i++) {
            BoundSetDomain bsd = new BoundSetDomain();
            for (int j = 0; j < soluzioni[i].length; j++) {
                if (soluzioni[i][j] == 1) {
                    bsd.addDom(new IntervalDomain(j, j));
                }
            }
            SetVar setVar = new SetVar(store, "sol_" + i, bsd);
            insiemi.add(setVar);
        }

        /*
         * Per ogni coppia di SetVar controllo l'inclusione (i incluso in j)
         * Se è incluso almeno in un insieme, l'insieme I non lo prendo, 
         * altrimenti sì. Questo equivale a dire prendo o no la i-esima soluzione.
         * Questo lo imponiamo creando un IntVar per ogni soluzione
         */
        for (int i = 0; i < insiemi.size(); i++) {

            ArrayList orInsiemi = new ArrayList();
            IntVar sol = new IntVar(store, "sol_presa_" + i, 0, 1);

            for (int j = 0; j < insiemi.size(); j++) {
                if (i != j) {
                    orInsiemi.add(new AinB(insiemi.get(i), insiemi.get(j)));
                }
            }

            variabili.add(sol);
            store.impose(new IfThenElse(new Or(orInsiemi), new XeqC(variabili.get(i), 0), new XeqC(variabili.get(i), 1)));
            maxinclusionconstraints++;
            nconstraints++;
        }

        /*
         * Qui ricostruisco la matrice della soluzione dagli insiemi selezionati (massimi per inclusione)
         */
        int[][] solPrese = searchMinSolutionWithCSPOptimization(store, variabili); // computiamo la soluzione
        int numSol = 0;
        for (int i = 0; i < solPrese[0].length; i++) {
            numSol += solPrese[0][i];
        }
        int[][] sol = new int[numSol][soluzioni[0].length];
        int idx = 0;
        for (int i = 0; i < solPrese[0].length; i++) {
            if (solPrese[0][i] == 1) {
                sol[idx] = soluzioni[i];
                idx++;
            }
        }
        
        //System.out.println("Maximal Inclusion constraints: " + maxinclusionconstraints);

        return sol;
    }
    
    private int[][] searchMinimalInclusion(int[][] soluzioni) {
        int maxinclusionconstraints = 0;
        /*
         * La massima inclusione sono il minimo numero di insiemi che 
         * non si includono uno negli altri.
         */
        Store store = new Store();
        ArrayList<IntVar> variabili = new ArrayList<IntVar>();

        /*
         * Ogni soluzione ha una serie di 0 e 1 che indicano un dato nodo non preso
         * o preso. Per ogni soluzione, quindi, creo una variabile SetVar con
         * dominio bucato equivalente agli 1 della soluzione.
         */
        ArrayList<SetVar> insiemi = new ArrayList<SetVar>();
        for (int i = 0; i < soluzioni.length; i++) {
            BoundSetDomain bsd = new BoundSetDomain();
            for (int j = 0; j < soluzioni[i].length; j++) {
                if (soluzioni[i][j] == 1) {
                    bsd.addDom(new IntervalDomain(j, j));
                }
            }
            SetVar setVar = new SetVar(store, "sol_" + i, bsd);
            insiemi.add(setVar);
        }

        /*
         * Per ogni coppia di SetVar controllo l'inclusione (i incluso in j)
         * Se è incluso almeno in un insieme, l'insieme I non lo prendo, 
         * altrimenti sì. Questo equivale a dire prendo o no la i-esima soluzione.
         * Questo lo imponiamo creando un IntVar per ogni soluzione
         */
        for (int i = 0; i < insiemi.size(); i++) {

            ArrayList andInsiemi = new ArrayList();
            IntVar sol = new IntVar(store, "sol_presa_" + i, 0, 1);

            for (int j = 0; j < insiemi.size(); j++) {
                if (i != j) {
                    andInsiemi.add(new AinB(insiemi.get(i), insiemi.get(j)));
                }
            }

            variabili.add(sol);
            store.impose(new IfThenElse(new And(andInsiemi), new XeqC(variabili.get(i), 1), new XeqC(variabili.get(i), 0)));
            maxinclusionconstraints++;
            nconstraints++;
        }
        
        /*
         * Qui ricostruisco la matrice della soluzione dagli insiemi selezionati (massimi per inclusione)
         */
        int[][] solPrese = searchSolution(store, variabili); // computiamo la soluzione
        int numSol = 0;
        for (int i = 0; i < solPrese[0].length; i++) {
            numSol += solPrese[0][i];
        }
        int[][] sol = new int[numSol][soluzioni[0].length];
        int idx = 0;
        for (int i = 0; i < solPrese[0].length; i++) {
            if (solPrese[0][i] == 1) {
                sol[idx] = soluzioni[i];
                idx++;
            }
        }
        
        //System.out.println("Minimal Inclusion constraints: " + maxinclusionconstraints);

        return sol;
    }

    // *-*-*-* SOLUZIONI *-*-*-*
    private void imposeConstraintConflictFree(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph) {
        int conflictfreeconstraints = 0;
        int nnodes = graph.getVertexCount();
        for (int i = 0; i < nnodes; i++) {
            Collection predecessors = graph.getPredecessors(new Integer(i));
            Iterator<Integer> iter = predecessors.iterator();
            while (iter.hasNext()) {
                Integer k = iter.next();
                store.impose(new ExtensionalConflictVA(new IntVar[]{v.get(i), v.get(k.intValue())}, new int[][]{{1, 1}}));
                conflictfreeconstraints++;
                nconstraints++;
            }
        }
    }

    private void imposeConstraintAdmissibleSet(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph) {
        int admissibleconstraints = 0;
        int nnodes = graph.getVertexCount();
        for (int i = 0; i < nnodes; i++) {
            Collection fathers = graph.getPredecessors(new Integer(i));
            Iterator<Integer> iter = fathers.iterator();
            while (iter.hasNext()) {
                ArrayList c = new ArrayList();
                Integer k = iter.next();
                Collection grandfathers = graph.getPredecessors(new Integer(k));
                Iterator<Integer> iter2 = grandfathers.iterator();
                if (iter2.hasNext()) {
                    while (iter2.hasNext()) {
                        Integer z = iter2.next();
                        c.add(new XeqC(v.get(z.intValue()), 1));
                    }
                    store.impose(new IfThen(new XeqC(v.get(i), 1), new Or(c)));
                    admissibleconstraints++;
                    nconstraints++;
                } else {
                    store.impose(new XeqC(v.get(i), 0));
                    admissibleconstraints++;
                    nconstraints++;
                }
            }
        }
    }

    private void imposeConstraintEqInput(Store store, ArrayList<IntVar> v, int[] input) {
        for (int c = 0; c < input.length; c++) {
            store.impose(new XeqC(v.get(c), input[c]));
        }
    }

    private void imposeConstraintEqInput2(Store store, ArrayList<IntVar> v, int[] input) {
        int contatore1 = 0;
        for (int c = 0; c < input.length; c++) {
            if (input[c] == 1) {
                store.impose(new XeqC(v.get(c), 1));
                contatore1++;
            }
        }
    }

    private void imposeConstraintPreferredSetSecond(Store store, int[] input, ArrayList<IntVar> output) {
        int contatore1 = 0;
        for (int c = 0; c < input.length; c++) {
            if (input[c] == 1) {
                store.impose(new XeqC(output.get(c), 1));
                contatore1++;
            }
        }
        ArrayList<IntVar> somma = new ArrayList<IntVar>();
        for (int i = 0; i < input.length; i++) {
            somma.add((IntVar) output.get(i));
        }

        IntVar sum = new IntVar(store, "sum", 0, input.length);
        output.add(sum);
        store.impose(new Sum(somma, sum));
        store.impose(new XgtC(sum, contatore1));
    }
    
    /*
     * VERSIONE CORRETTA (Capezzali, Mancini, Mignogna)
     */
    private void imposeConstraintCompleteExtensions(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph) {
     
        int completeconstraint = 0;
        int nnodes = graph.getVertexCount();
        for (int i = 0; i < nnodes; i ++) {
            
            Collection fathers = graph.getPredecessors(i);
            Iterator<Integer> iterF = fathers.iterator();
            ArrayList andFathersAttackedList = new ArrayList();
            ArrayList andFathers = new ArrayList();
            while (iterF.hasNext()) {
                Integer f = iterF.next();
                Collection grandFathers = graph.getPredecessors(f);
                Iterator<Integer> iterGF = grandFathers.iterator();
                ArrayList orGrandFathersList = new ArrayList();
                while (iterGF.hasNext()) {
                    Integer gf = iterGF.next();
                    orGrandFathersList.add(new XeqC(v.get(gf), 1)); // almeno un nonno selezionato
                }

                /* Per ogni padre che attacca I, che non è nel conflict-free, devo avere almeno un nonno che lo difende */
                andFathersAttackedList.add(new IfThen(new XeqC(v.get(f), 0), new Or(orGrandFathersList)));
                andFathers.add(new XeqC(v.get(f), 0)); // fortifico il vincolo che il padre non sia selezionato (NON è UN ATTACCO NEL CONFLICT FREE)
            }
            
            store.impose(new IfThen(new And(new And(andFathers), new And(andFathersAttackedList)), new XeqC(v.get(i), 1)));
            completeconstraint++;
            nconstraints++;
        }
        
        //System.out.println("Complete constraints: " + completeconstraint);
        
    }

    private void imposeConstraintStableExtensions(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph) {
        int stableconstraints = 0;
        int nnodes = graph.getVertexCount();
        /*
         * Per ogni nodo, controllo i suoi padri. Se non ho padri, devo
         * aggiungere il vincolo che quel nodo deve appartenere alla soluzione.
         * Altrimenti se il nodo considerato non appartiene alla soluzione,
         * allora deve essere selezionato almeno un padre.
         */
        for (int i = 0; i < nnodes; i++) {
            Collection fathers = graph.getPredecessors(new Integer(i));
            Iterator<Integer> iter = fathers.iterator();
            if (!iter.hasNext()) {
                store.impose(new XeqC(v.get(i), 1));
                stableconstraints++;
                nconstraints++;
            } else {
                ArrayList fathersOrList = new ArrayList();
                while (iter.hasNext()) {
                    Integer f = iter.next();
                    fathersOrList.add(new XeqC(v.get(f), 1));
                }
                store.impose(new IfThen(new XeqC(v.get(i), 0), new Or(fathersOrList))); // almeno un padre mi attacca
                stableconstraints++;
                nconstraints++;
            }
        }
        
        //System.out.println("Stable Constraints: " + stableconstraints);
    }

    private void imposeConstraintAlphaConflictFree(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph, int alpha) {
        int alphaconflictfreeconstraints = 0;
        Object[] edgeArray = graph.getEdges().toArray();
        /*
         * Facciamo uso della variabile COST per calcolare il costo di tutte le soluzione.
         */
        IntVar costo;
        int semiringTop;
        if (SemConArg.semiringType == SemiringType.fuzzy) {
            semiringTop = SemConArg.maxWeightForFuzzySemiring;
            costo = new IntVar(store, "cost", alpha, semiringTop);
        } else {
            semiringTop = 0;
            costo = new IntVar(store, "cost", semiringTop, alpha);
        }
        
        IntVar[] insiemeArchi = new IntVar[graph.getEdgeCount()];
        IntVar myEdge;

        for (int i = 0; i < insiemeArchi.length; i++) {
            /*
             * Definiamo una IntVar con dominio bucato, che sarà del tipo
             * [semiringTop, semiringTop] e [peso, peso].
             */
            IntervalDomain domain = new IntervalDomain();
            int peso = ((MyEdge) edgeArray[i]).getPeso();
            domain.addDom(new BoundDomain(semiringTop, semiringTop));
            domain.addDom(new BoundDomain(peso, peso));

            myEdge = new IntVar(store, "E" + i, domain);
            insiemeArchi[i] = myEdge;
        }
        
        /*
         * Questo vincolo fa la produttoria dei costi e impongo che il costo sia
         * migliore di alpha (in base al semiring scelto)
         */

        if (SemConArg.semiringType == SemiringType.fuzzy) {
            store.impose(new Min(insiemeArchi, costo));
            store.impose(new XgteqC(costo, alpha));
        } else {
            store.impose(new Sum(insiemeArchi, costo));
            store.impose(new XlteqC(costo, alpha));
        }
        alphaconflictfreeconstraints += 2;
        nconstraints += 2;
        /*
         * Per ogni arco controllo che gli estremi siano selezionati. Se sono
         * ENTRAMBI selezionati, allora l'arco è selezionato e rientra, così,
         * nel calcolo della soluzione.
         */
        for (int j = 0; j < edgeArray.length; j++) {
            MyEdge arco = (MyEdge) edgeArray[j];
            Pair<Integer> estremi = (Pair<Integer>) graph.getEndpoints(arco);

            store.impose(new IfThen(new And(new XeqC(v.get(estremi.getFirst()), 1), new XeqC(v.get(estremi.getSecond()), 1)), new XeqC((IntVar) store.vars[SemConArg.numNodi + j + 1], arco.getPeso())));
            store.impose(new IfThen(new And(new XeqC(v.get(estremi.getFirst()), 0), new XeqC(v.get(estremi.getSecond()), 0)), new XeqC((IntVar) store.vars[SemConArg.numNodi + j + 1], semiringTop)));
            store.impose(new IfThen(new And(new XeqC(v.get(estremi.getFirst()), 0), new XeqC(v.get(estremi.getSecond()), 1)), new XeqC((IntVar) store.vars[SemConArg.numNodi + j + 1], semiringTop)));
            store.impose(new IfThen(new And(new XeqC(v.get(estremi.getFirst()), 1), new XeqC(v.get(estremi.getSecond()), 0)), new XeqC((IntVar) store.vars[SemConArg.numNodi + j + 1], semiringTop)));

            alphaconflictfreeconstraints += 4;
            nconstraints += 4;
        }

        v.add(costo);
        //System.out.println("Alpha-Conflict Free Constraints: " + alphaconflictfreeconstraints);
    }

    private void imposeConstraintAlphaConflictFreeMinimal(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph, int budget) {
        int conflictfreeconstraintsW = 0;
        Object[] edgeArray = graph.getEdges().toArray();
        IntVar cost = new IntVar(store, "cost", 0, budget);
        IntVar[] insiemeArchi = new IntVar[graph.getEdgeCount()];
        int[] pesiArchi = new int[graph.getEdgeCount()];
        for (int i = 0; i < insiemeArchi.length; i++) {
            insiemeArchi[i] = new IntVar(store, "E" + i, 0, 1);
        }
        for (int i = 0; i < pesiArchi.length; i++) {
            MyEdge arco = (MyEdge) edgeArray[i];
            pesiArchi[i] = arco.getPeso();
        }
        store.impose(new SumWeight(insiemeArchi, pesiArchi, cost));
        conflictfreeconstraintsW++;
        nconstraints++;
        for (int j = 0; j < edgeArray.length; j++) {
            MyEdge arco = (MyEdge) edgeArray[j];
            Pair<Integer> coppia = (Pair<Integer>) graph.getEndpoints(arco);
            store.impose(new IfThen(new And(new XeqC(v.get(coppia.getFirst()), 1), new XeqC(v.get(coppia.getSecond()), 1)), new XeqC((IntVar) store.vars[SemConArg.numNodi + j + 1], 1)));
            store.impose(new IfThen(new And(new XeqC(v.get(coppia.getFirst()), 0), new XeqC(v.get(coppia.getSecond()), 0)), new XeqC((IntVar) store.vars[SemConArg.numNodi + j + 1], 0)));
            store.impose(new IfThen(new And(new XeqC(v.get(coppia.getFirst()), 0), new XeqC(v.get(coppia.getSecond()), 1)), new XeqC((IntVar) store.vars[SemConArg.numNodi + j + 1], 0)));
            store.impose(new IfThen(new And(new XeqC(v.get(coppia.getFirst()), 1), new XeqC(v.get(coppia.getSecond()), 0)), new XeqC((IntVar) store.vars[SemConArg.numNodi + j + 1], 0)));
            conflictfreeconstraintsW++;
            nconstraints++;
        }
        store.impose(new XeqC(cost, budget));
        v.add(cost);
        conflictfreeconstraintsW++;
        nconstraints++;
    }

    private void imposeConstraintAlphaConflictFreeMinimal_lw(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph, int budget) {
        int conflictfreeconstraintsW = 0;
        Object[] edgeArray = graph.getEdges().toArray();
        IntVar cost = new IntVar(store, "cost", 0, budget);
        IntVar[] insiemeArchi = new IntVar[graph.getEdgeCount()];
        int[] pesiArchi = new int[graph.getEdgeCount()];
        for (int i = 0; i < insiemeArchi.length; i++) {
            insiemeArchi[i] = new IntVar(store, "E" + i, 0, 1);
        }
        for (int i = 0; i < pesiArchi.length; i++) {
            MyEdge arco = (MyEdge) edgeArray[i];
            pesiArchi[i] = arco.getPeso();
        }
        store.impose(new SumWeight(insiemeArchi, pesiArchi, cost));
        conflictfreeconstraintsW++;
        nconstraints++;
        for (int j = 0; j < edgeArray.length; j++) {
            MyEdge arco = (MyEdge) edgeArray[j];
            Pair<Integer> coppia = (Pair<Integer>) graph.getEndpoints(arco);
            store.impose(new IfThen(new And(new XeqC(v.get(coppia.getFirst()), 1), new XeqC(v.get(coppia.getSecond()), 1)), new XeqC((IntVar) store.vars[SemConArg.numNodi + j + 1], 1)));
            store.impose(new IfThen(new And(new XeqC(v.get(coppia.getFirst()), 0), new XeqC(v.get(coppia.getSecond()), 0)), new XeqC((IntVar) store.vars[SemConArg.numNodi + j + 1], 0)));
            store.impose(new IfThen(new And(new XeqC(v.get(coppia.getFirst()), 0), new XeqC(v.get(coppia.getSecond()), 1)), new XeqC((IntVar) store.vars[SemConArg.numNodi + j + 1], 0)));
            store.impose(new IfThen(new And(new XeqC(v.get(coppia.getFirst()), 1), new XeqC(v.get(coppia.getSecond()), 0)), new XeqC((IntVar) store.vars[SemConArg.numNodi + j + 1], 0)));
            conflictfreeconstraintsW++;
            nconstraints++;
        }
        store.impose(new XltC(cost, budget));
        v.add(cost);
        conflictfreeconstraintsW++;
        nconstraints++;
    }

    private void imposeConstraintExistsGrounded(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph, int[] input) {
        for (int c = 0; c < input.length; c++) {
            if (input[c] == 1) {
                store.impose(new XeqC(v.get(c), 1));
            }
        }
    }

    private void imposeConstraintExistsGroundedFA(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph, int[] input) {
        for (int c = 0; c < input.length; c++) {
            if (input[c] == 1) {
                store.impose(new XeqC(v.get(c), 0));
            }
        }
        IntVar somma = new IntVar(store, "somma", 0, v.size());
        store.impose(new Sum(v, somma));
        store.impose(new XgtC(somma, 0));
    }

    private void imposeConstraintAlphaAdmissibleSet(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph) {
        int admissibleconstraints = 0;
        int nnodes = graph.getVertexCount();

        int semiringTop;
        if (SemConArg.semiringType == SemiringType.fuzzy) {
            semiringTop = SemConArg.maxWeightForFuzzySemiring;
        } else {
            semiringTop = 0;
        }

        // Per ogni nodo considero i padri
        for (int i = 0; i < nnodes; i++) {
            Collection fathers = graph.getPredecessors(i);
            Iterator<Integer> iterF = fathers.iterator();
            //Per il padre corrente, considero i suoi padri, quindi i nonni del nodo I
            while (iterF.hasNext()) {
                ArrayList<IntVar> grandFathersStrenghtAttacks = new ArrayList();
                ArrayList orListGrandFathers = new ArrayList();
                Integer f = iterF.next();  //padre                
                
                /*
                 * Trovo l'arco dal padre al figlio e lo clono. Imposto il vincolo
                 * che se il padre non è stato selezionato e il figlio sì, vuol
                 * dire che c'è un attacco esterno all'insieme conflict free.
                 * Allora devo considerare il nodo clonato per poi verificare che
                 * gli attacchi dei nonni (se esistenti nel conflict free) sono
                 * peggiori dell'attacco dal padre al figlio
                 */
                MyEdge attaccoPadreFiglio = ((MyEdge) graph.findEdge(f, i));
                IntervalDomain dom = new IntervalDomain();
                dom.addDom(new BoundDomain(semiringTop, semiringTop));
                dom.addDom(new BoundDomain(attaccoPadreFiglio.getPeso(), attaccoPadreFiglio.getPeso()));
                IntVar cloneArcoPadreFiglio = new IntVar(store, dom);
                
                store.impose( new IfThenElse( new And( new XeqC(v.get(f), 0), new XeqC(v.get(i), 1) ) , new XeqC(cloneArcoPadreFiglio, attaccoPadreFiglio.getPeso()) , new XeqC(cloneArcoPadreFiglio, semiringTop) ) );
                admissibleconstraints++;
                nconstraints++;


                Collection grandfathers = graph.getPredecessors(f);
                Iterator<Integer> iterGF = grandfathers.iterator();
                int pesoTotale = 0;

                while (iterGF.hasNext()) {

                    Integer gf = iterGF.next(); // nonno
                    orListGrandFathers.add(new XeqC(v.get(gf), 1)); // componiamo l'OR per dire di selezionare almeno un nonno

                    // ricaviamo l'arco che và dal nonno al padre (difendiamo il nipote)
                    MyEdge e = ((MyEdge) graph.findEdge(gf, f));
                    int pesoArco = e.getPeso();
                    pesoTotale += pesoArco;

                    /*
                     * Dobbiamo creare una copia degli archi che vanno dal nonno
                     * al figlio, in quanto non ci possiamo basare sugli archi
                     * che abbiamo utilizzato in conflit-free in quanto essi
                     * vengono raccolti solo se ambedue i vertici vengono presi.
                     * Nel nostro caso invece non avremo mai che tutti e due i
                     * vertici di questo arco vengano presi (in quanto il nonno
                     * _potrebbe_ essere nella soluzone, ma il figlio
                     * sicuramente no, figlio=padre che attacca il figlio)
                     */
                    IntervalDomain domain = new IntervalDomain();
                    domain.addDom(new BoundDomain(semiringTop, semiringTop));
                    domain.addDom(new BoundDomain(e.getPeso(), e.getPeso()));
                    IntVar cloneArcoNonnoPadre = new IntVar(store, domain);

                    /*
                     * Se abbiamo preso il nonno (vertice da cui parte l'arco)
                     * allora imponiamo il peso dell'arco come il peso dell'arco
                     * nel grafo, altrimenti a smiringTop.
                     */
                    store.impose(new IfThenElse(new XeqC(v.get(gf), 1), new XeqC(cloneArcoNonnoPadre, e.getPeso()), new XeqC(cloneArcoNonnoPadre, semiringTop)));
                    admissibleconstraints++;
                    nconstraints++;

                    grandFathersStrenghtAttacks.add(cloneArcoNonnoPadre); // verrà utilizzato per fare la funzione SUM
                }

                IntVar cost;
                PrimitiveConstraint condizione;
                if (SemConArg.semiringType == SemiringType.fuzzy) {
                    // mi ricavo la forza degli attacchi dei nonni
                    cost = new IntVar(store, "cost", 0, SemConArg.maxWeightForFuzzySemiring);
                    store.impose(new Min(grandFathersStrenghtAttacks, cost));

                    // dichiariamo quale operatore andiamo ad utilizzare per il confronto
                    condizione = new XltY(cost, cloneArcoPadreFiglio);
                } else {

                    // mi ricavo la forza degli attacchi dei nonni
                    cost = new IntVar(store, "cost", 0, pesoTotale);
                    store.impose(new Sum(grandFathersStrenghtAttacks, cost));

                    // dichiariamo quale operatore andiamo ad utilizzare per il confronto
                    condizione = new XgtY(cost, cloneArcoPadreFiglio);
                }
                admissibleconstraints++;
                nconstraints++;

                //Impongo che se il nodo I viene selezionato E esiste l'attacco del padre, allora deve valere contemporanemante che
                //almeno un nonno l'ho selezionato e la produttoria dell'attacco dei nonni è pegiore dell'attacco
                //del padre
                store.impose(new IfThen(new And(new XeqC(v.get(i), 1), new Not(new XeqC(cloneArcoPadreFiglio, semiringTop))), new And(new Or(orListGrandFathers), condizione)));
                admissibleconstraints++;
                nconstraints++;
            }
        }
        
        //System.out.println("Alpha-admissible constraints: " + admissibleconstraints);
    }
    
    
    private void imposeConstraintAlphaStableSet(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph, Integer alpha){
        int alphastableconstraints = 0;
        int wconstraints = 0;
        int nnodes = graph.getVertexCount();
        /*
         * Per ogni nodo, controllo i suoi padri. Se non ho padri, devo
         * aggiungere il vincolo che quel nodo deve appartenere alla soluzione.
         * Altrimenti se il nodo considerato non appartiene alla soluzione,
         * allora deve essere selezionato almeno un padre.
         */
        for (int i = 0; i < nnodes; i++) {
            Collection fathers = graph.getPredecessors(new Integer(i));
            Iterator<Integer> iter = fathers.iterator();
            if (!iter.hasNext()) {
                store.impose(new XeqC(v.get(i), 1));
                alphastableconstraints++;
                nconstraints++;
            } else {
                ArrayList fathersOrList = new ArrayList();
                while (iter.hasNext()) {
                    Integer f = iter.next();
                    fathersOrList.add(new XeqC(v.get(f), 1));
                }
                store.impose(new IfThen(new XeqC(v.get(i), 0), new Or(fathersOrList))); // almeno un padre mi attacca
                alphastableconstraints++;
                nconstraints++;
            }
        }
        
        //System.out.println("Alpha-stable Constraints: " + alphastableconstraints);
        
        
        // Commenta da qui fino in fondo per farlo funzionare        
        
        int semiringTop;
        if (SemConArg.semiringType == SemiringType.fuzzy) {
            semiringTop = SemConArg.maxWeightForFuzzySemiring;
        } else {
            semiringTop = 0;
        }

        // Per ogni nodo considero i padri
        for (int i = 0; i < nnodes; i++) {
            Collection fathers = graph.getPredecessors(i);
            Iterator<Integer> iterF = fathers.iterator();
            ArrayList<IntVar> fathersStrenghtAttacks = new ArrayList();
            //ArrayList orListFathers = new ArrayList();
            int pesoTotale = 0;
            while (iterF.hasNext()) {

                Integer f = iterF.next(); // nonno
                //orListFathers.add(new XeqC(v.get(f), 1)); // componiamo l'OR per dire di selezionare almeno un nonno
                ArrayList andCondition = new ArrayList();
                andCondition.add(new XeqC(v.get(f), 1));
                andCondition.add(new XeqC(v.get(i), 0));
                
                // ricaviamo l'arco che và dal nonno al padre (difendiamo il nipote)
                MyEdge e = ((MyEdge) graph.findEdge(f, i));
                int pesoArco = e.getPeso();
                pesoTotale += pesoArco;


                IntervalDomain domain = new IntervalDomain();
                domain.addDom(new BoundDomain(semiringTop, semiringTop));
                domain.addDom(new BoundDomain(e.getPeso(), e.getPeso()));
                IntVar pesoArcoPadreFiglio = new IntVar(store, domain);


                store.impose(new IfThenElse(new And(andCondition), new XeqC(pesoArcoPadreFiglio, pesoArco), new XeqC(pesoArcoPadreFiglio, semiringTop)));
                wconstraints++;
                nconstraints++;

                fathersStrenghtAttacks.add(pesoArcoPadreFiglio); // verrà utilizzato per fare la funzione SUM
            }

            if (fathersStrenghtAttacks.size() > 0) {
            
            IntVar cost;
            PrimitiveConstraint condizione;
            if (SemConArg.semiringType == SemiringType.fuzzy) {
                // mi ricavo la forza degli attacchi dei nonni
                cost = new IntVar(store, "cost", 0, SemConArg.maxWeightForFuzzySemiring);
                store.impose(new Min(fathersStrenghtAttacks, cost));
                wconstraints++;
                nconstraints++;

                // dichiariamo quale operatore andiamo ad utilizzare per il confronto
                condizione = new XltC(cost, alpha);
            } else {

                // mi ricavo la forza degli attacchi dei nonni
                cost = new IntVar(store, "cost", 0, pesoTotale);
                store.impose(new Sum(fathersStrenghtAttacks, cost));
                wconstraints++;
                nconstraints++;

                // dichiariamo quale operatore andiamo ad utilizzare per il confronto                
                condizione = new XgtC(cost, alpha);
                //System.out.println(condizione.toString());
            }
            ArrayList andCondition2 = new ArrayList();
            for (int k = 0; k < nnodes; k++) {
                andCondition2.add(new XeqC(v.get(k), 0));
            }
            
            store.impose(new IfThen(new Or(andCondition2), condizione));
            wconstraints++;
            nconstraints++;
            }
            
        }
        //System.out.println("WConstraints: " + wconstraints);
        
        
        
    }

    private void imposeSymmetryBreaking(DirectedOrderedSparseMultigraph<Integer, MyEdge> grafo, Store store, ArrayList<IntVar> v, ArrayList<IntVar> z) {
        int size = SemConArg.numNodi;

        //symmetry breaking/
        int symmetry = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                store.impose(new IfThen(new XeqC(v.get(i), j), new XlteqC(z.get(j), i)));
                store.impose(new IfThen(new XeqC(z.get(j), i), new XeqC(v.get(i), j)));
                symmetry += 2;
                nconstraints += 2;
            }
        }
        for (int i = 0; i < (size - 1); i++) {
            store.impose(new XltY(z.get(i), z.get(i + 1)));
            symmetry++;
            nconstraints++;
        }
        //System.out.println("Symmetry constraints: " + symmetry);
    }

    public void imposeConstraintCoalitionConflictFree(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph) {

        int conflictfreeconstraints = 0;
        int nnodes = graph.getVertexCount();
        for (int i = 0; i < nnodes; i++) {
            Collection predecessors = graph.getPredecessors(new Integer(i));
            Iterator<Integer> iter = predecessors.iterator();
            while (iter.hasNext()) {
                Integer k = iter.next();
                // Print graph structure
                //System.out.println("Il nodo " + i + " ha come predec " + k.intValue());
                store.impose(new XneqY(v.get(i), v.get(k.intValue())));
                conflictfreeconstraints++;
                nconstraints++;
            }
        }
        //System.out.println("Conflictfree constraints: " + conflictfreeconstraints);
    }

    public void imposeConstraintCoalitionAdmissible(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph) {

        int admissibleconstraints = 0;
        int nnodes = graph.getVertexCount();
        for (int i = 0; i < nnodes; i++) {
            Collection fathers = graph.getPredecessors(new Integer(i));
            Iterator<Integer> iter = fathers.iterator();
            while (iter.hasNext()) {
                ArrayList c = new ArrayList();
                //c.add(c1); c.add(c2); …c.add(cn);

                Integer k = iter.next();
                Collection grandfathers = graph.getPredecessors(new Integer(k));
                Iterator<Integer> iter2 = grandfathers.iterator();
                if (iter2.hasNext()) {
                    while (iter2.hasNext()) {
                        Integer z = iter2.next();
                        c.add(new XeqY(v.get(i), v.get(z.intValue())));
                    }
                    store.impose(new Or(c));
                    admissibleconstraints++;
                    nconstraints++;
                } else {
                    store.impose(new XeqC(v.get(i), -1));
                    admissibleconstraints++;
                }
            }

        }
        //System.out.println("Admissible constraints: " + admissibleconstraints);
    }

    public void imposeConstraintCoalitionComplete(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph) {

        int completeconstraints = 0;
        int nnodes = graph.getVertexCount();
        for (int i = 0; i < nnodes; i++) {
            Collection sons = graph.getSuccessors(new Integer(i));
            Iterator<Integer> iter = sons.iterator();
            while (iter.hasNext()) {
                ArrayList c = new ArrayList();

                Integer k = iter.next();
                Collection grandsons = graph.getSuccessors(new Integer(k));
                Iterator<Integer> iter2 = grandsons.iterator();
                while (iter2.hasNext()) {
                    Integer z = iter2.next();
                    c.add(new XeqY(v.get(i), v.get(z.intValue())));
                }
                store.impose(new And(c));
                completeconstraints++;
                nconstraints++;

            }

        }
        //System.out.println("Complete constraints: " + completeconstraints);
    }

    public void imposeConstraintCoalitionStable(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph) {

        int stableconstraints = 0;
        int nnodes = graph.getVertexCount();
        for (int i = 0; i < nnodes; i++) {
            for (int j = 0; i < nnodes; i++) {
                PrimitiveConstraint c1 = new XneqY(v.get(i), v.get(j));
                ArrayList cOR = new ArrayList();
                Collection fathers = graph.getPredecessors(new Integer(j));
                Iterator<Integer> iter = fathers.iterator();
                if (iter.hasNext()) {
                    while (iter.hasNext()) {
                        Integer z = iter.next();
                        cOR.add(new XeqY(v.get(i), v.get(z.intValue())));
                    }
                } else {
                    store.impose(new XeqC(v.get(i), -1));
                }
                store.impose(new IfThen(c1, new Or(cOR)));
                stableconstraints++;
            }
        }
        //System.out.println("Stable constraints: " + stableconstraints);
    }

    public  void imposeConstraintStableExtensions(Store store, IntVar[] v, DirectedOrderedSparseMultigraph graph) {
        int stableconstraints = 0;
        int nnodes = graph.getVertexCount();
        /*
         * Per ogni nodo, controllo i suoi padri. Se non ho padri, devo
         * aggiungere il vincolo che quel nodo deve appartenere alla soluzione.
         * Altrimenti se il nodo considerato non appartiene alla soluzione,
         * allora deve essere selezionato almeno un padre.
         */
        for (int i = 0; i < nnodes; i++) {
            Collection fathers = graph.getPredecessors(new Integer(i));
            Iterator<Integer> iter = fathers.iterator();
            if (!iter.hasNext()) {
                store.impose(new XeqC(v[i], 1));
                stableconstraints++;
                nconstraints++;
            } else {
                ArrayList fathersOrList = new ArrayList();
                while (iter.hasNext()) {
                    Integer f = iter.next();
                    fathersOrList.add(new XeqC(v[f], 1));
                }
                store.impose(new IfThen(new XeqC(v[i], 0), new Or(fathersOrList))); // almeno un padre mi attacca
                stableconstraints++;
                nconstraints++;
            }
        }
        
        //System.out.println("Stable Constraints: " + stableconstraints);
    }

    public void imposeConstraintAdmissibleSet(Store store, IntVar[] v, DirectedOrderedSparseMultigraph graph) {

        int admissibleconstraints = 0;
        int nnodes = graph.getVertexCount();
        for (int i = 0; i < nnodes; i++) {
            Collection fathers = graph.getPredecessors(new Integer(i));
            Iterator<Integer> iter = fathers.iterator();
            while (iter.hasNext()) {
                ArrayList c = new ArrayList();
                //c.add(c1); c.add(c2); …c.add(cn);

                Integer k = iter.next();
                Collection grandfathers = graph.getPredecessors(new Integer(k));
                Iterator<Integer> iter2 = grandfathers.iterator();
                if (iter2.hasNext()) {
                    while (iter2.hasNext()) {
                        Integer z = iter2.next();
                        c.add(new XeqY(v[i], v[z.intValue()]));
                    }
                    store.impose(new Or(c));
                    admissibleconstraints++;
                    nconstraints++;
                } else {
                    store.impose(new XeqC(v[i], -1));
                    admissibleconstraints++;
                }
            }

        }
        //System.out.println("Admissible constraints: " + admissibleconstraints);
    }

    public void imposeConstraintCompleteExtensions(Store store, IntVar[] v, DirectedOrderedSparseMultigraph graph) {
        int completeconstraints = 0;
        int nnodes = graph.getVertexCount();
        for (int i = 0; i < nnodes; i++) {
            Collection sons = graph.getSuccessors(new Integer(i));
            Iterator<Integer> iter = sons.iterator();
            while (iter.hasNext()) {
                ArrayList c = new ArrayList();

                Integer k = iter.next();
                Collection grandsons = graph.getSuccessors(new Integer(k));
                Iterator<Integer> iter2 = grandsons.iterator();
                while (iter2.hasNext()) {
                    Integer z = iter2.next();
                    c.add(new XeqY(v[i], v[z.intValue()]));
                }
                store.impose(new And(c));
                completeconstraints++;
                nconstraints++;

            }

        }
        //System.out.println("Complete constraints: " + completeconstraints);
    }

    private void imposeConstraintRange(Store store, ArrayList<IntVar> v, DirectedOrderedSparseMultigraph graph) {

        int rangeconstraints = 0;
        int vertexCount = graph.getVertexCount();

        //Creo per ogni nodo, il suo clone che verrà settato ad 1 se verrà attaccato da un nodo nell'insieme E
        for (int i = 0; i < vertexCount; i++) {
            v.add(new IntVar(store, "clone_" + i, 0, 1));
        }

        //Per ogni nodo vado a vedere suo padre e impongo che se almeno 1 padre è selezionato, allora il nodo clone
        //lo devo settare ad 1 (rientra nell'insieme E+), altrimenti non va nella soluzione. La condizione else
        //è fondamentale per diminuire lo spazio di ricerca notevolmente!
        for (int i = 0; i < vertexCount; i++) {

            Collection fathers = graph.getPredecessors(i);
            Iterator<Integer> iterF = fathers.iterator();
            ArrayList orFathersList = new ArrayList();
            while (iterF.hasNext()) {
                Integer f = iterF.next();
                orFathersList.add(new XeqC(v.get(f), 1));
            }
            store.impose(new IfThenElse(new Or(orFathersList), new XeqC(v.get(vertexCount + i), 1), new XeqC(v.get(vertexCount + i), 0)));
            rangeconstraints++;
            nconstraints++;
        }
        
        //System.out.println("Range constraints: " + rangeconstraints);
    }
}