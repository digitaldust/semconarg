/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.semconarg;

import java.util.Collection;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;

/**
 *
 * @author ogabbrie
 */
public class ExportAspartix extends DefaultReporter {

    @Override
    public Object report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        
        if (SemConArg.g == null) {
            throw new ExtensionException("please run load-AF first");
        }
        LogoListBuilder export = new LogoListBuilder();
        Collection<Integer> vertices = SemConArg.g.getVertices();
        Collection<MyEdge> edges = SemConArg.g.getEdges();
        for(Integer t:vertices){
            export.add("arg("+ t +").\n");
        }
        for(MyEdge l:edges){
            export.add("att(" + l.getSource() + "," + l.getDest() + ").\n");
        }
        return export.toLogoList();
    }
    
}
