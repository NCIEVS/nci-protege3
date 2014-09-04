package com.clarkparsia.dig20.responses;

public interface Response {

	public void accept(ResponseVisitor v);

	public String getId();

}
