/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.semconarg;

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
public class FavoriteColor extends DefaultReporter {
    
    /**
     * Return a color based on the size of the AF. 
     * FIXME: adesso esiste una probabilità di 1/138 che 
     * a due estensioni diverse venga assegnato lo stesso colore.
     * @param argmnts
     * @param cntxt
     * @return a double which represents a NetLogo color
     * @throws org.nlogo.api.ExtensionException
     * @throws org.nlogo.api.LogoException
     */
    @Override
    public Double report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        // retrieve world
        World world = (World) cntxt.getAgent().world();
        // retrieve the index of the semantic extensions variable - CAN BE DONE JUST ONCE!!!
        int AF = world.indexOfVariable(next, "AF");
        // retrieve logolist of semanti extensions for this agent
        LogoList semanticExtensions = (LogoList) next.getVariable(AF);
        // 
        return SemConArg.cachedColors.get(semanticExtensions);
    }
    
}

