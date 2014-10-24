/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.semconarg;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.nlogo.agent.Agent;
import org.nlogo.agent.Turtle;
import org.nlogo.agent.World;
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
 * @author Simone Gabbriellini
 */
public class FindPolarization extends DefaultReporter {

    /**
     *
     * @param argmnts
     * @param cntxt
     * @return A list with polarization values for global and single caves.
     * @throws ExtensionException
     * @throws LogoException
     */
    @Override
    public Double report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        // retrieve world
        World world = (World) cntxt.getAgent().world();
        // retrieve turtles
        org.nlogo.agent.AgentSet turtles = world.turtles();
        // and make iterator
        org.nlogo.agent.AgentSet.Iterator iterator = turtles.iterator();
        // argument list
        LogoList argumentList = (LogoList) world.getObserverVariableByName("ARGUMENTLIST");
        /**
         * Find if each of the argument are credolous, skeptical or not accepted
         * by reporting an array for a,b,c and extensions like [a b] [b c]: [0.5
         * 1 0.5] or for a,b and an extension like [a]: [1 0] More details can
         * be found in Appendix A to the AAMAS2015 paper.
         */
        while (iterator.hasNext()) {
            // for each agent try...
            try {
                // retrieve agent
                Agent next = iterator.next();
                // retrieve semantic extensions
                LogoList semanticExtensions = (LogoList) next.getTurtleOrLinkVariable("SEMANTIC-EXTENSIONS");
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
                        accepted.add(1);
                    } else if (counter == 0) {
                        // if it is in no extension then it is never accepted
                        accepted.add(0);
                    } else {
                        // finally if it is in some extensions then it is credolous accepted
                        accepted.add(0.5);
                    }
                }
                // store the logolist with results in the turtle
                next.setTurtleOrLinkVariable("ARGUMENTS-ACCEPTED", accepted.toLogoList());
            } catch (AgentException ex) {
                // catch exception and stop NetLogo
                throw new ExtensionException(ex);
            }
        }
        // distances
        double distances = 0;
        // how many turtles here
        int size = turtles.count();
        // find the distances between arguments-accepted lists of agents
        for(int i=0;i<(size-2);i++){
            Turtle myself = world.getTurtle(i);
            LogoList myselfAccepted = (LogoList)myself.getTurtleOrLinkVariable("ARGUMENTS-ACCEPTED");
            for(int j=i+1;j<size;j++){
                Turtle self = world.getTurtle(j);
                LogoList selfAccepted = (LogoList)self.getTurtleOrLinkVariable("ARGUMENTS-ACCEPTED");
                distances += findDistance(myselfAccepted, selfAccepted);
            }
        }
        // return polarization
        return distances / (argumentList.size() * Math.floor(size / 2) * Math.ceil(size / 2));
    }

    private double findDistance(LogoList myselfAccepted, LogoList selfAccepted) {
        // holds results
        double result = 0;
        // how many arguments
        int size = myselfAccepted.size();
        // for each argument
        for(int i = 0; i < size; i++){
            // add the absolute difference between 
            result += Math.abs((Double)myselfAccepted.get(i) - (Double)selfAccepted.get(i));
        }
        return result;
    }

}
