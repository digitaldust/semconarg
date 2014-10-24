/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.semconarg;

import java.util.HashMap;
import java.util.Random;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;

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
        LogoList favorite = argmnts[0].getList();
        LogoList all = argmnts[1].getList();
        Random random = new Random();
        for (Object ext : all) {
            if(!SemConArg.cachedColors.containsKey((LogoList)ext)){
                SemConArg.cachedColors.put((LogoList)ext, (double)random.nextInt(138));
            }
        }
        return SemConArg.cachedColors.get(favorite);
    }
    
}

