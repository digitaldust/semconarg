/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.semconarg;

import org.nlogo.agent.Turtle;
import org.nlogo.agent.World;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;

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
        // argument list
        LogoList argumentList = (LogoList) world.getObserverVariableByName("ARGUMENTLIST");
        // distances
        double distances = 0;
        // how many turtles here
        int size = turtles.count();
        // find the distances between arguments-accepted lists of agents
        for(int i=0;i<(size-1);i++){
            Turtle myself = world.getTurtle(i);
            LogoList myselfAccepted = (LogoList)myself.getTurtleOrLinkVariable("ARGUMENTS-ACCEPTED");
            for(int j=i+1;j<size;j++){
                Turtle self = world.getTurtle(j);
                LogoList selfAccepted = (LogoList)self.getTurtleOrLinkVariable("ARGUMENTS-ACCEPTED");
                double distance = findDistance(myselfAccepted, selfAccepted);
                distances += distance; 
            }
        }
        // return polarization
        double den = size / 2d;
        //System.out.println(distances + " / (" + argumentList.size() + " * " + Math.floor(den) + " * " + Math.ceil(den) + ")");
        return distances / (argumentList.size() * Math.floor(den) * Math.ceil(den));
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
        // return result
        return result;
    }

}
