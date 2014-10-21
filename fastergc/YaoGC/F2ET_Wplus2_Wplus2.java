// by Boyang

package YaoGC;

public class F2ET_Wplus2_Wplus2 extends CompositeCircuit {
    private final int w;
    private final int s1, s2; // sigmas

    public F2ET_Wplus2_Wplus2(int w, int s1, int s2) {
	super(w+2, w+2, 2*w+2, "F2ET_" + (w+2) + "_" + w+2);

	this.w = w;
	this.s1 = s1;
	this.s2 = s2;
    }

    protected void createSubCircuits() throws Exception {
	for (int i = 0; i < 2*w; i++)
	    subCircuits[i] = new XOR_2_1();
	subCircuits[2*w]   = new FindFirstZeroOrOne_Wplus1_Wplus1(w, false, s1);
	subCircuits[2*w+1] = new FindFirstZeroOrOne_Wplus1_Wplus1(w, false, s2);

	super.createSubCircuits();
    }

    protected void connectWires() throws Exception {
	// Enable wires
	inputWires[0].connectTo(subCircuits[2*w].inputWires, 0); 
	inputWires[1].connectTo(subCircuits[2*w+1].inputWires, 0);

	for (int i = 0; i < w; i++) {
	    inputWires[i+2].connectTo(subCircuits[2*w].inputWires, i+1); 
	    inputWires[i+2].connectTo(subCircuits[i].inputWires, 1);
	    subCircuits[2*w].outputWires[i].connectTo(subCircuits[i].inputWires, 0);
	    subCircuits[2*w].outputWires[i].connectTo(subCircuits[i+w].inputWires, 1);
	    subCircuits[i].outputWires[0].connectTo(subCircuits[2*w+1].inputWires, i+1);
	    subCircuits[2*w+1].outputWires[i].connectTo(subCircuits[i+w].inputWires, 0);
	}
    }

    protected void defineOutputWires() {
	outputWires[w+1] = subCircuits[2*w].outputWires[w];
	outputWires[w] = subCircuits[2*w+1].outputWires[w];

	for (int i = 0; i < w; i++)
	    outputWires[w-1-i] = subCircuits[i+w].outputWires[0];
    }
}