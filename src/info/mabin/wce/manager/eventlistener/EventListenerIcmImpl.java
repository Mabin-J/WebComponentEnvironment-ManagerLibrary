package info.mabin.wce.manager.eventlistener;

import java.util.Set;

public interface EventListenerIcmImpl extends EventListenerImpl{
	public void eventRegisteredIcm(String canonicalName);
	public void eventRegisteredIcm(Set<String> setCanonicalName);
	public void eventUnregisteredIcm(String canonicalName);
}
