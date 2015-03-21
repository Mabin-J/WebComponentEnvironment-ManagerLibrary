package info.mabin.wce.manager.eventlistener;

import info.mabin.wce.manager.ComponentAbstract.ComponentManifest;

import java.util.List;

public interface EventListenerComponentImpl extends EventListenerImpl{
	public void eventRegisteredComponent(ComponentManifest manifest);
	public void eventRegisteredComponent(List<ComponentManifest> listManifest);
	public void eventUnregisteredComponent(ComponentManifest manifest);
}
