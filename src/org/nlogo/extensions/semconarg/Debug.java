/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class Debug extends DefaultReporter {

    /**
     *
     * @param argmnts
     * @param cntxt
     * @return
     * @throws ExtensionException
     * @throws LogoException
     */
    @Override
    public LogoList report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        LogoListBuilder results = new LogoListBuilder();
        results.add("SEM EXT\n");
        results.addAll(SemConArg.cachedSemanticExtensions.keySet());
        results.add("\n");
        results.addAll(SemConArg.cachedSemanticExtensions.values());
        results.add("\n");
        results.add("HISTOGRAM\n");
        results.addAll(SemConArg.extHist.keySet());
        results.add("\n");
        results.addAll(SemConArg.extHist.values());
        results.add("\n");
        results.add("COLORS\n");
        results.addAll(SemConArg.cachedAFColors.keySet());
        results.add("\n");
        results.addAll(SemConArg.cachedAFColors.values());
        results.add("\n");
        results.add("LABELS\n");
        results.addAll(SemConArg.cachedArgumentLabels.keySet());
        results.add("\n");
        results.addAll(SemConArg.cachedArgumentLabels.values());
        results.add("\n");
        return results.toLogoList();
    }
}
