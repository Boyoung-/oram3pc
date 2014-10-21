// by Boyang
// for test use

package YaoGC;

public class Concatenate extends CompositeCircuit {
    private final int l;

    public Concatenate(int l) {
	super(l, l, 0, "Concatenate");
	
	this.l = l;
    }

    protected void createSubCircuits() throws Exception {
    }

    protected void connectWires() throws Exception {
    }

    protected void defineOutputWires() {
	for (int i=0; i<l; i++)
	    outputWires[i] = inputWires[i];
    }
}