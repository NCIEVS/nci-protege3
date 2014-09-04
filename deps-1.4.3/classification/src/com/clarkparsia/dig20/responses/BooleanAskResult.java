package com.clarkparsia.dig20.responses;

public class BooleanAskResult extends BaseAskResult {

	public static BooleanAskResult result(String id, boolean b) {
		return new BooleanAskResult( id, b );
	}

	boolean	b;

	public BooleanAskResult(String id, boolean b) {
		super( id );
		this.b = b;
	}

	public void accept(ResponseVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		if( !super.equals( obj ) )
			return false;

		if( obj instanceof BooleanAskResult ) {
			BooleanAskResult r = (BooleanAskResult) obj;
			return b == r.getBoolean();
		}

		return false;
	}

	public boolean getBoolean() {
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		// The magic numbers here are from the javadoc for java.lang.Boolean
		return prime * super.hashCode() + (b
			? 1231
			: 1237);
	}

	@Override
	public String toString() {
		return super.toString() + "(" + Boolean.toString( b ) + ")";
	}
}
