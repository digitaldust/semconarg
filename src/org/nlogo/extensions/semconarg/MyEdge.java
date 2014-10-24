package org.nlogo.extensions.semconarg;


public class MyEdge {

    private int peso;
    private int id;
    private int source;
    private int dest;
    

    public MyEdge(int id, int peso, int source, int dest) {
        //this.name = name;
        this.peso = peso;
        this.id = id;
    }

    public int getPeso() {
        return this.peso;
    }

    public int getID() {
        return this.id;
    }

    public void setPeso(int edgeFlow) {
        this.peso = edgeFlow;
    }

    public String toString() {
        return ""+this.peso;
    }

    /**
     * @return the source
     */
    public int getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(int source) {
        this.source = source;
    }

    /**
     * @return the dest
     */
    public int getDest() {
        return dest;
    }

    /**
     * @param dest the dest to set
     */
    public void setDest(int dest) {
        this.dest = dest;
    }

}