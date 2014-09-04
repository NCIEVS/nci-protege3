package com.clarkparsia.dig20.asks;

public interface AskQuery {

	public void accept(AskQueryVisitor v);

	public String getId();

}
