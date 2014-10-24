package org.nlogo.extensions.semconarg;

import java.util.Iterator;
import org.nlogo.agent.Agent;
import org.nlogo.agent.World;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;

/**
 *
 * @author Simone Gabbriellini
 */
public class HistogramExtensions extends DefaultReporter {

    /**
     * Report a list with two lists, one for semantic extensions and one for favorite extensions.
     * @param argmnts
     * @param cntxt
     * @return 
     * @throws org.nlogo.api.ExtensionException
     * @throws org.nlogo.api.LogoException
     */
    @Override
    public LogoList report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        LogoListBuilder histogram = new LogoListBuilder();
        LogoListBuilder all = new LogoListBuilder();
        LogoListBuilder favorites = new LogoListBuilder();
        Agent agent = (Agent) cntxt.getAgent();
        World world = (World) agent.world();
        org.nlogo.agent.AgentSet turtles = world.turtles();
        org.nlogo.agent.AgentSet.Iterator iterator = turtles.iterator();
        while (iterator.hasNext()) {
            Agent next = iterator.next();
            int favExt = world.indexOfVariable(next, "FAVORITE-EXTENSION");
            favorites.add(SemConArg.cachedColors.get((LogoList)next.getVariable(favExt)));
            int semExt = world.indexOfVariable(next, "SEMANTIC-EXTENSIONS");
            LogoList variable = (LogoList) next.getVariable(semExt);
            for (Object ext : variable) {
                all.add(SemConArg.cachedColors.get((LogoList)ext));
            }
        }
        histogram.add(all.toLogoList());
        histogram.add(favorites.toLogoList());
        return histogram.toLogoList();
    }
    
}
