package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class IcmNotRegisteredException extends ComponentException{
	private static final long serialVersionUID = Constant.VERSION_CODE;

	public IcmNotRegisteredException(String classCanonicalName) {
		super("'" + classCanonicalName + "' ICM is Not Registered");
	}
}
