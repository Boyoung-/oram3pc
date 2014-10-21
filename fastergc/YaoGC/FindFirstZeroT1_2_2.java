// by Boyang
// FindFirst0 Garbled Gate of Table 1

package YaoGC;

public class FindFirstZeroT1_2_2 extends CompositeCircuit {
    public FindFirstZeroT1_2_2() {
	super(2, 2, 5, "FindFirstZeroT1_2_2");
    }

    protected void createSubCircuits() throws Exception {
	subCircuits[0] = new XOR_2_1();
	subCircuits[1] = OR_2_1.newInstance();
	subCircuits[2] = AND_2_1.newInstance();
	subCircuits[3] = new XOR_2_1();
	subCircuits[4] = new XOR_2_1();

	super.createSubCircuits();
    }

    protected void connectWires() throws Exception {
	inputWires[0].connectTo(subCircuits[1].inputWires, 0);
	inputWires[0].connectTo(subCircuits[0].inputWires, 0);
	inputWires[1].connectTo(subCircuits[1].inputWires, 1);
	inputWires[1].connectTo(subCircuits[2].inputWires, 1);
	subCircuits[0].outputWires[0].connectTo(subCircuits[2].inputWires, 0);
	subCircuits[1].outputWires[0].connectTo(subCircuits[3].inputWires, 0);
	subCircuits[2].outputWires[0].connectTo(subCircuits[4].inputWires, 0);
    }

    protected void defineOutputWires() {
	outputWires[0] = subCircuits[4].outputWires[0];
	outputWires[1] = subCircuits[3].outputWires[0];
    }

    protected void fixInternalWires() {
    	Wire internalWire = subCircuits[0].inputWires[1];
    	internalWire.fixWire(1);
	internalWire = subCircuits[3].inputWires[1];
    	internalWire.fixWire(1);
	internalWire = subCircuits[4].inputWires[1];
    	internalWire.fixWire(1);
    }
}