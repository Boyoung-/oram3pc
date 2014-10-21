// by Boyang
// FindFirst0/1 Garbled Gate of Table 2

package YaoGC;

public class FindFirstZeroOrOneT2_2_2 extends CompositeCircuit {
    public FindFirstZeroOrOneT2_2_2() {
	super(2, 2, 2, "FindFirstZeroOrOneT2_2_2");
    }

    protected void createSubCircuits() throws Exception {
	subCircuits[0] = new XOR_2_1();
	subCircuits[1] = AND_2_1.newInstance();

	super.createSubCircuits();
    }

    protected void connectWires() throws Exception {
	inputWires[0].connectTo(subCircuits[0].inputWires, 0);
	inputWires[1].connectTo(subCircuits[1].inputWires, 0);
    }

    protected void defineOutputWires() {
	outputWires[0] = subCircuits[0].outputWires[0];
	outputWires[1] = subCircuits[1].outputWires[0];
    }

    protected void fixInternalWires() {
    	Wire internalWire = subCircuits[0].inputWires[1];
    	internalWire.fixWire(0);
	internalWire = subCircuits[1].inputWires[1];
    	internalWire.fixWire(0);
    }
}