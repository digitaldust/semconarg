package org.nlogo.extensions.semconarg;

import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentException;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;

/**
 *
 * @author Dr. Simone Gabbriellini
 */
public class RetrieveLabels extends DefaultReporter {

    @Override
    public Syntax getSyntax() {
        return Syntax.reporterSyntax(new int[]{Syntax.ListType()}, Syntax.ListType());
    }
    
    @Override
    public LogoList report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        // retrieve the right logolist
        try {
            // result
            LogoList argumentLabels;
            // retrieve turtle's AF as a logolist like [[0 1] [0 0]]
            LogoList list = argmnts[0].getList();
            // if there is nothing in the AF of the agent
            if (list.isEmpty()) {
                // throw an exception, something went wrong
                throw new ExtensionException("SemanticExtensions needs at least one agent with an AF");
            } else {
                // check if this framework is already present
                if (SemConArg.cachedArgumentLabels.containsKey(list)) {
                    // if the AF is already present in the cached results
                    argumentLabels = SemConArg.cachedArgumentLabels.get(list);
                } else {
                    // retrieve world
                    org.nlogo.agent.World world = (org.nlogo.agent.World) cntxt.getAgent().world();
                    // argument list
                    LogoList argumentList = (LogoList) world.getObserverVariableByName("ARGUMENTLIST");
                    // retrieve the calling agent
                    Turtle t = (Turtle) cntxt.getAgent();
                    // retrieve his semantic extensions
                    LogoList semanticExtensions = (LogoList)t.getVariable("SEMANTIC-EXTENSIONS");
                    // find the arguments labels
                    argumentLabels = findAssociateLabels(cntxt, semanticExtensions, argumentList);
                    // save the AF and the labels associated to its arguments
                    SemConArg.cachedArgumentLabels.put(list, argumentLabels);
                }
            }
            // return the semantic extensions
            return argumentLabels;
        } catch (LogoException e) {
            throw new ExtensionException(e.getMessage());
        } catch (AgentException ex){
            throw new ExtensionException(ex.getMessage());
        }
    }
    
    public LogoList findAssociateLabels(Context cntxt, LogoList semanticExtensions, LogoList argumentList) throws ExtensionException {
        /**
         * Find if each of the argument are credolous, skeptical or not accepted
         * by reporting an array for a,b,c and extensions like [a b] [b c]: [0.5
         * 1 0.5] or for a,b and an extension like [a]: [1 0] More details can
         * be found in Appendix A to the AAMAS2015 paper.
         */
        // logo list that holds the result
        LogoListBuilder accepted = new LogoListBuilder();
        // for each of the possible arguments
        for (Object a : argumentList) {
            // the argument that must be examined
            Double argument = (Double) a;
            // counts how many times this argument is in semantic extensions
            int counter = 0;
            // for each set of extensions
            for (Object se : semanticExtensions) {
                // retrieve a set
                LogoList inner = (LogoList) se;
                // if this set contains this argument
                if (inner.contains(argument)) {
                    // increment counter
                    counter += 1;
                }
            }
            // if the argument is in all the extensions
            if (counter == semanticExtensions.size()) {
                // the argument is skeptical accepted
                accepted.add(1d);
            } else if (counter == 0) {
                // if it is in no extension then it is never accepted
                accepted.add(0d);
            } else {
                // finally if it is in some extensions then it is credolous accepted
                accepted.add(0.5);
            }
        }
        return accepted.toLogoList();
    }
}
