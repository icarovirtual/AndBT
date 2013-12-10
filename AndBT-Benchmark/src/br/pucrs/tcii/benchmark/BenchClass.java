package br.pucrs.tcii.benchmark;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class BenchClass {
	private int intVal;
	private List<long[]> listVal;
	private String strVal;
	private boolean boolVal;
	private BenchSubclass classVal;

	public BenchClass() {
		this.intVal = Integer.MIN_VALUE;
		this.boolVal = false;
		this.strVal = new String(BenchClass.class.getName());
		this.listVal = Arrays.asList(new long[] { 123L, 654L, 8709L });
		this.classVal = new BenchSubclass();
	}

	class BenchSubclass {
		private double doubleVal;
		private byte byteVal;

		public BenchSubclass() {
			this.doubleVal = 1234.5;
			this.byteVal = 43;
		}
	}
	
	
}
