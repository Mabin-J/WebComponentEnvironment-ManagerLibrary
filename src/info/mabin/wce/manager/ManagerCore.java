package info.mabin.wce.manager;

import info.mabin.wce.manager.ComponentAbstract.ComponentContext;
import info.mabin.wce.manager.ComponentAbstract.ComponentManifest;
import info.mabin.wce.manager.eventlistener.EventListenerComponentImpl;
import info.mabin.wce.manager.eventlistener.EventListenerConfigurationImpl;
import info.mabin.wce.manager.eventlistener.EventListenerIcmImpl;
import info.mabin.wce.manager.exception.ComponentAlreadyRegisteredException;
import info.mabin.wce.manager.exception.ComponentNotRegisteredException;
import info.mabin.wce.manager.exception.IcmAlreadyRegisteredException;
import info.mabin.wce.manager.exception.IcmNotRegisteredException;
import info.mabin.wce.manager.icm.IcmImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.NodeList;


public class ManagerCore {
	static Logger logger = null;
	static ManagerCore instance = new ManagerCore();
	
	static int componentCnt = 0;
	
	/**
	 * Key: PackageName
	 */
	private static Map<String, ComponentAbstract> mapComponent;
	private static List<ComponentManifest> listManifest;
	/**
	 * Key: PackageName
	 */
	private static Map<String, String> mapComponentName;
	/**
	 * Key: ClassCanonicalName
	 */
	private static Map<String, IcmImpl> mapIcm;
	
	private static List<EventListenerComponentImpl> listEventComponent;
	private static List<EventListenerIcmImpl> listEventIcm;
	/**
	 * Key: PackageName
	 */
	private static Map<String, List<EventListenerConfigurationImpl>> mapEventConfiguration;
	
	private static String pathFolderManager;
//	private static String pathFolderLibrariesManager;
	private static String pathFolderData;
	private static String pathFolderConfiguration;
	private static String pathFolderResourcesCommon;
	private static String pathFolderLogs;
	
	private static boolean isWindows;
	
	static boolean isWindows(){
		return isWindows;
	}
	
	static String getComponentName(String packageName){
		return mapComponentName.get(packageName);
	}
	
	static Set<String> getComponentPackageNames(){
		return mapComponentName.keySet();
	}
	
	static String getPathFolderData() {
		return pathFolderData;
	}

