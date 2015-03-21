package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class IcmException extends ComponentException{
	private static final long serialVersionUID = Constant.VERSION_CODE;

	public IcmException(String message) {
		super(message);
	}

	public IcmException(String message, Throwable e) {
		super(message, e);
	}
	
	public IcmException(String classCanonicalName, String message) {
		super("'" + classCanonicalName + "' ICM Exception: " + message);
	}
	
	public IcmException(String classCanonicalName, String message, Throwable e) {
		super("'" + classCanonicalName + "' ICM Exception: " + message, e);
	}
}
