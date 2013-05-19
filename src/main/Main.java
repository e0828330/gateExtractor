package main;

import gate.Gate;
import gate.util.GateException;

public class Main {

	/**
	 * @param args
	 * @throws GateException 
	 */
	public static void main(String[] args) throws GateException {
		Gate.runInSandbox(true);
		Gate.init();
	}

}
