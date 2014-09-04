package com.clarkparsia.dig20.responses;

import com.clarkparsia.dig20.explanation.ExplanationsResponse;

public interface ResponseVisitor {

	public void visit(BooleanAskResult r);
	
	public void visit(OkResponse r);

	public void visit(ErrorResponse r);

	public void visit(ExplanationsResponse r);

	public void visit(SynonymsAskResult r);

}
