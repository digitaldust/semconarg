package org.nlogo.extensions.semconarg;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.nlogo.api.*;
import org.nlogo.agent.Agent;

/**
 *
 * @author Dr. Simone Gabbriellini
 */
public class AF extends DefaultReporter {

    static int numCoalition = -1;
    //Definizione static per edge factory
    static int minWeigth = -1;
    static int maxWeigth = -1;

    public enum AlphaExtensionType {

        NONE, CONFLICT_FREE, ADMISSIBILE, COMPLETE, STABLE
    }

    /**
     * Pass some values to the reporter in the find-semantic-extensions
     * procedure
     *
     * first input: AF of the turtle second input: semantic used - here always
     * preferred
     *
     * @return a list of lists that contains the semantic extensions of this AF
     * for the preferred semantic
     */
    @Override
    public Syntax getSyntax() {
        return Syntax.reporterSyntax(new int[]{Syntax.ListType()}, Syntax.ListType());
    }

    /**
     * Called by find-semantic-extension, this method finds the semantic
     * extensions for the AF of this turtle
     *
     * @param argmnts
     * @param cntxt
     * @return
     * @throws ExtensionException
     * @throws LogoException
     */
    @Override
    public LogoList report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {

        try {
            // result
            LogoList semanticExtensions;
            // retrieve turtle's AF as a logolist like [[0 1] [0 0]]
            LogoList list = argmnts[0].getList();
            // 
            if (SemConArg.first == null) {
                SemConArg.first = list;
            }
            // if there is nothing in the AF of the agent
            if (list.isEmpty()) {
                // throw an exception, something went wrong
                throw new ExtensionException("SemanticExtensions needs at least one agent with an AF");
            } else {

                // check if this framework is already present
                if (SemConArg.cachedSemanticExtensions.containsKey(list)) {
                    // if the AF is already present in the cached results
                    semanticExtensions = SemConArg.cachedSemanticExtensions.get(list);
                } else {
                    // AF not present yet, creates it
                    DirectedOrderedSparseMultigraph<Integer, MyEdge> g = createAF(list);
                    // find new semantic extensions
                    semanticExtensions = findSemanticExtensions(g);
                    // save the AF and its solutions
                    SemConArg.cachedSemanticExtensions.put(list, semanticExtensions);
                }
            }
            // return the semantic extensions
            return semanticExtensions;
        } catch (LogoException e) {
            throw new ExtensionException(e.getMessage());
        }
    }

    /**
     * Create an AF network out of the matrix represented as a list.
     *
     * @param list the AF matrix represented as a logolist
     * @return the AF as a JUNG graph
     */
    private DirectedOrderedSparseMultigraph<Integer, MyEdge> createAF(LogoList list) {
        // instantiate the graph that holds the AF
        DirectedOrderedSparseMultigraph<Integer, MyEdge> g = new DirectedOrderedSparseMultigraph<Integer, MyEdge>();
        // each entry in the list represents a row in the AF matrix
        for (int i = 0; i < list.size(); i++) {
            // add each argument to the graph
            g.addVertex(i);
        }
        // store the AF size, used by Soluzioni
        SemConArg.numNodi = g.getVertexCount();
        // add attacks
        int linkId = 0;
        int row = 0;
        for (Iterator<Object> it = list.iterator(); it.hasNext();) {
            LogoList l = (LogoList) it.next();
            for (int col = 0; col < l.size(); col++) {
                Double isLink = (Double) l.get(col);
                if (isLink == 1) {
                    String key = (double) row + "-" + (double) col;
                    if (SemConArg.weightList == null) {
                        g.addEdge(new MyEdge(linkId, 0, row, col), row, col, EdgeType.DIRECTED);
                    } else {
                        int weight = SemConArg.weightList.get(key);
                        g.addEdge(new MyEdge(linkId, weight, row, col), row, col, EdgeType.DIRECTED);
                    }
                    linkId++;
                }
            }
            row++;
        }
        // return the result
        return g;
    }

    /**
     * Find the semantic extension for the AF represented by g
     *
     * @param g
     * @return a logolist with the semantic extensions
     */
    private LogoList findSemanticExtensions(DirectedOrderedSparseMultigraph<Integer, MyEdge> g) {

        // instantiate a new object that holds the abstract argumentation computations
        Soluzioni soluzione = new Soluzioni(g);
        // holds solutions computed by Francesco Santini's procedures
        final int[][] matriceSoluzione;
        // check if links are weighted
        if (SemConArg.isWeighted) {
            // preferred is Paolo's choice for the moment
            matriceSoluzione = soluzione.searchSolutionAlphaPreferred(SemConArg.alpha);
        } else {
            // preferred is Paolo's choice for the moment
            matriceSoluzione = soluzione.searchSolutionPreferred();
        }
        // create a NetLogo list to hold admissible extensions
        LogoListBuilder semanticExtensions = new LogoListBuilder();
        // assure that each extension is sorted, so that [0 1] and [1 0] are mapped
        // to [0 1] to facilitate further processes like counting and so on...
        // converti _matriceSoluzione_ in _se_
        for (int i = 0; i < matriceSoluzione.length; i++) {
            // build the inner logo list
            ArrayList<Double> inner = Lists.newArrayList();
            for (int j = 0; j < SemConArg.numNodi; j++) {
                // if this argument should be added
                if (matriceSoluzione[i][j] == 1) {
                    // add the argument to the set of solutions
                    inner.add((double) j);
                }
            }
            // order the values
            Collections.sort(inner);
            // inner logo list
            LogoListBuilder innerList = new LogoListBuilder();
            // transform to logolist
            for (Double d : inner) {
                innerList.add(d);
            }
            LogoList innerLogoList = innerList.toLogoList();
            // add this set to the 
            semanticExtensions.add(innerLogoList);
        }
        // UPDATE HISTOGRAM EXTENSIONS
        LogoList semExt = semanticExtensions.toLogoList();
        // if not present already, add this extension to the histogram keys
        if (!SemConArg.extHist.containsKey(semExt)) {
                // add the logolist and identify this with a counter, which will be
            // later on reported to NetLogo to histogram how many times a
            // particular extension is present
            SemConArg.extHist.put(semExt, SemConArg.counter);
            // increment the counter
            SemConArg.counter += 1;
        }
        // return semantic extension to NetLogo list
        return semExt;
    }

}
