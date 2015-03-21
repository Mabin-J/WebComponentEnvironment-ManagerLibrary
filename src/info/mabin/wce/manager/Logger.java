package info.mabin.wce.manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Logger {
	private static StringBuilder logAll = new StringBuilder();
	private static SimpleDateFormat sdfForLog = new SimpleDateFormat("yyyyMMddHHmmss");
	
	private static File fileLogAll;
	
	private static Timer timerAll = new Timer(true);
	private static TimerTask taskAll = new TimerTask() {
		@Override
		public void run() {
			try {
				FileWriter fw = new FileWriter(fileLogAll, true);
				fw.append(logAll.toString());
				logAll.delete(0, logAll.length());
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	/**
	 * Key: ClassCanonicalName
	 */
	private static Map<String, Logger> mapLogger = new HashMap<String, Logger>();

	/**
	 * Key: packageName
	 */
	private static Map<String, ComponentLogger> mapComponentLogger = new HashMap<String, Logger.ComponentLogger>();
	
	private static String pathLogs = null;
	
	private ComponentLogger componentLogger;
	
	private String classCanonicalName = null;
	private String packageName = null;
	private String prefixConsole = null;
	private String prefixComponent = null;

	static void setPath(String pathLogs){
		Logger.pathLogs = pathLogs;
		
		fileLogAll = new File(pathLogs + "All.log");
		taskAll.run();
		timerAll.schedule(taskAll, Constant.LOGGER_WRITE_PERIOD, Constant.LOGGER_WRITE_PERIOD);
		
		for(String packageName: mapComponentLogger.keySet()){
			ComponentLogger tmpComponentLogger = mapComponentLogger.get(packageName);
			
			tmpComponentLogger.fileLog = new File(pathLogs + packageName + ".log");
			tmpComponentLogger.task.run();
			tmpComponentLogger.timer.schedule(tmpComponentLogger.task, Constant.LOGGER_WRITE_PERIOD, Constant.LOGGER_WRITE_PERIOD);
		}
	}
	
	/**
	 * Get Logger for Class.
	 * @param targetClass
	 * @return Logger
	 * @throws Exception
	 */
	public static Logger getInstance(Class<?> targetClass) throws Exception{
		String tmpClassCanonicalName = targetClass.getCanonicalName();
		if(mapLogger.containsKey(tmpClassCanonicalName)){
			return mapLogger.get(tmpClassCanonicalName);
		} else {
			Logger tmpLogger = null;
			try {
				tmpLogger = new Logger(targetClass.getCanonicalName());
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			mapLogger.put(tmpClassCanonicalName, tmpLogger);
			return tmpLogger;
		}
	}

	public static Logger newInstance(String canonicalName, String packageName, String componentName) throws Exception{
		ComponentLogger tmpComponentLogger = new ComponentLogger();
		
		tmpComponentLogger.componentName = componentName;
		tmpComponentLogger.logContainer = new StringBuilder();
		
		mapComponentLogger.put(packageName, tmpComponentLogger);
		
		if(pathLogs != null){
			tmpComponentLogger.fileLog = new File(pathLogs + packageName + ".log");
			tmpComponentLogger.timer.schedule(tmpComponentLogger.task, Constant.LOGGER_WRITE_PERIOD, Constant.LOGGER_WRITE_PERIOD);
		}
		
		try {
			return new Logger(canonicalName);
		} catch (Exception e) {
			throw e;
		}
	}
	
	static Logger newInstance(Class<?> targetClass, String packageName, String componentName) throws Exception{
		ComponentLogger tmpComponentLogger = new ComponentLogger();
		
		tmpComponentLogger.componentName = componentName;
		tmpComponentLogger.logContainer = new StringBuilder();
		
		mapComponentLogger.put(packageName, tmpComponentLogger);
		
		if(pathLogs != null){
			tmpComponentLogger.fileLog = new File(pathLogs + packageName + ".log");
			tmpComponentLogger.timer.schedule(tmpComponentLogger.task, Constant.LOGGER_WRITE_PERIOD, Constant.LOGGER_WRITE_PERIOD);
		}
		
		try {
			return new Logger(targetClass.getCanonicalName());
		} catch (Exception e) {
			throw e;
		}
	}
	
	private Logger(String classCanonicalName) throws Exception{
		this.classCanonicalName = classCanonicalName;
		
		for(String packageName: mapComponentLogger.keySet()){
			if(this.classCanonicalName.startsWith(packageName)){
				this.packageName = packageName;
				this.componentLogger = mapComponentLogger.get(packageName);
				
				break;
			}
		}
		
		if(this.componentLogger == null){
			// TODO Exception for Not Registered Package;
			throw new Exception("CanonicalName: " + classCanonicalName);
		} else {
			this.prefixConsole = this.classCanonicalName.replace(this.packageName, this.componentLogger.componentName);
			this.prefixComponent = this.classCanonicalName.replace(this.packageName + ".", "");
			
			mapLogger.put(this.classCanonicalName, this);

			componentLogger.listCanonicalNames.add(this.classCanonicalName);
		}
	}
	
	private Logger(String packageName, String canonicalName){
		
	}
/**
 * Debug Log
 * @param logContext
 */
	public void debug(String logContext){
		if(Constant.LOGGER_PRINT_DEBUG)
			logging("DEBUG", logContext);
	}

/**
 * Informatin Log
 * @param logContext
 */
	public void info(String logContext){
		logging("INFO ", logContext);
	}
	
/**
 * Error Log
 * @param logContext
 */
	public void error(String logContext){
		logging("ERROR", logContext);
	}
	
/**
 * Error Log with Exception
 * @param exception
 */
	public void error(Throwable exception){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		logging("ERROR", "StackTrace: \n" + sw.toString());
	}
	
/**
 * Real Logging Method
 * @param type Type in Log. (INFO, DEBUG, ERROR, etc)
 * @param logContext
 */
	private void logging(String type, String logContext){
		String dateString = sdfForLog.format(Calendar.getInstance().getTime());

		String tmpLogConsole = dateString + " " + type + " " + prefixConsole + ": " + logContext + "\n";
		String tmpLogComponent = dateString + " " + type + " " + prefixComponent + ": " + logContext + "\n";
		String tmpLogAll = dateString + " " + type + " " + classCanonicalName + ": " + logContext + "\n";
		componentLogger.logContainer.append(tmpLogComponent);
		logAll.append(tmpLogAll);
		System.out.print(tmpLogConsole);
	}
	
	public void d(String logContext){
		debug(logContext);
	}
	
	public void i(String logContext){
		info(logContext);
	}

	public void e(String logContext){
		error(logContext);
	}
	
	public void e(Throwable exception){
		error(exception);
	}

	void destroy() {
		componentLogger.timer.cancel();
		componentLogger.timer.purge();
		componentLogger.task.run();
		
		for(String canonicalName: componentLogger.listCanonicalNames){
			mapLogger.remove(canonicalName);
		}
		
		mapComponentLogger.remove(packageName);
		System.out.println("LoggerDestroy Finish");
	}
	
	static void destroyAll(){
		for(String packageName: mapComponentLogger.keySet()){
			System.out.println("RemainedLogger: " + packageName);
			ComponentLogger tmpLogger = mapComponentLogger.get(packageName);
			tmpLogger.timer.cancel();
			tmpLogger.timer.purge();
			tmpLogger.task.run();
		}
		
		timerAll.cancel();
		timerAll.purge();
		taskAll.run();
		
		System.out.println("LoggerAllDestroy Finish");
	}

	
	private static class ComponentLogger {
		String componentName;
		List<String> listCanonicalNames = new ArrayList<String>();
		StringBuilder logContainer = new StringBuilder();
		File fileLog;
		Timer timer = new Timer(true);
		
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				try {
					FileWriter fw = new FileWriter(fileLog, true);
					fw.append(logContainer.toString());
					logContainer.delete(0, logContainer.length());
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}
}