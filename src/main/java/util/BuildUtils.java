package util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public final class BuildUtils {

	/**
	 * Function escapes the special character in the String replacement string.  
	 * @param s
	 * @return
	 */	
    public static String quoteReplacementForStringReplace(String s) {
        if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))
            return s;
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                sb.append('\\'); sb.append('\\');
            } else if (c == '$') {
                sb.append('\\'); sb.append('$');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static Pattern xmlNode = Pattern.compile("<([^>]+)>([^<]+)<[^>]+>");

	public static Map<String, String> xmlToMap(String xml) {
		Map<String, String> result = new HashMap<String, String>();
		if (xml == null)
			return result;
		Matcher m = xmlNode.matcher(xml);
		while (m.find()) {
			result.put(m.group(1), m.group(2));
		}

		return result;
	}

	public static String mapToXml(Map map) {
		String result = "";

		if (map != null && map.size() > 0) {
			StringBuffer buf = new StringBuffer();

			for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				buf.append("<" + entry.getKey() + ">");
				buf.append(entry.getValue());
				buf.append("</" + entry.getKey() + ">");
			}
			result = buf.toString();
		}

		return result;
	}
	

	/**
	 * Get the value from the input map using input key.
	 * If the value is null, a new instance of the valueClass
	 * will be generated and put into the map.
	 * @param <K> map key type
	 * @param <V> map value type
	 * @param valueClass class of type V
	 */
	public static <K, V> V getOrDefault(Map<K, V> map,  K key, Class valueClass) {
		V value = map.get(key);
		if(value == null) {
			try {
				value = (V)valueClass.newInstance();
			} catch(Exception e)  {
				throw new RuntimeException(e);
			}
			map.put(key, value);
		}
		return value;
	}

	/**
	 * format the String value to given type. The type can be one of the follows:
	 * String, Boolean/boolean, Long/long, Integer/int, Double/double, DataSource,
	 * String[], boolean[]/Boolean[], Long[]/long[], Integer[]/intp[], Double[]/double[],
	 * and any subclass of Enum/Enum[].
	 * 
	 * A String represeting an Array needs to use coma "," as the delimiter, e.g., "100, 200,300".
	 * 
	 * If the type is null or none of the above, a null value will be returned.
	 * 
	 * Only the Object of the primitive type can be processed. Enum[] types will get list 
	 * all the way (despite the setting of returnListForArray).
	 * 
	 * Each element in the array needs to be separated by the given regex, and all
	 * 
	 * @param value
	 * @param type
	 * @param returnListForArray  true return a list for given array type, else return the array type
	 * @param regex splitting regex
	 * @return
	 */
	public static Object formatString(String value, Class type, boolean returnListForArray, String regex) {
		if (value == null) {
			return null;
		}

		String splitRegex = regex == null ? "," : regex;
		
		if (type == null) {
			return null;
		} else if (type.equals(String.class)) {
			return value;
		} else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
			return TaskUtils.parseBooleanValue(value, false)
			        ? Boolean.TRUE
			        : Boolean.FALSE;
		} else if (type.equals(long.class) || type.equals(Long.class)) {
			return new Long(TaskUtils.parseLongValue(value, 0));
		} else if (type.equals(int.class) || type.equals(Integer.class)) {
			return new Integer(TaskUtils.parseIntValue(value, 0));
		} else if (type.equals(double.class) || type.equals(Double.class)) {
			return new Double(TaskUtils.parseDoubleValue(value, 0));
		} else if (type.equals(String[].class)) {
			String[] result = value.split(splitRegex);
			if (returnListForArray) {
				return Arrays.asList(result);
			}
			return result;
		
		
		
		} else if (Enum.class.isAssignableFrom(type)) {
			try {
				return Enum.valueOf(type, value);
			} catch (IllegalArgumentException e) {
				return null;
			}
		} else if (Enum[].class.isAssignableFrom(type)) {
			String[] values = value.replaceAll("\\s", "").split(splitRegex);
			List<Enum> result = new ArrayList<Enum>();
			for (String strValue : values) {
				try {
					result.add(Enum.valueOf(type.getComponentType(), strValue));
				} catch (IllegalArgumentException e) {
					// ignore unknown
				}
			}

			if (returnListForArray) {
				return result;
			}

			Object arr = Array.newInstance(type.getComponentType(), result.size());
			System.arraycopy(result.toArray(), 0, arr, 0, result.size());

			return arr;
		} else if (List.class.isAssignableFrom(type) || type.equals(String[].class)) {
			String val = value.trim();
			boolean haveBracket = false;
			if (val.startsWith("[")) {
				val = val.substring(1);
				haveBracket = true;
			}
			if (haveBracket && val.endsWith("]")) {
				val = val.substring(0, val.length() - 1);
			}
			String[] values = val.split(splitRegex);
			List<String> result = new ArrayList();
			for (String svalue : values) {
				result.add(svalue.trim());
			}

			if (List.class.isAssignableFrom(type)) {
				return result;
			}

			return result.toArray(new String[result.size()]);
		} else if (Set.class.isAssignableFrom(type)) {
			String val = value.trim();
			boolean haveBracket = false;
			if (val.startsWith("[")) {
				val = val.substring(1);
				haveBracket = true;
			}
			if (haveBracket && val.endsWith("]")) {
				val = val.substring(0, val.length() - 1);
			}
			String[] values = val.split(splitRegex);
			Set result = new HashSet();
			for (String svalue : values) {
				result.add(svalue.trim());
			}
			return result;
		} else if (Map.class.isAssignableFrom(type)) {
			String val = value.trim();
			boolean haveBracket = false;
			if (val.startsWith("[") || val.startsWith("{")) {
				val = val.substring(1);
				haveBracket = true;
			}
			if (haveBracket && (val.endsWith("]") || val.endsWith("}"))) {
				val = val.substring(0, val.length() - 1);
			}
			// decide what delimitor
			String delimitor = "\\|";
			if (val.indexOf('|') == -1 &&
			    (StringUtils.countMatches(val, "->") > 1 || 
			    		StringUtils.countMatches(val,"=") > 1)) {
				// string contains no '|', but has > 1 mapping fields
				delimitor = "," ;// try comma
			}
			String[] values = val.split(delimitor);
			Map result = new HashMap();
			for (String svalue : values) {
				svalue = svalue.trim();
				if (StringUtils.isBlank(svalue)) {
					continue;
				}

				String[] kv = null;
				if (svalue.indexOf("->") > 0) {
					kv = svalue.split("->");
				} else {
					kv = svalue.split("=");
				}

				if (kv.length > 0 && StringUtils.isNotBlank(kv[0])) {
					if (kv.length == 1 && !svalue.startsWith("=")) {
						result.put(kv[0].trim(), null);
					} else if (kv.length > 1) {
						result.put(kv[0].trim(), kv[1].trim());
					}
				}
			}
			return result;
		} else {
			// unknown type
			return null;
		}
	}

	/**
	 * format the String value to given type. The type can be one of the follows:
	 * String, Boolean/boolean, Long/long, Integer/int, Double/double, DataSource,
	 * String[], boolean[]/Boolean[], Long[]/long[], Integer[]/intp[], Double[]/double[],
	 * and any subclass of Enum/Enum[].
	 * 
	 * A String represeting an Array needs to use coma "," as the delimiter, e.g., "100, 200,300".
	 * 
	 * If the type is null or none of the above, a null value will be returned. 
	 * 
	 * Only the Object of the primitive type will be returned; List will be returned if 
	 * array is specified as the type and each element of the list is the Object 
	 * representataion of the specified primitive type.
	 * 
	 * Each element in the array needs to be separated by coma ",", and all
	 * 
	 * @param value
	 * @param type
	 * @return
	 */
	public static Object formatString(String value, Class type) {
		return formatString(value, type, true, null);
	}

	/**
	 * format the String value to given type. The type can be one of the follows:
	 * String, Boolean/boolean, Long/long, Integer/int, Double/double, DataSource,
	 * String[], boolean[]/Boolean[], Long[]/long[], Integer[]/intp[], Double[]/double[],
	 * and any subclass of Enum/Enum[].
	 * 
	 * A String represeting an Array needs to use coma "," as the delimiter, e.g., "100, 200,300".
	 * 
	 * If the type is null or none of the above, a null value will be returned. 
	 * 
	 * Only the Object of the primitive type will be returned; List will be returned if 
	 * array is specified as the type and each element of the list is the Object 
	 * representataion of the specified primitive type.
	 * 
	 * Each element in the array needs to be separated by the given regex, and all
	 * 
	 * @param value
	 * @param type
	 * @param regex	splitting regex
	 * @return
	 */
	public static Object formatString(String value, Class type, String regex) {
		return formatString(value, type, true, regex);
	}

	/**
	 * format the String value to given type. The type can be one of the follows:
	 * String, Boolean/boolean, Long/long, Integer/int, Double/double, DataSource,
	 * String[], boolean[]/Boolean[], Long[]/long[], Integer[]/intp[], Double[]/double[],
	 * and any subclass of Enum/Enum[].
	 * 
	 * A String represeting an Array needs to use coma "," as the delimiter, e.g., "100, 200,300".
	 * 
	 * If the type is null or none of the above, a null value will be returned. 
	 * 
	 * Only the Object of the primitive type will be returned; List will be returned if 
	 * array is specified as the type and each element of the list is the Object 
	 * representataion of the specified primitive type.
	 * 
	 * Each element in the array needs to be separated by coma ",", and all
	 * 
	 * @param value
	 * @param type
	 * @param returnListForArray
	 * @return
	 */
	public static Object formatString(String value, Class type, boolean returnListForArray) {
		return formatString(value, type, returnListForArray, null);
	}


}
