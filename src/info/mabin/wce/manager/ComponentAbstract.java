package info.mabin.wce.manager;

import info.mabin.wce.manager.eventlistener.EventListenerComponentImpl;
import info.mabin.wce.manager.eventlistener.EventListenerConfigurationImpl;
import info.mabin.wce.manager.eventlistener.EventListenerIcmImpl;
import info.mabin.wce.manager.eventlistener.EventListenerImpl;
import info.mabin.wce.manager.exception.ComponentAlreadyRegisteredException;
import info.mabin.wce.manager.exception.ComponentException;
import info.mabin.wce.manager.exception.ComponentManifestNotFoundException;
import info.mabin.wce.manager.exception.ComponentNotRegisteredException;
import info.mabin.wce.manager.exception.EventClassNotFoundException;
import info.mabin.wce.manager.exception.EventException;
import info.mabin.wce.manager.exception.IcmAlreadyRegisteredException;
import info.mabin.wce.manager.exception.IcmClassNotFoundException;
import info.mabin.wce.manager.exception.IcmException;
import info.mabin.wce.manager.exception.IcmNotRegisteredException;
import info.mabin.wce.manager.exception.InvalidManagerVersionException;
import info.mabin.wce.manager.exception.ParsingManifestException;
import info.mabin.wce.manager.icm.IcmImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class ComponentAbstract {
	private Logger loggerManager;

	/**
	 * Component Context
	 */
	protected ComponentContext context = new ComponentContext();

	/**
	 * Component Manifest
	 */
	protected ComponentManifest manifest = new ComponentManifest();

	/**
	 * Componenent Logger
	 */
	protected Logger logger = null;

	private ClassLoader classLoader;

	/**
	 * Component Configuration
	 */
	protected NodeList config;


	/**
	 * for Loading Component. It must be contained in 'contextInitialized' Method.
	 * @param servletContext
	 * @param tmpComponent
	 * @throws ComponentException
	 */
	protected void loadComponent(ServletContext servletContext, ComponentAbstract tmpComponent) throws ComponentException{
		this.loadComponent(servletContext, tmpComponent, tmpComponent.getClass().getCanonicalName());
	}
	
	/**
	 * for Loading Component. It must be contained in 'contextInitialized' Method.
	 * @param servletContext
	 * @param tmpComponent
	 * @param canonicalNameForLogger
	 * @throws ComponentException
	 */
	protected void loadComponent(ServletContext servletContext, ComponentAbstract tmpComponent, String canonicalNameForLogger) throws ComponentException{
		try{
			ManagerCore.init();
			try {
				loggerManager = Logger.getInstance(ComponentAbstract.class);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			loggerManager.info("");
			loggerManager.info("Loading Component...");

			context.pathFolderContext = servletContext.getRealPath("") + "/";
			context.defaultUri = servletContext.getContextPath() + "/";

			File fileManifest = context.getResourceContext("WEB-INF/WCManifest.xml");
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser parser;
			try {
				parser = spf.newSAXParser();
				parser.parse(fileManifest, handlerXmlManifest);
			} catch (ParserConfigurationException e) {
				ComponentException tmpE = new ComponentException(e);
				loggerManager.e(tmpE);
				throw tmpE;
			} catch (SAXException e) {
				ComponentException tmpE = new ParsingManifestException(e);
				throw tmpE;
			} catch (FileNotFoundException e){
				ComponentManifestNotFoundException tmpE = new ComponentManifestNotFoundException();
				throw tmpE;
			} catch (IOException e) {
				ComponentException tmpE = new ComponentException(e);
				loggerManager.e(tmpE);
				throw tmpE;
			}



			config = ConfigurationManager.getConfiguration(manifest.getPackageName());
			context.config = config;
			classLoader = tmpComponent.getClass().getClassLoader();



			// ======== Check Component Registered
			if(ManagerCore.isRegisteredComponent(manifest.getPackageName())){
				throw new ComponentAlreadyRegisteredException(manifest.getPackageName());
			}

			if(Constant.VERSION_CODE < manifest.useManagerVersion){
				throw new InvalidManagerVersionException(manifest.packageName, Constant.VERSION_CODE, manifest.useManagerVersion);
			}



			// ======== Init Component Start!

			try {
				logger = Logger.newInstance(canonicalNameForLogger, manifest.packageName, manifest.componentName);
			} catch (Exception e1) {
				throw new ComponentException(e1);
			}
			logger.info("Component Start...");

			logger.i("Default URI: " + context.getDefaultUri());


			context.pathFolderComponentData = ManagerCore.getPathFolderData() + manifest.getPackageName() + "/";

			logger.info("ComponentDataPath: " + context.pathFolderComponentData);



			File tmpFolderData = context.getResourceData("");
			if(!tmpFolderData.exists()){
				logger.error("Data Folder of Component is not exist!, Creating...");
				tmpFolderData.mkdir();
			}



			// ========== Get Instance of ICM if it exist
			if (manifest.listIcmCanonicalName.size() != 0){
				logger.info("This Component has ICMs.");
				logger.info("\tInitializing ICMs...");

				for(String icmCanonicalName: manifest.listIcmCanonicalName){
					try {
						logger.info("\t\t" + icmCanonicalName);
						Class<?> tmpIcm = classLoader.loadClass(icmCanonicalName);
						context.mapIcm.put(icmCanonicalName, (IcmImpl) tmpIcm.newInstance());
					} catch (ClassNotFoundException e) {
						throw new IcmClassNotFoundException(icmCanonicalName, e);
					} catch (InstantiationException e) {
						throw new IcmException(icmCanonicalName, e);
					} catch (IllegalAccessException e) {
						throw new IcmException(icmCanonicalName, e);
					}
				}
			}



			// ========== Get Instance of EventListener if it exist
			if (manifest.listEventListenerCanonicalName.size() != 0){
				logger.info("This Component has EventListeners.");
				logger.info("\tInitializing EventListeners...");

				for(String eventListenerCanonicalName: manifest.listEventListenerCanonicalName){
					try {
						logger.info("\t\t" + eventListenerCanonicalName);
						Class<?> tmpEvent = classLoader.loadClass(eventListenerCanonicalName);
						EventListenerImpl tmpEventListener = (EventListenerImpl) tmpEvent.newInstance();

						Class<?>[] interfaces = tmpEvent.getInterfaces();

						for(Class<?> testInterface: interfaces){
							
							if(testInterface == EventListenerComponentImpl.class){
								context.listEventListenerComponent.add((EventListenerComponentImpl) tmpEventListener);
							}
							if(testInterface == EventListenerConfigurationImpl.class){
								context.listEventListenerConfiguration.add((EventListenerConfigurationImpl) tmpEventListener);
							}
							if(testInterface == EventListenerIcmImpl.class){
								context.listEventListenerIcm.add((EventListenerIcmImpl) tmpEventListener);
							}
						}
					} catch (ClassNotFoundException e) {
						throw new EventClassNotFoundException(eventListenerCanonicalName, e);
					} catch (InstantiationException e) {
						throw new EventException(eventListenerCanonicalName, e);
					} catch (IllegalAccessException e) {
						throw new EventException(eventListenerCanonicalName, e);
					}
				}
			}



			initComponent();


			logger.i("Registering to Manager...");
			try {
				ManagerCore.registerComponent(this);
			} catch (ComponentAlreadyRegisteredException e) {
				throw e;
			} catch (IcmAlreadyRegisteredException e) {
				throw e;
			}
			logger.i("Registering Complete.");
			logger.i("Component Initializing Complete.");
			logger.i("");
		} catch (ComponentException e){
			if(logger == null){
				loggerManager.e(e);
			} else {
				logger.e(e);
			}

			throw e;
		}
	}

	/**
	 * Unload Component. It must be contained in 'contextDestroyed' Method.
	 * @throws ComponentException 
	 */
	protected void unloadComponent() throws ComponentException{
		try{
			destroyComponent();

			try {
				ManagerCore.unregisterComponent(this);
			} catch (ComponentNotRegisteredException e) {
				throw e;
			} catch (IcmNotRegisteredException e) {
				throw e;
			}
		} catch (ComponentException e){
			logger.e(e);
		} finally {
			logger.info("Component Destroyed!");
			logger.destroy();
			ManagerCore.destroy();
		}
	}



	private DefaultHandler handlerXmlManifest = new DefaultHandler(){
		boolean isManifest = false;
		List<String> stack = new ArrayList<String>();
		int stackSize = 0;
		String lastElement = "";

		/* Unused
		@Override
		public void startDocument() throws SAXException {}
		 */

		@Override
		public void endDocument() throws SAXException {
			if(!isManifest){
				throw new SAXException("It Doesn't Contain Manifest!");
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (!isManifest){
				if (stackSize == 0 && qName.equals("WCManifest")){
					isManifest = true;

					manifest.packageName = attributes.getValue("Package");
					loggerManager.info("\tPackageName: " + manifest.getPackageName());

					if(manifest.getPackageName() == null){
						throw new SAXException("Manifest Doesn't Contain Package Name!");
					}
				}
			}

			if (isManifest){
				stack.add(qName);
				stackSize++;
				lastElement = qName;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (isManifest){
				stack.remove(stackSize - 1);
				stackSize--;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (isManifest && stackSize >= 2){
				if (lastElement.equals("ComponentName")){
					manifest.componentName = new String(ch, start, length);
					loggerManager.info("\tComponentName: " + manifest.getComponentName());
				} else if (lastElement.equals("UseManagerVersion")){
					manifest.useManagerVersion = Long.parseLong(new String(ch, start, length));

					loggerManager.info("\tUseManagerVersion: " + manifest.getUseManagerVersion());
				} else if (lastElement.equals("IcmClass")){
					if(stack.get(stackSize - 2) == "IcmClasses"){
						String tmpIcmClassName = new String(ch, start, length); 

						if (tmpIcmClassName.substring(0, 1).equals(".")){
							tmpIcmClassName = manifest.getPackageName() + tmpIcmClassName;
						}

						manifest.listIcmCanonicalName.add(tmpIcmClassName);
						loggerManager.info("\tICMClassName: " + tmpIcmClassName);
					}
				} else if (lastElement.equals("EventListenerClass")){
					if(stack.get(stackSize - 2) == "EventListenerClasses"){
						String tmpEventListenerClassName = new String(ch, start, length); 

						if (tmpEventListenerClassName.substring(0, 1).equals(".")){
							tmpEventListenerClassName = manifest.getPackageName() + tmpEventListenerClassName;
						}

						manifest.listEventListenerCanonicalName.add(tmpEventListenerClassName);
						loggerManager.info("\tEventListenerClassName: " + tmpEventListenerClassName);
					}
				} else if (lastElement.equals("VersionCode")){
					manifest.versionCode = Long.parseLong(new String(ch, start, length));
					loggerManager.info("\tVersionCode: " + manifest.getVersionCode());
				} else if (lastElement.equals("VersionName")){
					manifest.versionName = new String(ch, start, length);
					loggerManager.info("\tVersionName: " + manifest.getVersionName());
				}
			}
		}
	};

	/**
	 * Get Component Context
	 * @return Component Context
	 */
	public ComponentContext getContext() {
		return context;
	}

	/**
	 * Get Component Manifest
	 * @return Component Manifest
	 */
	public ComponentManifest getManifest() {
		return manifest;
	}

	/**
	 * Component Manifest
	 */
	public static class ComponentManifest {
		protected String packageName;
		protected String componentName;
		protected long useManagerVersion;
		protected List<String> listIcmCanonicalName = new ArrayList<String>();
		protected List<String> listEventListenerCanonicalName = new ArrayList<String>();
		protected long versionCode;
		protected String versionName;

		/**
		 * @since 1
		 */
		public String getPackageName(){
			return packageName;
		}
		/**
		 * @since 1
		 */
		public String getComponentName(){
			return componentName;
		}


		/**
		 * @since 1
		 */
		public long getUseManagerVersion(){
			return useManagerVersion;
		}

		/**
		 * @since 1
		 */
		public long getVersionCode(){
			return versionCode;
		}

		/**
		 * @since 1
		 */
		public String getVersionName(){
			return versionName;
		}

		/**
		 * @since 1
		 */
		public List<String> getListIcmClassName() {
			return listIcmCanonicalName;
		}

		/**
		 * @since 1
		 */
		public List<String> getListEventListenerClassName() {
			return listEventListenerCanonicalName;
		}
	}



	public static class ComponentContext {
		protected String pathFolderContext;
		protected String pathFolderComponentData;
		protected String defaultUri;
		protected Map<String, IcmImpl> mapIcm = new HashMap<String, IcmImpl>(); 
		protected List<EventListenerComponentImpl> listEventListenerComponent = new ArrayList<EventListenerComponentImpl>();
		protected List<EventListenerConfigurationImpl> listEventListenerConfiguration = new ArrayList<EventListenerConfigurationImpl>();
		protected List<EventListenerIcmImpl> listEventListenerIcm = new ArrayList<EventListenerIcmImpl>();

		protected NodeList config;

		/**
		 * @since 1
		 */
		protected String getPathComponentContextLocation(){
			return pathFolderContext;
		}

		/**
		 * @throws Exception 
		 * @since 1
		 */
		public File getResourceCommon(String path) throws Exception{
			return ManagerCore.getResourceCommon(path);
		}

		/**
		 * @since 1
		 */
		public File getResourceData(String path){
			return new File(pathFolderComponentData + path);
		}

		/**
		 * @since 1
		 */
		public File getResourceContext(String path){
			return new File(pathFolderContext + path);
		}

		/**
		 * @since 1
		 */
		public String getDefaultUri(){
			return defaultUri;
		}

		/**
		 * @since 1
		 */
		public NodeList getConfiguration() {
			return config;
		}

		/**
		 * @since 1
		 */
		public Map<String, IcmImpl> getMapIcm(){
			return mapIcm;
		}

		/**
		 * @since 1
		 */
		public List<EventListenerComponentImpl> getListEventListenerComponent(){
			return listEventListenerComponent;
		}
		/**
		 * @since 1
		 */
		public List<EventListenerConfigurationImpl> getListEventListenerConfiguration(){
			return listEventListenerConfiguration;
		}
		/**
		 * @since 1
		 */
		public List<EventListenerIcmImpl> getListEventListenerIcm(){
			return listEventListenerIcm;
		}
	}





	/**
	 * Method for Component Initializing
	 */
	protected abstract void initComponent() throws ComponentException;

	/**
	 * Method for Component Destroying
	 */
	protected abstract void destroyComponent() throws ComponentException;
}
