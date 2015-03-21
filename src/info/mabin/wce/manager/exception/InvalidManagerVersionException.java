package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class InvalidManagerVersionException extends ComponentException{
	private static final long serialVersionUID = Constant.VERSION_CODE;

	public InvalidManagerVersionException(String packageName, long versionManager, long versionComponent) {
		super("'" + packageName + "' Component need '" + versionComponent + "' Version. (Current Manager Version: '" + versionManager + "'");
	}
}
