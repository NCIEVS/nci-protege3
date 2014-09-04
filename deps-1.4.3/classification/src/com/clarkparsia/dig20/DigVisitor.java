package com.clarkparsia.dig20;

import com.clarkparsia.dig20.asks.AskQueryVisitor;
import com.clarkparsia.dig20.responses.ResponseVisitor;

public interface DigVisitor extends ResponseVisitor, AskQueryVisitor {

}
