package com.tjf;

public class ErrorMessage {
	@Override
	public String toString() {
		return "ErrorMessage [errorMessage=" + errorMessage + ", messageId="
				+ errorId + "]";
	}

	private String errorMessage;
	private String errorId;
	
	public ErrorMessage() {
		this.errorId = "99999";
		this.errorMessage = "Not sure what the problem is!";
	}
	
	public ErrorMessage(String id, String msg) {
		this.errorId = id;
		this.errorMessage = msg;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorId() {
		return errorId;
	}

	public void setErrorId(String messageId) {
		this.errorId = messageId;
	}
}
