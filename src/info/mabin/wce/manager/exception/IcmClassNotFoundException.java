package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class IcmClassNotFoundException extends IcmException{
	private static final long serialVersionUID = Constant.VERSION_CODE;

	public IcmClassNotFoundException(String classCanonicalName, Throwable e) {
		super("'" + classCanonicalName + "' ICM Class is Not Found", e);
	}
}
