package com.clarkparsia.dig20;

import java.net.URI;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.responses.Response;

public interface QueryProcessor {

	public Response getResponse(URI kbURI, AskQuery q);

}
