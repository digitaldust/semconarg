/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.semconarg;

import java.util.Iterator;
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
public class ArgFrameEvolution extends DefaultReporter {

    @Override
    public LogoList report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        LogoListBuilder report = new LogoListBuilder();
        Iterator<LogoList> iterator = SemConArg.keyKnowWhen.keySet().iterator();
        LogoListBuilder oldKnowledge = new LogoListBuilder();
        while(iterator.hasNext()){
            LogoList semExt = iterator.next();
            LogoListBuilder innerOldKnowledge = new LogoListBuilder();
            innerOldKnowledge.add(semExt);
            Iterator<Double> iterator1 = SemConArg.keyKnowWhen.get(semExt).iterator();
            LogoListBuilder times = new LogoListBuilder();
            while(iterator1.hasNext()){
                times.add(iterator1.next());
            }
            innerOldKnowledge.add(times.toLogoList());
            oldKnowledge.add(innerOldKnowledge.toLogoList());
        }
        report.add(oldKnowledge.toLogoList());
        return report.toLogoList();
    }
    
}
