package org.nlogo.extensions.semconarg;

import org.nlogo.agent.Agent;
import org.nlogo.agent.World;
import org.nlogo.api.AgentException;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;

/**
 * Retrieve a list of double that represent what extension we have in the
 * system.
 *
 * @author Dr. Simone Gabbriellini
 */
public class HistogramExtensions extends DefaultReporter {

    /**
     * Report a list for semantic extensions with values keyed in the extHist
     * hashmap, it is useful to repor x many times a value according to how much
     * time it is found in the system, then NetLogo will make the histogram.
     *
     * @param argmnts
     * @param cntxt
     * @return
     * @throws org.nlogo.api.ExtensionException
     * @throws org.nlogo.api.LogoException
     */
    @Override
    public LogoList report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        // holds the list of doubles that represents the extensions available
        LogoListBuilder histogram = new LogoListBuilder();
        // retrieve world
        World world = (World) cntxt.getAgent().world();
        // retrieve agent set
        org.nlogo.agent.AgentSet turtles = world.turtles();
        // build iterator
        org.nlogo.agent.AgentSet.Iterator iterator = turtles.iterator();
        // for each agent
        while (iterator.hasNext()) {
            // retrieve the agent
            Agent next = iterator.next();
            // retrieve the index of the semantic extensions variable
            LogoList semanticExtensions;
            try {
                semanticExtensions = (LogoList) next.getTurtleOrLinkVariable("SEMANTIC-EXTENSIONS");
                // add to histogram the correnspoding double for this extension
                histogram.add(SemConArg.extHist.get(semanticExtensions));
            } catch (AgentException ex) {
                throw new ExtensionException(ex);
            }
        }
        // return the logolist for the histogram
        return histogram.toLogoList();
    }

}
