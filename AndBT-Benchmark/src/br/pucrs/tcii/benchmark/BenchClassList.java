package br.pucrs.tcii.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class BenchClassList {
	private int intVal;
	private List<long[]> listVal;
	private String strVal;
	private boolean boolVal;
	private BenchSubclass classVal;
	private ArrayList<BenchClassList> listClass;

	public BenchClassList(int size) {
		this.intVal = Integer.MIN_VALUE;
		this.boolVal = false;
		this.strVal = new String(BenchClass.class.getName());
		this.listVal = Arrays.asList(new long[] { 123L, 654L, 8709L });
		this.classVal = new BenchSubclass();

		if (size > 0) {
			this.listClass = new ArrayList<BenchClassList>(size);
			for (int x = 0; x < size; x++) {
				listClass.add(new BenchClassList(0));
			}
		} else {
			this.listClass = null;
		}
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
