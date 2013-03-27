package util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;



import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.StringUtils;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TaskUtils {

	private static final Logger log = Logger.getLogger(TaskUtils.class.getName());
	
	
	public static String getDayOfMonthSuffix(final int n) {
	    if (n >= 11 && n <= 13) {
	        return "th";
	    }
	    switch (n % 10) {
	        case 1:  return "st";
	        case 2:  return "nd";
	        case 3:  return "rd";
	        default: return "th";
	    }
	}
	
	
	public static boolean parseBooleanValue(String valueStr, boolean defaultValue) {
		if (!StringUtils.isBlank(valueStr)) {
			char c = valueStr.trim().charAt(0);
			// yes, true and 1 map to TRUE
			if (c == 'y' || c == 'Y' || c == 't' || c == 'T' || c == '1') {
					return true;
			}
			// no, false and 0 map to FALSE
			if (c == 'n' || c == 'N' || c == 'f' || c == 'F' || c == '0') {
					return false;
			}
		}
		return defaultValue;
}
	
	/**
     * Parse a long string and return the long value. If the string is null, or
     * if it cannot be parsed, then return the defaultValue.
     */
	public static long parseLongValue(String valueStr, long defaultValue) {
		long value = defaultValue;

		if (valueStr != null) {
			try {
				value = Long.parseLong(valueStr.trim());
			} catch (NumberFormatException ex) {
				// Do nothing.
			}
		}

		return value;
	}

	/**
     * Parse a int string and return the int value. If the string is null, or if
     * it cannot be parsed, then return the defaultValue.
     */
	public static int parseIntValue(String valueStr, int defaultValue) {
		int value = defaultValue;
		if (valueStr != null) {
			try {
				value = Integer.parseInt(valueStr.trim());
			} catch (NumberFormatException ex) {
				// Do nothing.
			}
		}
		return value;
	}

	/**
     * Parse a string double and return the double value. If the string is null,
     * or if it cannot be parsed, then return the defaultValue.
     */
	public static double parseDoubleValue(String valueStr, double defaultValue) {
		double value = defaultValue;

		if (valueStr != null) {
			try {
				value = Double.parseDouble(valueStr);
			} catch (NumberFormatException ex) {
				// Do nothing.
			}
		}

		return value;
	}

	/**
     * Parse a double parameter and return its value. If the parameter is not
     * specified, or if it is not a valid number, then return the defaultValue.
     */
	public static double getDoubleParameter(Map parameters,
	                                        String name,
	                                        double defaultValue) {
		String valueStr = (String) parameters.get(name);
		return (parseDoubleValue(valueStr, defaultValue));
	}
	
	/**
	 * @param bean
	 * @param paramaters
	 * @return
	 */
	public static Map<String, Object> bindBeanWithParameters(Object bean,
	                                                         Map paramaters) {
		return bindBeanWithParameters(bean, paramaters, true, null);
	}

	public static Map<String, Object> bindBeanWithParameters(Object bean,
	                                                         Map paramaters,
	                                                         boolean onlyForPublicSetter) {
		return bindBeanWithParameters(bean, paramaters, onlyForPublicSetter, null);
	}

	public static Map<String, Object> bindBeanWithParameters(Object bean,
	                                                         Map parameters,
	                                                         Set<String> parameterToSet) {
		return bindBeanWithParameters(bean, parameters, false, parameterToSet);
	}

	public static Map<String, Object> bindBeanWithParameters(Object bean,
	                                                         Map parameters,
	                                                         boolean onlyForPublicSetter,
	                                                         Set<String> parameterToSet) {
		Map<String, Object> bindedValues = new HashMap();
		PropertyUtilsBean bub = BeanUtilsBean.getInstance().getPropertyUtils();
		PropertyDescriptor[] descriptors = bub.getPropertyDescriptors(bean);

		for (PropertyDescriptor pt : descriptors) {
			// only bind public methods
			if (onlyForPublicSetter) {
				Method writeMethod = pt.getWriteMethod();
				if (writeMethod == null ||
				    !Modifier.isPublic(pt.getWriteMethod().getModifiers())) {
					continue;
				}
			}

			String name = pt.getName();
			
			Object valueObj = getParamValue(name, parameters, parameterToSet);

			// no setting found, try next property
			if (valueObj == null) {
				continue;
			}
			
			Class pType = pt.getPropertyType();
			
			Object value = null;
			if (pType.isAssignableFrom(valueObj.getClass())) {
				value = valueObj;
			} else {
				String valueStr = valueObj.toString().trim();
				if (StringUtils.isBlank(valueStr)){
					continue;
				}
				// try BuildUitls.formatString(...)
				try {
					value = BuildUtils.formatString(valueStr, pType, false);
				} catch (RuntimeException e) {
				}
				// if conversion fails, try ConvertUtils.convert(...)
				if (value == null) {
					try {
						value = ConvertUtils.convert(valueStr, pType);
					} catch (ConversionException e) {
					}
				}
				
				// check if conversion is successful
				if (value!=null){
					if (!pType.isPrimitive() && !pType.isAssignableFrom(value.getClass())) {
						value = null;
					}
				}
			}

			if (value != null) {
				try {
					BeanUtils.setProperty(bean, name, value);
					bindedValues.put(name, value);
				} catch (IllegalAccessException e) {
					log.info(Fmt.S(e));
				} catch (InvocationTargetException e) {
					log.info(Fmt.S(e));
				} catch (IllegalArgumentException e) {
					log.info(Fmt.S(e));
				} catch (SecurityException e) {
					log.info(Fmt.S(e));
				}
			}
		}
		
		return bindedValues;
	}
	
	public static String getParamString(String name, Map parameters) {
		Object val = getParamValue(name, parameters);
		if (val == null) {
			return null;
		} else {
			return val.toString();
		}
	}

	public static Object getParamValue(String name, Map parameters) {
		return getParamValue(name, parameters, null);
	}

	public static Object getParamValue(String name,
	                                   Map parameters,
	                                   Set<String> parameterToSet) {
		String[] pNames = new String[3];
		pNames[0] = name;
		pNames[1] = name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
		pNames[2] = name.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();

		// do not set if the property is not included in propertiesSettable
		if (parameterToSet != null) {
			boolean allowSetting = false;
			for (String pName : pNames) {
				allowSetting = allowSetting || parameterToSet.contains(pName);
			}
			if (!allowSetting) {
				return null;
			}
		}

		// try to find the setting in parameters
		Object valueObj = null;
		for (int i = 0; i < pNames.length &&
		    (valueObj = parameters.get(pNames[i])) == null; i++) {
		}
		return valueObj;
	}

	public static String getMemoryStatus() {
		NumberFormat nf = new DecimalFormat("###,###,###.##");

		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();

		return "Memory Usage = " + nf.format((total - free) / 1024 / 1024) +
		    "MB ( JVM memory = " + nf.format(total / 1024 / 1024) + "MB, Free memory = " +
		    nf.format(free / 1024 / 1024) + "MB)";
	}

	  public static HashMap parseXmlParameter(String xmlParam, String rootName) {
			HashMap paramMap = null;
			try {
				INode root = DomUtils.makeNode(xmlParam);
				
				if(!root.getName().equalsIgnoreCase(rootName)) {  
					// Traverse into child nodes recursively to find node matching name.
					INode[] children = root.getNodes();
					if(children != null) {
						for(INode child:children) {
							paramMap = parseXmlParameter(child.getValue().trim(), rootName);
							if(paramMap != null)
								return paramMap;
						}
					}
					else
						return paramMap;
				} else {
					INode[] child = root.getNodes();
					paramMap = new HashMap();
					for (int j = 0; j < child.length;j++) {
						// Add both leaf and non-leaf children. Non-leaf children is necessary
						// for case like mpi-parameters of CaptureProductInfo.
						// Add non-leaf children as string for now for backward compatibility.
						// Ideally, non-leaf children should be extracted into a HashMap itself.
						paramMap.put(child[j].getName(),
						child[j].getValue().trim());
					}
					return paramMap;
				}
				return new HashMap();
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new RuntimeException();
			}
		   }
	  
	public static Map<String, Object> parseXmlParameter(String xmlParam, Class clzName) {
		return parseXmlToMap(xmlParam, clzName.getSimpleName());
	}

	public static Map<String, Object> parseXmlToMap(String xml, String rootName) {
		return parseXmlToMap(xml, rootName, false);
	}

	public static Map<String, Object> parseXmlToMap(String xml,
	                                                String rootName,
	                                                boolean orderedKeys) {
		try {
			INode root = DomUtils.makeNode(xml);
			return parseXmlToMap(root, rootName, orderedKeys);
		} catch (Exception e) {
			throw e instanceof RuntimeException
			        ? (RuntimeException) e
			        : new RuntimeException(e);
		}
	}

	private static Map<String, Object> parseXmlToMap(INode root,
	                                                 String rootName,
	                                                 boolean orderedKeys) {
		Map<String, Object> paramMap = null;
		if (!root.getName().equalsIgnoreCase(rootName)) {
			// Traverse into child nodes recursively to find node matching name.
			INode[] children = root.getNodes();
			if (children != null) {
				for (INode child : children) {
					paramMap = parseXmlToMap(child, rootName, orderedKeys);
					if (paramMap != null) {
						return paramMap;
					}
				}
			}
		} else {
			paramMap = orderedKeys ? new TreeMap() : new HashMap();
			for (INode child : root.getNodes()) {
				String key = child.getName();
				Object newVal;
				if (child.isLeaf()) {
					newVal = child.getValue().trim();
				} else {
					newVal = parseXmlToMap(child, child.getName(), orderedKeys);
				}
				Object oldVal = paramMap.get(key);
				if (oldVal == null) {
					paramMap.put(key, newVal);
				} else if (oldVal instanceof List) {
					((List) oldVal).add(newVal);
				} else {
					List l = new ArrayList();
					l.add(oldVal);
					l.add(newVal);
					paramMap.put(key, l);
				}
			}
		}
		return paramMap;
	}

	public static String createXMLFromMap(Map map, String rootName) {
		try {
			BranchNode root = new BranchNode(rootName);
			createDomFromMap(map, root);
			String xml = DomUtils.makeXml(root);
			return xml;
		} catch (Exception e) {
			throw e instanceof RuntimeException
			        ? (RuntimeException) e
			        : new RuntimeException(e);
		}
	}

	private static void createDomFromMap(Map map, BranchNode root) {
		// sort all values by key, so output is decisive
		Map<String, Object> copy = new TreeMap();
		for (Map.Entry me : (Set<Map.Entry>) map.entrySet()) {
			copy.put(me.getKey().toString(), me.getValue());
		}

		for (Map.Entry<String, Object> me : copy.entrySet()) {
			String key = me.getKey();
			Object val = me.getValue();
			if (val instanceof Map) {
				BranchNode n = new BranchNode(key);
				root.add(n);
				createDomFromMap((Map) val, n);
			} else if (val instanceof List) {
				for (Object o : (List) val) {
					LeafNode n = new LeafNode(key, Fmt.S(o));
					root.add(n);
				}
			} else {
				LeafNode n = new LeafNode(key, Fmt.S(val));
				root.add(n);
			}
		}
	}

	public static Map<String, Object> convertBeanToMap(Object bean) {
		return convertBeanToMap(bean, true, true);
	}

	public static Map<String, Object> convertBeanToMap(Object bean,
	                                                   boolean orderedKey,
	                                                   boolean publicGetterOnly) {
		Map<String, Object> bindedValues = orderedKey ? new TreeMap() : new HashMap();
		PropertyUtilsBean bub = BeanUtilsBean.getInstance().getPropertyUtils();
		PropertyDescriptor[] descriptors = bub.getPropertyDescriptors(bean);

		for (PropertyDescriptor pt : descriptors) {
			// only bind public methods
			if (publicGetterOnly) {
				Method readMethod = pt.getReadMethod();
				if (readMethod == null ||
				    !Modifier.isPublic(pt.getWriteMethod().getModifiers())) {
					continue;
				}
			}
			try {
				bindedValues.put(pt.getName(), bub.getNestedProperty(bean, pt.getName()));
			} catch (IllegalAccessException e) {
				log.info(e.getMessage());
			} catch (InvocationTargetException e) {
				log.info(e.getMessage());
			} catch (NoSuchMethodException e) {
				log.info(e.getMessage());
			}
		}

		return bindedValues;
	}

}

