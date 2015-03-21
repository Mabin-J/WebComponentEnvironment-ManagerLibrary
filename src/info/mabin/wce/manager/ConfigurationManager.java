package info.mabin.wce.manager;

import info.mabin.wce.manager.exception.ParsingConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigurationManager {
	private static Logger logger;

	private static Map<String, Configuration> mapConfiguration = new HashMap<String, ConfigurationManager.Configuration>();

	private static Map<String, FileInfo> mapFileInfo = new HashMap<String, ConfigurationManager.FileInfo>();

	private static File folderConfigurations;
	private static String configurationsPath;

	private static Timer timer;

	private static DocumentBuilderFactory dbf;
	private static DocumentBuilder db;

	static{
		try {
			logger = Logger.getInstance(ConfigurationManager.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dbf = DocumentBuilderFactory.newInstance();
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
	}

	static void loadConfigures(String configurationsPath) {
		ConfigurationManager.configurationsPath = configurationsPath;
		logger.info("ConfigurationManager: Loading Configurations...");
		folderConfigurations = new File(configurationsPath);

		scanTask.run();

		logger.info("ConfigurationManager: Loading Configurations Finished!");


		timer = new Timer(true);
		timer.schedule(scanTask, Constant.CONFIGURATION_CHECK_CHANGE_PERIOD, Constant.CONFIGURATION_CHECK_CHANGE_PERIOD);
	}

	private static TimerTask scanTask = new TimerTask() {

		@Override
		public void run() {
			String[] arrayXmlFilename = folderConfigurations.list(FilenameFilterFactory.getFilenameFilterWithExtension("xml"));
			Map<String, FileInfo> mapNewFileInfo = new HashMap<String, FileInfo>();

			for(String xmlFilename: arrayXmlFilename){
				File targetFileXml = new File(configurationsPath + xmlFilename);

				FileInfo fileInfoNew = new FileInfo();
				fileInfoNew.lastModified = targetFileXml.lastModified();
				mapNewFileInfo.put(xmlFilename, fileInfoNew);

				if(mapFileInfo.containsKey(xmlFilename)){
					FileInfo tmpFileInfo = mapFileInfo.get(xmlFilename);
					if(tmpFileInfo.lastModified >= targetFileXml.lastModified()){
						continue;
					}
				}

				logger.info("Parsing '" + xmlFilename + "'...");


				try {
					Document doc = db.parse(targetFileXml);

					NodeList tmpNodeList = doc.getChildNodes();
					for(int i = 0; i < tmpNodeList.getLength(); i++){
						Node tmpNode = tmpNodeList.item(i);

						if(tmpNode.getNodeName().equals("Configuration")){
							NamedNodeMap attributes = tmpNode.getAttributes();
							String tmpPackageName = attributes.getNamedItem("Package").getTextContent();

							if(tmpPackageName == null){
								logger.e("PackageName Not Found in Configuration");
							} else {
								logger.i("PackageName: " + tmpPackageName);
								Configuration tmpConfiguration;
								if(mapConfiguration.containsKey(tmpPackageName)){
									tmpConfiguration = mapConfiguration.get(tmpPackageName);

									tmpConfiguration.content = tmpNode.getChildNodes();

									ManagerCore.eventChangedConfiguration(tmpPackageName, tmpConfiguration.content);
									logger.i(tmpPackageName);
								} else {
									tmpConfiguration = new Configuration();
									tmpConfiguration.content = tmpNode.getChildNodes();

									mapConfiguration.put(tmpPackageName, tmpConfiguration);
								}
							}
						}
					}
				} catch (SAXException e) {
					ParsingConfigurationException tmpException = new ParsingConfigurationException(e);
					logger.e(tmpException);
				} catch (IOException e) {
					ParsingConfigurationException tmpException = new ParsingConfigurationException(e);
					logger.e(tmpException);
				} catch (Exception e) {
					ParsingConfigurationException tmpException = new ParsingConfigurationException(e);
					logger.e(tmpException);
				}
			}

			mapFileInfo = mapNewFileInfo;
		}
	};

	/**
	 * Get Configuration for Component
	 * @param componentPackageName Package Name of Component
	 * @return NodeList of DOM Structure
	 */
	public static NodeList getConfiguration(String componentPackageName){
		logger.d(componentPackageName);
		Configuration tmpConf = mapConfiguration.get(componentPackageName);
		if(tmpConf == null)
			return null;

		return mapConfiguration.get(componentPackageName).content;
	}

	static void destroy(){
		timer.cancel();
		timer.purge();
		System.out.println("Cancel Timer of ConfigurationManager Finish");
	}

	private static class Configuration{
		NodeList content;
	}

	private static class FileInfo{
		Long lastModified;
	}
}