package org.nlogo.extensions.semconarg;

import java.util.HashMap;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;

/**
 *
 * @author Simone Gabbriellini
 */
public class GiveWeights extends DefaultCommand {

    @Override
    public Syntax getSyntax() {
        return Syntax.commandSyntax(new int[]{Syntax.ListType(), Syntax.BooleanType(), Syntax.NumberType()});
    }
    
    @Override
    public void perform(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        
        LogoList weights = argmnts[0].getList();
        SemConArg.weightList = new HashMap<String, Integer>();
        for(Object o : weights){
            LogoList inner = (LogoList) o;
            String key1 = String.valueOf(inner.first());
            String key2 = String.valueOf(inner.butFirst().first());
            String key = key1 + "-" + key2;
            Integer value = (int)Math.round((Double)inner.butFirst().butFirst().first());
            SemConArg.weightList.put(key, value);
        }
        SemConArg.isWeighted = argmnts[1].getBoolean();
        SemConArg.alpha = argmnts[2].getIntValue();
    }
}
