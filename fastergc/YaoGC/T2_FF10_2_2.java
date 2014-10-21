// by Boyang
// FindFirst1 Garbled Gate of Table 1

package YaoGC;

public class T2_FF10_2_2 extends CompositeCircuit {
    public T2_FF10_2_2() {
	super(2, 2, 2, "T2_FF10_2_2");
    }

    protected void createSubCircuits() throws Exception {
	subCircuits[0] = T2_FF10_S_2_1.newInstance();
	subCircuits[1] = T2_FF10_O_2_1.newInstance();

	super.createSubCircuits();
    }

    protected void connectWires() throws Exception {
	inputWires[0].connectTo(subCircuits[0].inputWires, 0);
	inputWires[0].connectTo(subCircuits[1].inputWires, 0);
	inputWires[1].connectTo(subCircuits[0].inputWires, 1);
	inputWires[1].connectTo(subCircuits[1].inputWires, 1);
    }

    protected void defineOutputWires() {
	outputWires[0] = subCircuits[0].outputWires[0];
	outputWires[1] = subCircuits[1].outputWires[0];
    }

    protected void fixInternalWires() {
    }
}