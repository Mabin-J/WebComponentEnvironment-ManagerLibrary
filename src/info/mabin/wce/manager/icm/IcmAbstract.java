package info.mabin.wce.manager.icm;

import info.mabin.wce.manager.ManagerCore;
import info.mabin.wce.manager.exception.IcmNotRegisteredException;
import info.mabin.wce.manager.icm.exception.IcmException;
import info.mabin.wce.manager.icm.exception.IcmGetMethodException;
import info.mabin.wce.manager.icm.exception.IcmInvokeMethodException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class IcmAbstract implements IcmImpl{
	protected IcmImpl instance;
	private Class<?> classIcm;

	final static Map<Class<?>, Class<?>> mapPrimitiveClass = new HashMap<Class<?>, Class<?>>();
	static{
		mapPrimitiveClass.put(java.lang.Boolean.class,	boolean.class);
		mapPrimitiveClass.put(java.lang.Byte.class,		byte.class);
		mapPrimitiveClass.put(java.lang.Short.class,	short.class);
		mapPrimitiveClass.put(java.lang.Integer.class,	int.class);
		mapPrimitiveClass.put(java.lang.Long.class,		long.class);
		mapPrimitiveClass.put(java.lang.Character.class,char.class);
		mapPrimitiveClass.put(java.lang.Float.class,	float.class);
		mapPrimitiveClass.put(java.lang.Double.class,	double.class);
	}
	
	public IcmAbstract(String icmCanonicalName) throws IcmNotRegisteredException{
		try {
			this.instance = ManagerCore.getIcm(icmCanonicalName);
		} catch (IcmNotRegisteredException e) {
			throw e;
		}

		classIcm = instance.getClass();
	}

	protected Object invokeMethod(String methodName, Object... params) throws IcmException{
		Class<?>[] arrClass = new Class<?>[params.length];

		for(int i = 0; i < params.length; i++){
			Object param = params[i];
			Class<?> classParam = param.getClass();
			if(mapPrimitiveClass.containsKey(classParam)){
				arrClass[i] = mapPrimitiveClass.get(classParam);
			} else {
				arrClass[i] = classParam;
			}
		}

		try {
			return classIcm.getMethod(methodName, arrClass).invoke(instance, params);
		} catch (NoSuchMethodException e) {
			throw new IcmGetMethodException(e);
		} catch (SecurityException e) {
			throw new IcmException(e);
		} catch (IllegalAccessException e) {
			throw new IcmInvokeMethodException(e);
		} catch (IllegalArgumentException e) {
			throw new IcmInvokeMethodException(e);
		} catch (InvocationTargetException e) {
			throw new IcmInvokeMethodException(e);
		}
	}
	
	protected Object invokeMethod(Method method, Object... params) throws IcmException{
		try {
			return method.invoke(instance, params);
		} catch (SecurityException e) {
			throw new IcmInvokeMethodException(e);
		} catch (IllegalAccessException e) {
			throw new IcmInvokeMethodException(e);
		} catch (IllegalArgumentException e) {
			throw new IcmInvokeMethodException(e);
		} catch (InvocationTargetException e) {
			throw new IcmInvokeMethodException(e);
		}
	}
	
	protected Method getMethod(String methodName, Class<?>[] arrClass) throws IcmException{
		try {
			return classIcm.getMethod(methodName, arrClass);
		} catch (NoSuchMethodException e) {
			throw new IcmGetMethodException(e);
		} catch (SecurityException e) {
			throw new IcmGetMethodException(e);
		}
	}
	
	protected Method getMethod(String methodName) throws IcmException{
		try{
			return classIcm.getMethod(methodName);
		} catch (NoSuchMethodException e) {
			throw new IcmGetMethodException(e);
		} catch (SecurityException e) {
			throw new IcmGetMethodException(e);
		}
	}
	
	public void destroy(){
		instance = null;
	}
}
