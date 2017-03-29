package com.tjf;

public class EchoMessage {
	@Override
	public String toString() {
		return "EchoMessage [_id=" + _id + ", _rev=" + _rev + ", message=" + message + ", messageId=" + messageId
				+ "]";
	}

	private String message;
	private String messageId;
	private String _id;
	private String _rev;
	
	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String get_rev() {
		return _rev;
	}

	public void set_rev(String _rev) {
		this._rev = _rev;
	}

	public EchoMessage() {
		this.messageId = "0000";
		this.message = "hello tim";
	}
	
	public EchoMessage(String id, String msg) {
		this.messageId = id;
		this.message = msg;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
