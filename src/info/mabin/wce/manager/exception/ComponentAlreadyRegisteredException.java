package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class ComponentAlreadyRegisteredException extends ComponentException{
	private static final long serialVersionUID = Constant.VERSION_CODE;
	
	public ComponentAlreadyRegisteredException(String packageName) {
		super("'" + packageName + "' Component is Already Registered");
	}
}
