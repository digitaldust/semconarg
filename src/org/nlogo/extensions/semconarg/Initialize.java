package org.nlogo.extensions.semconarg;

import java.util.HashMap;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;

/**
 * Initialize some data structure, done each time setup is pressed.
 * 
 * @author Dr. Simone Gabbriellini
 */
public class Initialize extends DefaultCommand {

    @Override
    public void perform(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {

        SemConArg.alpha = 0;
        SemConArg.numNodi = 0;
        SemConArg.maxWeightForFuzzySemiring = 100;
        SemConArg.isWeighted = false;
        SemConArg.weightList = null;
        SemConArg.cachedExt = new HashMap<LogoList, LogoList>();
        SemConArg.cachedColors = new HashMap<LogoList, Double>();
        SemConArg.extHist = new HashMap<LogoList, Double>();
        SemConArg.counter = 1d;
    }
    
}
