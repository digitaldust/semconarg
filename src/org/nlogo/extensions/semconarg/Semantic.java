package org.nlogo.extensions.semconarg;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.nlogo.api.*;

/**
 * Calls the method for the selected semantic.
 * @author Simone Gabbriellini
 */
public class Semantic extends DefaultReporter {

    @Override
    public LogoList report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        // create a NetLogo list to hold admissible extensions
        LogoListBuilder semanticExtensions = new LogoListBuilder();
        // find extension sets
        Set<Set<Double>> se;
        se = Sets.newHashSet();
        String semantic = argmnts[1].getString();
        Integer alpha = SemConArg.alpha;
        Soluzioni soluzione = new Soluzioni(SemConArg.g);
        final int[][] matriceSoluzione;
        if (!SemConArg.isWeighted) {
            if (semantic.equals("admissible")) {
                matriceSoluzione = soluzione.searchSolutionAdmissible();
            } else if (semantic.equals("complete")) {
                matriceSoluzione = soluzione.searchSolutionComplete();
            } else if (semantic.equals("grounded")) {
                matriceSoluzione = soluzione.searchSolutionGrounded();
            } else if (semantic.equals("preferred")) {
                matriceSoluzione = soluzione.searchSolutionPreferred();
            } else if (semantic.equals("stable")) {
                matriceSoluzione = soluzione.searchSolutionStable();
            } else if (semantic.equals("conflict free")) {
                matriceSoluzione = soluzione.searchSolutionConflictFree();
            } else {
                throw new ExtensionException("Accepted semantics are: admissible, complete, conflict free, grounded, preferred, stable. Please select one of these.");
            }
        } else {
            if (semantic.equals("admissible")) {
                matriceSoluzione = soluzione.searchSolutionAlphaAdmissible(alpha);
            } else if (semantic.equals("complete")) {
                matriceSoluzione = soluzione.searchSolutionAlphaCompleteConstraints(alpha);
            } else if (semantic.equals("grounded")) {
                matriceSoluzione = soluzione.searchSolutionAlphaGrounded(alpha);
            } else if (semantic.equals("preferred")) {
                matriceSoluzione = soluzione.searchSolutionAlphaPreferred(alpha);
            } else if (semantic.equals("stable")) {
                matriceSoluzione = soluzione.searchSolutionAlphaStable(alpha);
            } else if (semantic.equals("conflict free")) {
                matriceSoluzione = soluzione.searchSolutionAlphaConflictFree(alpha);
            } else {
                throw new ExtensionException("Accepted semantics are: admissible, complete, conflict free, grounded, preferred, stable. Please select one of these.");
            }
        }
        // converti _matriceSoluzione_ in _se_
        for (int i = 0; i < matriceSoluzione.length; i++) {
            Set<Double> aSolution = Sets.newHashSet();
            for (int j = 0; j < SemConArg.numNodi; j++) {
                if (matriceSoluzione[i][j] == 1) {
                    aSolution.add((double) j);
                }
            }
            se.add(aSolution);
        }
        // riprendi il corso normale
        for (Set<Double> unsortedSet : se) {
            List<Double> unsortedList = new ArrayList<Double>(unsortedSet);
            // sort in the ascending order
            List<Double> sortedList = new ArrayList<Double>(unsortedList);
            Collections.sort(sortedList, new Comparator<Double>() {
                @Override
                public int compare(Double p1, Double p2) {
                    return p1.compareTo(p2);
                }
            });
            // build result
            LogoListBuilder inner = new LogoListBuilder();
            for (Double n : sortedList) {
                inner.add(n);
            }
            // add to the result
            semanticExtensions.add(inner.toLogoList());
        }
        // return semantic extension to NetLogo list
        return semanticExtensions.toLogoList();
    }
}
