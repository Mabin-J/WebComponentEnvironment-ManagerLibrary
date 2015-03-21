package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class ParsingManifestException extends ComponentException{
	private static final long serialVersionUID = Constant.VERSION_CODE;

	public ParsingManifestException(Exception e){
		super("Parsing Manifest Error.", e);
	}
}
