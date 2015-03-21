package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class IcmAlreadyRegisteredException extends IcmException{
	private static final long serialVersionUID = Constant.VERSION_CODE;

	public IcmAlreadyRegisteredException(String classCanonicalName) {
		super("'" + classCanonicalName + "' ICM is Already Registered");
	}
}
