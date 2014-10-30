package org.nlogo.extensions.semconarg;

import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import java.util.ArrayList;
import java.util.HashMap;
import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.LogoList;
import org.nlogo.api.PrimitiveManager;

/**
 * This NetLogo extension aims at evaluating semantic extensions in
 * argumentation frameworks; It relies upon ConArg, by Francesco Santini.
 *
 * @author Simone Gabbriellini
 * @version 0.1
 */
public class SemConArg extends DefaultClassManager {

    public static enum SemiringType {

        none,
        weighted, // indice option group = 1
        fuzzy, // indice option group = 2
        notWeighted     // indice option group = 3
    }
    // Santini's stuff
    public static int numNodi;
    public static SemiringType semiringType = SemiringType.none;
    public static int maxWeightForFuzzySemiring = 100;
    // Cache for each AF as a list, the corresponding semantic extensions computed
    public static HashMap<LogoList, LogoList> cachedSemanticExtensions;
    // The list of cached semantic extension along with the counter used to label each semantic extension
    public static HashMap<LogoList, Double> extHist;
    public static Double counter;
    // List of colors for extensions.
    public static HashMap<LogoList, Double> cachedAFColors;
    // list of labels for arguments in AF
    public static HashMap<LogoList, LogoList> cachedArgumentLabels;
    // Is the graph weighted?.
    public static boolean isWeighted;
    // List of weights.
    public static HashMap<String, Integer> weightList;
    // alpha weight
    public static int alpha;
    // 
    public static LogoList first;

    /**
     *
     * @param primitiveManager
     */
    @Override
    public void load(PrimitiveManager primitiveManager) {
        // initialize
        primitiveManager.addPrimitive("initialize", new Initialize());
        // solve argumentation framework
        primitiveManager.addPrimitive("find-ext", new AF());
        // assign an acceptance score to arguments according to skeptical, credolous and not acceptance
        primitiveManager.addPrimitive("find-labels", new RetrieveLabels());
        // report a color for the agent according to its AF edit distance to initial opinion
        primitiveManager.addPrimitive("set-color", new FavoriteColor());
        // report polarization
        primitiveManager.addPrimitive("find-polarization", new FindPolarization());
        // histogram favorite extensions
        primitiveManager.addPrimitive("hist-extensions", new HistogramExtensions());
        // DEBUG
        primitiveManager.addPrimitive("debug", new Debug());
        // output extension labels for histogram
        primitiveManager.addPrimitive("label-ext", new OutputExtensionLabels());

    }
}
