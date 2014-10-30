package org.nlogo.extensions.semconarg;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;

/**
 *
 * @author Dr. Simone Gabbriellini
 */
public class OutputExtensionLabels extends DefaultReporter {

    @Override
    public LogoList report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        // hold results as a list of lists
        LogoListBuilder results = new LogoListBuilder();
        SemConArg.extHist.values();
        
        // for each semantic extension in extHist
        for(LogoList l : SemConArg.extHist.keySet()){
            // save this result
            results.add(l);
            results.add(" HAS LABEL ");
            results.add(SemConArg.extHist.get(l));
            results.add("\n");
        }
        // reorder the values 
        // return the values
        return results.toLogoList();
    }
    
}
