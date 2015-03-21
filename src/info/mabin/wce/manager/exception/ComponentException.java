package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class ComponentException extends Exception{
	private static final long serialVersionUID = Constant.VERSION_CODE;
	
	public ComponentException(){
		super();
	}
	
	public ComponentException(Throwable e){
		super(e);
	}

	public ComponentException(String message){
		super(message);
	}

	public ComponentException(String message, Throwable e){
		super(message, e);
	}
	
	public ComponentException(String packageName, String message) {
		super("'" + packageName + "' ComponentException\n: " + message);
	}

	public ComponentException(String packageName, String message, Throwable e) {
		super("'" + packageName + "' ComponentException\n: " + message, e);
	}
}
