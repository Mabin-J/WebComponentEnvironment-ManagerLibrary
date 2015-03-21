package info.mabin.wce.manager.exception;

import info.mabin.wce.manager.Constant;

public class ComponentManifestNotFoundException extends ComponentException{
	private static final long serialVersionUID = Constant.VERSION_CODE;
	
	public ComponentManifestNotFoundException() {
		super("Component Manifest Not Found: WEB-INF/WCManifest.xml");
	}
}