	/**
	 * Get Common Resource
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static File getResourceCommon(String path) throws IOException {
		File tmpFile = new File(pathFolderResourcesCommon + path);
		
		boolean resultOutsideManagerPath = false;
		try{
			resultOutsideManagerPath = isOutsideManagerPath(tmpFile);
		} catch (IOException e){
			throw e;
		}
		
		if(resultOutsideManagerPath){
			throw new SecurityException("Out of WCM Path (" + path + ")");
		} else {
			return tmpFile;
		}
	}
	
	

	static void registerComponent(ComponentAbstract component) throws ComponentAlreadyRegisteredException, IcmAlreadyRegisteredException{
		ComponentManifest manifest = component.getManifest();
		ComponentContext context = component.getContext();
		
		String packageName = manifest.getPackageName();
		
		if(mapComponent.containsKey(packageName)){
			throw new ComponentAlreadyRegisteredException(packageName);
		}

		eventRegisteredComponent(context.getListEventListenerComponent(), listManifest);
		mapComponentName.put(packageName, manifest.getComponentName());
		mapComponent.put(packageName, component);
		listManifest.add(manifest);
		eventRegisteredComponent(manifest);
		
		eventRegisteredIcm(context.getListEventListenerIcm(), mapIcm.keySet());
		Map<String, IcmImpl> mapTargetIcm = context.getMapIcm();
		for(String icmCanonicalName: mapTargetIcm.keySet()){
			registerIcm(icmCanonicalName, mapTargetIcm.get(icmCanonicalName));
		}
		
		registerEventComponent(context.getListEventListenerComponent());
		registerEventConfiguration(packageName, context.getListEventListenerConfiguration());
		registerEventIcm(context.getListEventListenerIcm());
	}
	
	static void unregisterComponent(ComponentAbstract component) throws ComponentNotRegisteredException, IcmNotRegisteredException{
		String packageName = component.getManifest().getPackageName();

		if(!mapComponent.containsKey(packageName)){
			throw new ComponentNotRegisteredException(packageName);
		}
		
		ComponentManifest manifest = component.getManifest();
		ComponentContext context = component.getContext();

		unregisterEventComponent(context.getListEventListenerComponent());
		unregisterEventConfiguration(packageName);
		unregisterEventIcm(context.getListEventListenerIcm());
		
		Map<String, IcmImpl> mapTargetIcm = context.getMapIcm();
		for(String icmClassName: mapTargetIcm.keySet()){
			unregisterIcm(icmClassName);
		}			
		
		mapComponentName.remove(packageName);
		mapComponent.remove(packageName);
		listManifest.remove(manifest);
		
		eventUnregisteredComponent(manifest);		
	}
	
	
	
	static boolean isRegisteredComponent(String packageName){
		return mapComponentName.containsKey(packageName);
	}

	
	
	static void registerIcm(String classCanonicalName, IcmImpl targetIcm) throws IcmAlreadyRegisteredException{
		if(mapIcm.containsKey(classCanonicalName)){
			throw new IcmAlreadyRegisteredException(classCanonicalName);
		}
		
		mapIcm.put(classCanonicalName, targetIcm);
		eventRegisteredIcm(classCanonicalName);
	}
	
	static void unregisterIcm(String classCanonicalName) throws IcmNotRegisteredException{
		if(mapIcm.containsKey(classCanonicalName)){
			mapIcm.remove(classCanonicalName);
			eventUnregisteredIcm(classCanonicalName);
		} else {
			throw new IcmNotRegisteredException(classCanonicalName);
		}
	}
	
	/**
	 * Get Icm
	 * @param icmCanonicalName
	 * @return
	 * @throws IcmNotRegisteredException
	 */
	public static IcmImpl getIcm(String icmCanonicalName) throws IcmNotRegisteredException{
		if(mapIcm.containsKey(icmCanonicalName)){
			return mapIcm.get(icmCanonicalName);
		} else {
			throw new IcmNotRegisteredException(icmCanonicalName);
		}
	}
	
	static boolean isRegisteredIcm(String classCanonicalName){
		return mapIcm.containsKey(classCanonicalName);
	}
	
	
	
	static void registerEventComponent(List<EventListenerComponentImpl> listEvent){
		listEventComponent.addAll(listEvent);
	}
	
	static void registerEventConfiguration(String packageName, List<EventListenerConfigurationImpl> listEvent){
		mapEventConfiguration.put(packageName, listEvent);
	}
	
	static void registerEventIcm(List<EventListenerIcmImpl> listEvent){
		listEventIcm.addAll(listEvent);
	}
	
	static void unregisterEventComponent(List<EventListenerComponentImpl> listEvent){
		listEventComponent.removeAll(listEvent);
	}

	static void unregisterEventConfiguration(String packageName){
		mapEventConfiguration.remove(packageName);
	}
	
	static void unregisterEventIcm(List<EventListenerIcmImpl> listEvent){
		listEventIcm.removeAll(listEvent);
	}

	
	
	private static boolean isOutsideManagerPath(File target) throws IOException{
		String tmpRealPath = null;
		
		try {
			tmpRealPath = target.getCanonicalPath();
		} catch (IOException e) {
			throw e;
		}
		
		if(tmpRealPath.startsWith(pathFolderManager)){
			return true;
		} else {
			return false;
		}
	}
	
	
	
	static void eventChangedConfiguration(String packageName, NodeList configuration) {
		List<EventListenerConfigurationImpl> listEvent = mapEventConfiguration.get(packageName);
		
		for(EventListenerConfigurationImpl event: listEvent){
			event.eventChangedConfiguration(configuration);
		}
	}
	
	private static void eventRegisteredComponent(ComponentManifest manifest){
		for(EventListenerComponentImpl event: listEventComponent){
			event.eventRegisteredComponent(manifest);
		}
	}

	private static void eventRegisteredComponent(List<EventListenerComponentImpl> listEvent, List<ComponentManifest> listManifest){
		for(EventListenerComponentImpl event: listEvent){
			event.eventRegisteredComponent(listManifest);
		}
		
	}

	private static void eventUnregisteredComponent(ComponentManifest manifest){
		for(EventListenerComponentImpl event: listEventComponent){
			event.eventUnregisteredComponent(manifest);
		}
	}

