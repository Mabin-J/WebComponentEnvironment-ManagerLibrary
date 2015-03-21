package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class ParsingConfigurationException extends ComponentException{
	private static final long serialVersionUID = Constant.VERSION_CODE;

	public ParsingConfigurationException(Exception e){
		super("Parsing Configuration Error.", e);
	}
}
