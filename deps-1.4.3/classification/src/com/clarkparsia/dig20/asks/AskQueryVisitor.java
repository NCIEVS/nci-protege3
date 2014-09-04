package com.clarkparsia.dig20.asks;

import com.clarkparsia.dig20.explanation.ExplainQuery;

public interface AskQueryVisitor {

	public void visit(ConsistentQuery q);

	public void visit(ClassifyQuery q);

	public void visit(EquivalentClassesQuery q);

	public void visit(ExplainQuery q);

	public void visit(IsEquivalentClassToQuery q);

	public void visit(IsSatisfiableQuery q);

	public void visit(IsSubClassOfQuery q);

	public void visit(NamedClassesQuery q);

	public void visit(NamedDataPropertiesQuery q);

	public void visit(NamedIndividualsQuery q);

	public void visit(NamedObjectPropertiesQuery q);

	public void visit(SubClassesQuery q);

	public void visit(SuperClassesQuery q);

	public void visit(UnrecognizedQuery q);

}
