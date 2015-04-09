package org.baderlab.expressioncorrelation.internal.util;

import java.io.InputStream;
import java.util.Properties;

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Christian Lopes
 * * Authors: Gary Bader, Elena Potylitsine, Chris Sander, Weston Whitaker
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no type shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

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
