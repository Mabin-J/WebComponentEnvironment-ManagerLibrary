package info.mabin.wce.manager.icm.exception;

import info.mabin.wce.manager.Constant;

public class IcmGetMethodException extends IcmException{
	private static final long serialVersionUID = Constant.VERSION_CODE;
	
	public IcmGetMethodException(Throwable e){
		super(e);
	}
}
