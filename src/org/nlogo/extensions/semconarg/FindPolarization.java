/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.semconarg;

import org.nlogo.agent.Agent;
import org.nlogo.agent.World;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;

/**
 *
 * @author Simone Gabbriellini
 */
public class FindPolarization extends DefaultReporter {

    double[] distances;
    double[] distancesCave1;
    double[] distancesCave2;
    double[] distancesCave3;
    double[] distancesCave4;
    double[] distancesCave5;
    double[] distancesCave6;
    double[] distancesCave7;
    double[] distancesCave8;
    double[] distancesCave9;
    double[] distancesCave10;
    double[] distancesCave11;
    double[] distancesCave12;
    double[] distancesCave13;
    double[] distancesCave14;
    double[] distancesCave15;
    double[] distancesCave16;
    double[] distancesCave17;
    double[] distancesCave18;
    double[] distancesCave19;
    double[] distancesCave20;

    /**
     *
     * @return
     */
    @Override
    public Syntax getSyntax() {
        return Syntax.reporterSyntax(new int[]{Syntax.NumberType()}, Syntax.ListType());
    }

    /**
     *
     * @param argmnts
     * @param cntxt
     * @return A list with polarization values for global and single caves.
     * @throws ExtensionException
     * @throws LogoException
     */
    @Override
    public LogoList report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        int caveSize = argmnts[0].getIntValue();
        int size = caveSize * (caveSize - 1);
        Agent agent = (Agent) cntxt.getAgent();
        World world = (World) agent.world();
        org.nlogo.agent.AgentSet turtles = world.turtles();
        distances = new double[turtles.count() * (turtles.count() - 1)];
        distancesCave1 = new double[size];
        distancesCave2 = new double[size];
        distancesCave3 = new double[size];
        distancesCave4 = new double[size];
        distancesCave5 = new double[size];
        distancesCave6 = new double[size];
        distancesCave7 = new double[size];
        distancesCave8 = new double[size];
        distancesCave9 = new double[size];
        distancesCave10 = new double[size];
        distancesCave11 = new double[size];
        distancesCave12 = new double[size];
        distancesCave13 = new double[size];
        distancesCave14 = new double[size];
        distancesCave15 = new double[size];
        distancesCave16 = new double[size];
        distancesCave17 = new double[size];
        distancesCave18 = new double[size];
        distancesCave19 = new double[size];
        distancesCave20 = new double[size];
        int i = 0; // global counter
        int j1 = 0; // cave counter
        int j2 = 0; // cave counter
        int j3 = 0; // cave counter
        int j4 = 0; // cave counter
        int j5 = 0; // cave counter
        int j6 = 0; // cave counter
        int j7 = 0; // cave counter
        int j8 = 0; // cave counter
        int j9 = 0; // cave counter
        int j10 = 0; // cave counter
        int j11 = 0; // cave counter
        int j12 = 0; // cave counter
        int j13 = 0; // cave counter
        int j14 = 0; // cave counter
        int j15 = 0; // cave counter
        int j16 = 0; // cave counter
        int j17 = 0; // cave counter
        int j18 = 0; // cave counter
        int j19 = 0; // cave counter
        int j20 = 0; // cave counter
        org.nlogo.agent.AgentSet.Iterator asker = turtles.iterator();
        while (asker.hasNext()) {
            Agent myself = asker.next();
            org.nlogo.agent.AgentSet.Iterator respondent = turtles.iterator();
            while (respondent.hasNext()) {
                Agent self = respondent.next();
                if (myself.id() != self.id()) {
                    int myselfLabelId = world.indexOfVariable(myself, "LABEL");
                    int selfLabelId = world.indexOfVariable(self, "LABEL");
                    int myselfSemExtId = world.indexOfVariable(myself, "FAVORITE-EXTENSION");
                    int selfSemExtId = world.indexOfVariable(self, "FAVORITE-EXTENSION");
                    double myselfLabel = (Double) myself.getVariable(myselfLabelId);
                    double selfLabel = (Double) self.getVariable(selfLabelId);
                    double findAfDistance = findAfDistance(myself, self, myselfSemExtId, selfSemExtId);
                    distances[i] = findAfDistance;
                    i++;
                    if (myselfLabel == 1.0 && selfLabel == 1.0) {
                        distancesCave1[j1] = findAfDistance;
                        j1++;
                    }
                    if (myselfLabel == 2.0 && selfLabel == 2.0) {
                        distancesCave2[j2] = findAfDistance;
                        j2++;
                    }
                    if (myselfLabel == 3.0 && selfLabel == 3.0) {
                        distancesCave3[j3] = findAfDistance;
                        j3++;
                    }
                    if (myselfLabel == 4.0 && selfLabel == 4.0) {
                        distancesCave4[j4] = findAfDistance;
                        j4++;
                    }
                    if (myselfLabel == 5.0 && selfLabel == 5.0) {
                        distancesCave5[j5] = findAfDistance;
                        j5++;
                    }
                    if (myselfLabel == 6.0 && selfLabel == 6.0) {
                        distancesCave6[j6] = findAfDistance;
                        j6++;
                    }
                    if (myselfLabel == 7.0 && selfLabel == 7.0) {
                        distancesCave7[j7] = findAfDistance;
                        j7++;
                    }
                    if (myselfLabel == 8.0 && selfLabel == 8.0) {
                        distancesCave8[j8] = findAfDistance;
                        j8++;
                    }
                    if (myselfLabel == 9.0 && selfLabel == 9.0) {
                        distancesCave9[j9] = findAfDistance;
                        j9++;
                    }
                    if (myselfLabel == 10.0 && selfLabel == 10.0) {
                        distancesCave10[j10] = findAfDistance;
                        j10++;
                    }
                    if (myselfLabel == 11.0 && selfLabel == 11.0) {
                        distancesCave11[j11] = findAfDistance;
                        j11++;
                    }
                    if (myselfLabel == 12.0 && selfLabel == 12.0) {
                        distancesCave12[j12] = findAfDistance;
                        j12++;
                    }
                    if (myselfLabel == 13.0 && selfLabel == 13.0) {
                        distancesCave13[j13] = findAfDistance;
                        j13++;
                    }
                    if (myselfLabel == 14.0 && selfLabel == 14.0) {
                        distancesCave14[j14] = findAfDistance;
                        j14++;
                    }
                    if (myselfLabel == 15.0 && selfLabel == 15.0) {
                        distancesCave15[j15] = findAfDistance;
                        j15++;
                    }
                    if (myselfLabel == 16.0 && selfLabel == 16.0) {
                        distancesCave16[j16] = findAfDistance;
                        j16++;
                    }
                    if (myselfLabel == 17.0 && selfLabel == 17.0) {
                        distancesCave17[j17] = findAfDistance;
                        j17++;
                    }
                    if (myselfLabel == 18.0 && selfLabel == 18.0) {
                        distancesCave18[j18] = findAfDistance;
                        j18++;
                    }
                    if (myselfLabel == 19.0 && selfLabel == 19.0) {
                        distancesCave19[j19] = findAfDistance;
                        j19++;
                    }
                    if (myselfLabel == 20.0 && selfLabel == 20.0) {
                        distancesCave20[j20] = findAfDistance;
                        j20++;
                    }
                }
            }
        }
        LogoListBuilder polarization = new LogoListBuilder();
        polarization.add(findPolarization(distances));
        polarization.add(findPolarization(distancesCave1));
        polarization.add(findPolarization(distancesCave2));
        polarization.add(findPolarization(distancesCave3));
        polarization.add(findPolarization(distancesCave4));
        polarization.add(findPolarization(distancesCave5));
        polarization.add(findPolarization(distancesCave6));
        polarization.add(findPolarization(distancesCave7));
        polarization.add(findPolarization(distancesCave8));
        polarization.add(findPolarization(distancesCave9));
        polarization.add(findPolarization(distancesCave10));
        polarization.add(findPolarization(distancesCave11));
        polarization.add(findPolarization(distancesCave12));
        polarization.add(findPolarization(distancesCave13));
        polarization.add(findPolarization(distancesCave14));
        polarization.add(findPolarization(distancesCave15));
        polarization.add(findPolarization(distancesCave16));
        polarization.add(findPolarization(distancesCave17));
        polarization.add(findPolarization(distancesCave18));
        polarization.add(findPolarization(distancesCave19));
        polarization.add(findPolarization(distancesCave20));
        return polarization.toLogoList();
    }

    private double findPolarization(double[] distanceToOther) {

        double meanDist = 0.0;
        for (double dd : distanceToOther) {
//            System.out.print(dd +" ");
            meanDist += dd;
        }
//        System.out.println(" ");
        meanDist /= distanceToOther.length;
        double[] meanDiff = new double[distanceToOther.length];
        for (int i = 0; i < distanceToOther.length; i++) {
            double m = distanceToOther[i] - meanDist;
            meanDiff[i] = m * m;
        }
        double polarization = 0.0;
        for (double dd : meanDiff) {
//            System.out.print(dd +" ");
            polarization += dd;
        }
//        System.out.println("");
        polarization /= meanDiff.length;
        return polarization;
    }

    private double findAfDistance(Agent myself, Agent self, int myselfSemExtId, int selfSemExtId) {
        double AFdistance = 0;
        LogoList myselfSemExt = (LogoList) myself.getVariable(myselfSemExtId);
        LogoList selfSemExt = (LogoList) self.getVariable(selfSemExtId);
        int limit = SemConArg.g.getVertexCount();
        for (double s = 0; s < limit; s++) {
            boolean seeIfMyselfMember = seeIfMember(myselfSemExt, s);
            boolean seeIfSelfMember = seeIfMember(selfSemExt, s);
//            System.out.println("myself ext: " + myselfSemExt);
//            System.out.println("self ext: " + selfSemExt);
//            System.out.println("arg: " + s + " è presente in myself: " + seeIfMyselfMember);
//            System.out.println("arg: " + s + " è presente in self: " + seeIfSelfMember);
            if ((!seeIfMyselfMember && seeIfSelfMember) || (seeIfMyselfMember && !seeIfSelfMember)) {
                AFdistance++;
            }
        }
        AFdistance /= SemConArg.g.getVertexCount();
        return AFdistance;
    }

    private boolean seeIfMember(LogoList listToSearch, Double arg) {
//        for (Object innerList : listToSearch) {
        for (Object item : listToSearch) {
            Double castedItem = (Double) item;
            if (castedItem.equals(arg)) {
//                    System.out.println("c'è!!!");
                return true;
            }
        }
//        }
//        System.out.println("non c'è!!!");
        return false;
    }
}
