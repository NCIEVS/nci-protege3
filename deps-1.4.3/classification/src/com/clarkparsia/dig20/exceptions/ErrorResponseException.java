package com.clarkparsia.dig20.exceptions;

import static com.clarkparsia.dig20.responses.ErrorResponse.*;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_NOT_IMPLEMENTED_QUERY_ATTRIBUTE;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_NOT_IMPLEMENTED_QUERY_TYPE;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_CLASS;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_DATATYPE;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_DATA_PROPERTY;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_INDIVIDUAL;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_OBJECT_PROPERTY;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNKNOWN;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNRECOGNIZED_QUERY_ATTRIBUTE;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNRECOGNIZED_QUERY_TYPE;

import org.semanticweb.owl.model.OWLRuntimeException;

import com.clarkparsia.dig20.responses.ErrorResponse;

public class ErrorResponseException extends OWLRuntimeException {

	private static final long	serialVersionUID	= 2079499355724927565L;

	private static String errorCodeString(int code) {

		// Compiler won't allow the constants in a switch statement because of
		// how they are initialized
		if( code == ERROR_CODE_INTERNAL_SERVER_ERROR )
			return "Internal Server Error";
		else if( code == ERROR_CODE_NOT_IMPLEMENTED_QUERY_ATTRIBUTE )
			return "Support For Query Attribute Not Implemented";
		else if( code == ERROR_CODE_NOT_IMPLEMENTED_QUERY_TYPE )
			return "Query Type Not Implemented";
		else if( code == ERROR_CODE_UNDEFINED_CLASS )
			return "Query Referenced Undefined Class";
		else if( code == ERROR_CODE_UNDEFINED_DATA_PROPERTY )
			return "Query Referenced Undefined Data Property";
		else if( code == ERROR_CODE_UNDEFINED_DATATYPE )
			return "Query Referenced Undefined Datatype";
		else if( code == ERROR_CODE_UNDEFINED_INDIVIDUAL )
			return "Query Referenced Undefined Invidual";
		else if( code == ERROR_CODE_UNDEFINED_OBJECT_PROPERTY )
			return "Query Referenced Undefined Object Property";
		else if( code == ERROR_CODE_UNDEFINED_KB )
			return "Request Referenced Undefined KB";
		else if( code == ERROR_CODE_UNKNOWN )
			return "Unknown Error Condition";
		else if( code == ERROR_CODE_UNRECOGNIZED_QUERY_ATTRIBUTE )
			return "Query Used Unrecognized Attribute";
		else if( code == ERROR_CODE_UNRECOGNIZED_QUERY_TYPE )
			return "Unrecognized Query Type";
		else
			return "Unrecognized Error Code  (" + code + ")";
	}

	private int		code;
	private String	content;

	public ErrorResponseException(ErrorResponse r) {
		super( (r.getContent() != null && r.getContent().length() > 0)
			? errorCodeString( r.getCode() ) + " : " + r.getContent()
			: errorCodeString( r.getCode() ) );
		code = r.getCode();
		content = r.getContent();
	}

	public int getCode() {
		return code;
	}

	public String getContent() {
		return content;
	}
}
