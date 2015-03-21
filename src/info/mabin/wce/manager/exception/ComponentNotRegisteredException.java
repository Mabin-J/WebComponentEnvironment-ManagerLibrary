package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class ComponentNotRegisteredException extends ComponentException{
	private static final long serialVersionUID = Constant.VERSION_CODE;
	
	public ComponentNotRegisteredException(String packageName) {
		super("'" + packageName + "' Component is Not Registered");
	}
}
