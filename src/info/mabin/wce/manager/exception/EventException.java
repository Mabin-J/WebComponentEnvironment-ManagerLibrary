package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class EventException extends ComponentException{
	private static final long serialVersionUID = Constant.VERSION_CODE;

	public EventException(String classCanonicalName, Throwable e) {
		super("'" + classCanonicalName + "' Event Exception", e);
	}
}
