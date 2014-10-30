/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.semconarg;

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

    @Override
    public Syntax getSyntax() {
        return Syntax.reporterSyntax(new int[]{Syntax.ListType()}, Syntax.NumberType());
    }

    /**
     * Return a color based on the size of the AF. FIXME: adesso esiste una
     * probabilità di 1/138 che a due estensioni diverse venga assegnato lo
     * stesso colore.
     *
     * @param argmnts
     * @param cntxt
     * @return a double which represents a NetLogo color
     * @throws org.nlogo.api.ExtensionException
     * @throws org.nlogo.api.LogoException
     */
    @Override
    public Double report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        // retrieve the right logolist
        try {
            // result
            Double color;
            // retrieve turtle's AF as a logolist like [[0 1] [0 0]]
            LogoList list = argmnts[0].getList();
            // if there is nothing in the AF of the agent
            if (list.isEmpty()) {
                // throw an exception, something went wrong
                throw new ExtensionException("SemanticExtensions needs at least one agent with an AF");
            } else {
                // check if this framework is already present
                if (SemConArg.cachedAFColors.containsKey(list)) {
                    // if the AF is already present in the cached results
                    color = SemConArg.cachedAFColors.get(list);
                } else {
                    // retrieve agent
                    color = findEditDistance(list) * 10 + 15;
                    // save the AF and the labels associated to its arguments
                    SemConArg.cachedAFColors.put(list, color);
                }
            }
            // return the semantic extensions
            return color;
        } catch (LogoException e) {
            throw new ExtensionException(e.getMessage());
        }
    }

    /**
     * Counts how many 1's there are in the AF matrix represented as a list of
     * lists In this way we count the edit distance from this AF from an empty
     * one.
     *
     * @param list
     * @return the sum of attacks in the matrix
     */
    private Double findEditDistance(LogoList list) {
        // holds the final value
        double counter = 0;
        // original AF
        int nRows = SemConArg.first.size();
        //
        for(int i=0;i<nRows;i++){
            // original row
            LogoList original = (LogoList)SemConArg.first.get(i);
            // actual row
            LogoList actual = (LogoList)list.get(i);
            // find how different we are - nRows is ok because it is a square matrix
            // so the number of rows and columns is the same
            for(int j=0;j<nRows;j++){
                // 
                Double o = (Double) original.get(j);
                Double a = (Double) actual.get(j);
                if(!o.equals(a)){
                    counter += 1;
                }
            }
        }
//        System.out.println("Original AF " + SemConArg.first.toString());
//        System.out.println("Actual AF " + list.toString());
//        System.out.println("Distance " + counter);
        // returns how many 1's there are in the matrix
        return counter;
    }
}
