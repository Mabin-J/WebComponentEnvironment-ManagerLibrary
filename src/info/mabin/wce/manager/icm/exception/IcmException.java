package info.mabin.wce.manager.icm.exception;

import info.mabin.wce.manager.Constant;

public class IcmException extends Exception{
	private static final long serialVersionUID = Constant.VERSION_CODE;
	
	public IcmException(){
		super();
	}
	
	public IcmException(Throwable e){
		super(e);
	}

	public IcmException(String message){
		super(message);
	}

	public IcmException(String message, Throwable e){
		super(message, e);
	}
	
	public IcmException(String packageName, String message) {
		super("'" + packageName + "' IcmException\n: " + message);
	}

	public IcmException(String packageName, String message, Throwable e) {
		super("'" + packageName + "' IcmException\n: " + message, e);
	}
}
