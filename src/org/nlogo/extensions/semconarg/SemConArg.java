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
    weighted,       // indice option group = 1
    fuzzy,          // indice option group = 2
    notWeighted     // indice option group = 3
}
    // Santini's stuff
    public static int numNodi;
    public static SemiringType semiringType = SemiringType.none;
    public static int maxWeightForFuzzySemiring = 100;
    // The list of cached extension already computed.
    public static HashMap<LogoList, LogoList> cachedExt;
    // The list of cached extension along with the counter, used to hist them
    public static HashMap<LogoList, Double> extHist;
    public static Double counter;    
    // List of colors for extensions.
    public static HashMap<LogoList, Double> cachedColors;
    // Is the graph weighted?.
    public static boolean isWeighted;
    // List of weights.
    public static HashMap<String, Integer> weightList;
    // alpha weight
    public static int alpha;
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
        // report a color for the favorite extension
        primitiveManager.addPrimitive("set-color", new FavoriteColor());
        // report polarization
        primitiveManager.addPrimitive("find-polarization", new FindPolarization());
        // histogram favorite extensions
        primitiveManager.addPrimitive("hist-extensions", new HistogramExtensions());
        // weigths
        primitiveManager.addPrimitive("give-weights", new GiveWeights());
    }
}
