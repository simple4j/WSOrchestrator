package org.simple4j.wsorchestrator.exception;

public class SystemException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8374067926733201898L;

	private String reasonCode = null;

	public SystemException(String reasonCode)
	{
		super();
		this.reasonCode = reasonCode;
	}

	public SystemException(String reasonCode, String message)
	{
		super(message);
		this.reasonCode = reasonCode;
	}

	public SystemException(String reasonCode, Throwable cause)
	{
		super(cause);
		this.reasonCode = reasonCode;
	}

	public SystemException(String reasonCode, String message, Throwable cause)
	{
		super(message, cause);
		this.reasonCode = reasonCode;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [reasonCode=").append(reasonCode).append("]");
		return builder.toString();
	}
	
	
}
