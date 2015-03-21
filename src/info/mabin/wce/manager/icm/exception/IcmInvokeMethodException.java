package info.mabin.wce.manager.icm.exception;

import info.mabin.wce.manager.Constant;

public class IcmInvokeMethodException extends IcmException{
	private static final long serialVersionUID = Constant.VERSION_CODE;
	
	public IcmInvokeMethodException(Throwable e){
		super(e);
	}
}
