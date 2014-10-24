/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.semconarg;

import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.nlogo.api.*;

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
     *
     * @return
     */
    @Override
    public Syntax getSyntax() {
        return Syntax.reporterSyntax(new int[]{Syntax.ListType(), Syntax.StringType()}, Syntax.ListType());
    }

    /**
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
            LogoList list = argmnts[0].getList();
            double ticks = cntxt.getAgent().world().ticks();
            if (list.isEmpty()) {
                // signals a NetLogo runtime error to the modeler
                throw new ExtensionException("SemanticExtensions needs at least one agent with an AF");
            // the af is already present in the cached results
            } else if(SemConArg.cachedExt!=null && SemConArg.cachedExt.containsKey(list)){
                // gets cached semantic extensions
                LogoList get = SemConArg.cachedExt.get(list);
                // at time ticks someone used a semantic extension already known by the system
                ArrayList<Double> time = SemConArg.alreadyKnowWhen.get(get);
                // add ticks to time
                time.add(ticks);
                //
                SemConArg.alreadyKnowWhen.put(get, time);
                // save result
                ArrayList<Double> afTime = SemConArg.keyKnowWhen.get(list);
                //
                afTime.add(ticks);
                //
                SemConArg.keyKnowWhen.put(list, afTime);
                // return cached semantic extensions
                return get;
            } else {
                if(SemConArg.cachedExt==null){
                   SemConArg.cachedExt = new HashMap<LogoList, LogoList>();
                }
                // creates argumentation framework
                SemConArg.g = new DirectedOrderedSparseMultigraph<Integer, MyEdge>();
                //SemConArg.g = new DirectedSparseGraph<String, String>();
                // add new arguments
                for (int i = 0; i < list.size(); i++) {
                    SemConArg.g.addVertex(i);
                }
                SemConArg.numNodi = SemConArg.g.getVertexCount();
                // add attacks
                int linkId = 0;
                int row = 0;
                for (Iterator<Object> it = list.iterator(); it.hasNext();) {
                    LogoList l = (LogoList) it.next();
                    for (int col = 0; col < l.size(); col++) {
                        Double isLink = (Double) l.get(col);
                        if (isLink == 1) {
                            String key = (double)row + "-" + (double)col;
                            if(SemConArg.weightList == null){
                                SemConArg.g.addEdge(new MyEdge(linkId, 0, row, col), row, col, EdgeType.DIRECTED);
                            } else {
                                int weight = SemConArg.weightList.get(key);
                                SemConArg.g.addEdge(new MyEdge(linkId, weight, row, col), row, col, EdgeType.DIRECTED);
                            }
                            linkId++;
                        }
                    }
                    row++;
                }
                // find new semantic extensions
                Semantic sem = new Semantic();
                LogoList report = sem.report(argmnts, cntxt);
                // save the AF and its solutions
                SemConArg.cachedExt.put(list, report);
                ArrayList<Double> time = new ArrayList<Double>();
                time.add(ticks);
                // already
                if(SemConArg.alreadyKnowWhen==null){
                    SemConArg.alreadyKnowWhen = new HashMap<LogoList, ArrayList<Double>>();
                }
                if(SemConArg.alreadyKnowWhen.containsKey(report)){
                    SemConArg.alreadyKnowWhen.get(report).add(ticks);
                } else {
                    SemConArg.alreadyKnowWhen.put(report, time);
                }
                // key
                if(SemConArg.keyKnowWhen==null){
                    SemConArg.keyKnowWhen = new HashMap<LogoList, ArrayList<Double>>();
                }
                if(SemConArg.keyKnowWhen.containsKey(report)){
                    SemConArg.keyKnowWhen.get(list).add(ticks);
                } else {
                    SemConArg.keyKnowWhen.put(list, time);
                }
                return report;
            }
        } catch (LogoException e) {
            throw new ExtensionException(e.getMessage());
        }
    }
}
