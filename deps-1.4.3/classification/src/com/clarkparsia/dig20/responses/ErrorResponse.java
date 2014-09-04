package com.clarkparsia.dig20.responses;

public class ErrorResponse extends Message implements Response {

	// Loosely aligned with / based on HTTP Status codes. First digit is general
	// description of error code. Subsequent digits refine the error condition.

	public final static int	ERROR_CODE_INTERNAL_SERVER_ERROR;
	public final static int	ERROR_CODE_NOT_IMPLEMENTED;
	public final static int	ERROR_CODE_NOT_IMPLEMENTED_QUERY_ATTRIBUTE;
	public final static int	ERROR_CODE_NOT_IMPLEMENTED_QUERY_TYPE;
	public final static int	ERROR_CODE_SERVER_ERROR_BASE;
	public final static int	ERROR_CODE_UNDEFINED_CLASS;
	public final static int	ERROR_CODE_UNDEFINED_DATA_PROPERTY;
	public final static int	ERROR_CODE_UNDEFINED_DATATYPE;
	public final static int	ERROR_CODE_UNDEFINED_ENTITY_BASE;
	public final static int	ERROR_CODE_UNDEFINED_INDIVIDUAL;
	public final static int	ERROR_CODE_UNDEFINED_KB;
	public final static int	ERROR_CODE_UNDEFINED_OBJECT_PROPERTY;
	public final static int	ERROR_CODE_UNKNOWN;
	public final static int	ERROR_CODE_UNRECOGNIZED_BASE;
	public final static int	ERROR_CODE_UNRECOGNIZED_QUERY_ATTRIBUTE;
	public final static int	ERROR_CODE_UNRECOGNIZED_QUERY_TYPE;

	static {
		ERROR_CODE_UNKNOWN = 0;

		ERROR_CODE_UNDEFINED_ENTITY_BASE = 400;
		ERROR_CODE_UNDEFINED_CLASS = ERROR_CODE_UNDEFINED_ENTITY_BASE + 10;
		ERROR_CODE_UNDEFINED_DATA_PROPERTY = ERROR_CODE_UNDEFINED_ENTITY_BASE + 20;
		ERROR_CODE_UNDEFINED_DATATYPE = ERROR_CODE_UNDEFINED_ENTITY_BASE + 30;
		ERROR_CODE_UNDEFINED_OBJECT_PROPERTY = ERROR_CODE_UNDEFINED_ENTITY_BASE + 40;
		ERROR_CODE_UNDEFINED_INDIVIDUAL = ERROR_CODE_UNDEFINED_ENTITY_BASE + 50;
		ERROR_CODE_UNDEFINED_KB = ERROR_CODE_UNDEFINED_ENTITY_BASE + 60;

		ERROR_CODE_SERVER_ERROR_BASE = 500;
		ERROR_CODE_INTERNAL_SERVER_ERROR = ERROR_CODE_SERVER_ERROR_BASE;
		ERROR_CODE_NOT_IMPLEMENTED = ERROR_CODE_SERVER_ERROR_BASE + 10;
		ERROR_CODE_NOT_IMPLEMENTED_QUERY_TYPE = ERROR_CODE_NOT_IMPLEMENTED + 1;
		ERROR_CODE_NOT_IMPLEMENTED_QUERY_ATTRIBUTE = ERROR_CODE_NOT_IMPLEMENTED + 2;

		ERROR_CODE_UNRECOGNIZED_BASE = 600;
		ERROR_CODE_UNRECOGNIZED_QUERY_TYPE = ERROR_CODE_UNRECOGNIZED_BASE + 10;
		ERROR_CODE_UNRECOGNIZED_QUERY_ATTRIBUTE = ERROR_CODE_UNRECOGNIZED_BASE + 20;
	}

	public static ErrorResponse error(String id) {
		return new ErrorResponse( id, ERROR_CODE_UNKNOWN );
	}

	public static ErrorResponse error(String id, int code) {
		return new ErrorResponse( id, code );
	}

	public static ErrorResponse error(String id, int code, String content) {
		return new ErrorResponse( id, code, content );
	}

	private String	id;

	public ErrorResponse(String id, int code) {
		super( code );
		this.id = id;
	}

	public ErrorResponse(String id, int code, String content) {
		super( code, content );
		this.id = id;
	}

	public void accept(ResponseVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		if( !super.equals( obj ) )
			return false;
		if( obj instanceof ErrorResponse ) {
			ErrorResponse other = (ErrorResponse) obj;
			return id.equals( other.getId() );
		}
		return false;
	}

	public String getId() {
		return id;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * super.hashCode() + id.hashCode();
	}

	@Override
	public String toString() {
		return "E[" + id + "](code=" + getCode() + ",content=" + getContent() + ")";
	}
}
