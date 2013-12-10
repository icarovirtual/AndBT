package br.pucrs.tcii.models;

public abstract class GameControl {
	private static final String TAG = GameControl.class.getName();

	public Object prepareTurn() {
		throw new UnsupportedOperationException(TAG + ": prepareTurn not implemented");
	}

	public Object prepareRealtime() {
		throw new UnsupportedOperationException(TAG + ": prepareRealtime not implemented");
	}

	public Object prepareBegin() {
		throw new UnsupportedOperationException(TAG + ": prepareBegin not implemented");
	}

	public Object prepareFinish() {
		throw new UnsupportedOperationException(TAG + ": prepareFinish not implemented");
	}
}