	private static void eventRegisteredIcm(String classCanonicalName){
		for(EventListenerIcmImpl event: listEventIcm){
			event.eventRegisteredIcm(classCanonicalName);
		}
	}

	private static void eventRegisteredIcm(List<EventListenerIcmImpl> listEvent, Set<String> listIcmClassCanonicalName){
		for(EventListenerIcmImpl event: listEvent){
			event.eventRegisteredIcm(listIcmClassCanonicalName);
		}
	}

	private static void eventUnregisteredIcm(String classCanonicalName){
		for(EventListenerIcmImpl event: listEventIcm){
			event.eventUnregisteredIcm(classCanonicalName);
		}
	}

	static void init(){
		if(componentCnt == 0){
			listManifest = new ArrayList<ComponentManifest>();
			mapComponent = new HashMap<String, ComponentAbstract>();
			mapComponentName = new HashMap<String, String>();
			mapIcm = new HashMap<String, IcmImpl>();
			listEventComponent = new ArrayList<EventListenerComponentImpl>();
			listEventIcm = new ArrayList<EventListenerIcmImpl>();
			mapEventConfiguration = new HashMap<String, List<EventListenerConfigurationImpl>>();
			
			mapComponentName.put(Constant.PACKAGE_NAME_MANAGER, Constant.COMPONENT_NAME_MANAGER);
			try {
				logger = Logger.newInstance(ManagerCore.class, Constant.PACKAGE_NAME_MANAGER, Constant.COMPONENT_NAME_MANAGER);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String pathThisClass = 
					ManagerCore.class.getClassLoader().getResource("").getPath();

			logger.info("Web Component Manager Start...");
			logger.info("This Library Path: " + pathThisClass);


			File thisClasspath = new File(pathThisClass + "../");

			try {
				pathFolderManager = thisClasspath.getCanonicalPath() + "/";
			} catch (IOException e) {
				logger.e(e);
				return;
			}


			// ========== Define Default Locations
//			pathFolderLibrariesManager 	= pathFolderManager + Constant.FOLDER_NAME_LIBRARIES + "/";
			pathFolderConfiguration 	= pathFolderManager + Constant.FOLDER_NAME_CONFIGURATIONS + "/";
			pathFolderResourcesCommon	= pathFolderManager + Constant.FOLDER_NAME_COMMONRESOURCES + "/";
			pathFolderData 				= pathFolderManager + Constant.FOLDER_NAME_DATA + "/";
			pathFolderLogs				= pathFolderManager + Constant.FOLDER_NAME_LOGS + "/";

			File tmpFolderData;
			tmpFolderData = new File(pathFolderConfiguration);
			if(!tmpFolderData.exists()){
				logger.error("Configuration Folder is not exist!, Creating...");
				tmpFolderData.mkdir();
			}

			tmpFolderData = new File(pathFolderResourcesCommon);
			if(!tmpFolderData.exists()){
				logger.error("Common Resources Folder is not exist!, Creating...");
				tmpFolderData.mkdir();
			}
			
			tmpFolderData = new File(pathFolderData);
			if(!tmpFolderData.exists()){
				logger.error("Data Folder is not exist!, Creating...");
				tmpFolderData.mkdir();
			}
			
			tmpFolderData = new File(pathFolderLogs);
			if(!tmpFolderData.exists()){
				logger.error("Logs Folder is not exist!, Creating...");
				tmpFolderData.mkdir();
			}
			
			Logger.setPath(pathFolderLogs);

			logger.info("Web Component Manager Location: " + pathFolderManager);


			// =========== Checking Windows
			String osName = System.getProperty("os.name").toLowerCase();
			if(osName.contains("win")){
				logger.info("This OS: Windows (" + osName + ")");

				isWindows = true;
				pathFolderManager = "C:" + pathFolderManager; 
			} else {
				logger.info("This OS: Non Windows (" + osName + ")");

				isWindows = false;
			}

			ConfigurationManager.loadConfigures(pathFolderConfiguration);		}
		
		componentCnt++;
	}
	
	static void destroy(){
		componentCnt--;
		
		if(componentCnt == 0){
			ConfigurationManager.destroy();

			logger.info("Manager Shutdowned!\n\n");
			logger.destroy();
			Logger.destroyAll();			
		}
	}
}
