package edu.stanford.smi.protegex.owl.owlapi.converter.exception;

public class ConversionException extends Exception {
	
	public ConversionException(String message) {
		super(message);
	}
	
	public ConversionException(Throwable t) {
		super(t);
	}
	
	public ConversionException(String message, Throwable t) {
		super(message, t);
	}
	
}
