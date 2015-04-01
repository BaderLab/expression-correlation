package org.baderlab.expressioncorrelation.internal.util;

import java.io.InputStream;
import java.util.Properties;

public final class AppUtil {

	private static Properties props;
	
	static {
		props = loadProperties("/app.properties");
	}
	
	public static String getProperty(final String key) {
		return props.getProperty(key);
	}
	
	public static String getVersion() {
		return props.getProperty("project.version");
	}
	
	public static String getBuildDate() {
		return props.getProperty("project.build.date");
	}
	
	private static Properties loadProperties(String name) {
		Properties props = new Properties();

		try {
			InputStream in = AppUtil.class.getResourceAsStream(name);

			if (in != null) {
				props.load(in);
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return props;
	}
	
	private AppUtil() {
		// Prevents instantiation
	}
}
