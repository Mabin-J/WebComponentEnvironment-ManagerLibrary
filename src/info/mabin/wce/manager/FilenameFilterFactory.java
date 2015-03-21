package info.mabin.wce.manager;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameFilterFactory{

	/**
	 * Filename Filter with Extension
	 * @param extension File Extension name
	 * @return FileFilter
	 */
	public static FilenameFilter getFilenameFilterWithExtension(final String extension){
		FilenameFilter filenameFilter = new FilenameFilter(){
			private int tmpFilenameLength;

			@Override
			public boolean accept(File dir, String name) {
				tmpFilenameLength = name.length();
				try{
					if(name.substring(tmpFilenameLength - (extension.length() + 1), tmpFilenameLength).toLowerCase().equals("." + extension)){
						return true;
					} else {
						return false;
					}
				} catch (Exception e){
					return false;
				}
			}
		};

		return filenameFilter;
	}
}