package com.clarkparsia.dig20.responses;

public abstract class Message {

	private static String	emptyString	= "";
	private int				code;
	private String			content;

	public Message(int code) {
		this( code, emptyString );
	}

	public Message(int code, String content) {
		if( content == null )
			throw new IllegalArgumentException( "Content cannot be null" );

		this.code = code;
		this.content = content;
	}

	@Override
	public boolean equals(Object obj) {
		if( obj instanceof Message ) {
			Message other = (Message) obj;
			if( code != other.getCode() )
				return false;
			return content.equals( other.getContent() );
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * content.hashCode() + code;
	}
	
	public int getCode() {
		return code;
	}

	public String getContent() {
		return content;
	}
}
