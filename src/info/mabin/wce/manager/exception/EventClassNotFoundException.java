package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class EventClassNotFoundException extends EventException{
	private static final long serialVersionUID = Constant.VERSION_CODE;

	public EventClassNotFoundException(String classCanonicalName, Throwable e) {
		super("'" + classCanonicalName + "' Event Class is Not Found", e);
	}
}
