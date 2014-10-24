package org.nlogo.extensions.semconarg;

import java.util.HashMap;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;

/**
 *
 * @author Simone Gabbriellini
 */
public class Initialize extends DefaultCommand {

    @Override
    public void perform(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
    
        SemConArg.cachedColors = new HashMap<LogoList, Double>();
        SemConArg.g = null;
        SemConArg.numNodi = 0;
        SemConArg.cachedExt = null;
        SemConArg.alreadyKnowWhen = null;
        SemConArg.keyKnowWhen = null;
        SemConArg.isWeighted = false;
        SemConArg.weightList = null;
        SemConArg.alpha = 0;
    }
}
