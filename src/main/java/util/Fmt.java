package util;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Fmt {

	public static String S(Object o) {
		return P(o);
	}

	public static String S(String fmt, Object o1) {
		return P(fmt, new Object[] { o1 });
	}

	public static String S(String fmt, Object o1, Object o2) {
		return P(fmt, new Object[] { o1, o2 });
	}

	public static String S(String fmt, Object o1, Object o2, Object o3) {
		return P(fmt, new Object[] { o1, o2, o3 });
	}

	public static String S(String fmt,
	                       Object o1,
	                       Object o2,
	                       Object o3,
	                       Object o4) {
		return P(fmt, new Object[] { o1, o2, o3, o4 });
	}

	public static String S(String fmt,
	                       Object o1,
	                       Object o2,
	                       Object o3,
	                       Object o4,
	                       Object o5) {
		return P(fmt, new Object[] { o1, o2, o3, o4, o5 });
	}

	public static String S(String fmt,
	                       Object o1,
	                       Object o2,
	                       Object o3,
	                       Object o4,
	                       Object o5,
	                       Object o6) {
		return P(fmt, new Object[] { o1, o2, o3, o4, o5, o6 });
	}

	public static String S(String fmt,
	                       Object o1,
	                       Object o2,
	                       Object o3,
	                       Object o4,
	                       Object o5,
	                       Object o6,
	                       Object o7) {
		return P(fmt, new Object[] { o1, o2, o3, o4, o5, o6, o7 });
	}

	public static String S(String fmt,
	                       Object o1,
	                       Object o2,
	                       Object o3,
	                       Object o4,
	                       Object o5,
	                       Object o6,
	                       Object o7,
	                       Object o8) {
		return P(fmt, new Object[] { o1, o2, o3, o4, o5, o6, o7, o8 });
	}

	public static String S(String fmt,
	                       Object o1,
	                       Object o2,
	                       Object o3,
	                       Object o4,
	                       Object o5,
	                       Object o6,
	                       Object o7,
	                       Object o8,
	                       Object o9) {
		return P(fmt, new Object[] { o1, o2, o3, o4, o5, o6, o7, o8, o9 });
	}

	public static String S(String fmt,
	                       Object o1,
	                       Object o2,
	                       Object o3,
	                       Object o4,
	                       Object o5,
	                       Object o6,
	                       Object o7,
	                       Object o8,
	                       Object o9,
	                       Object o10) {
		return P(fmt, new Object[] { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10 });
	}

	public static String S(String fmt, List objs) {
		return S(fmt, objs.toArray());
	}

	public static String S(String fmt, Object[] objs) {
		return P(fmt, objs);
	}

	private static String P(String fmt, Object[] objs) {
		if (fmt == null) {
			return null;
		}
		int objIndex = 0;
		StringBuilder buffer = new StringBuilder();
		char[] chars = fmt.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (i < chars.length - 1 && chars[i] == '%') {
				if (chars[i + 1] == '%') {
					buffer.append(chars[i]);
					buffer.append(chars[i + 1]);
				} else if (chars[i + 1] == 's') {
					if (objIndex < objs.length) {
						buffer.append(P(objs[objIndex++]));
					} else {
						buffer.append(chars[i]);
						/* basically the format args are out of bounds */
						buffer.append(chars[i + 1]);
					}
				} else {
					buffer.append(chars[i]);
					/* we can handle the other formats here in the future */
					buffer.append(chars[i + 1]);
				}
				i++;
			} else if (i < chars.length && chars[i] == '\\'
			        && chars[i + 1] == 'n') {
				buffer.append("\n");
			} else if (i < chars.length && chars[i] == '\\'
			        && chars[i + 1] == 't') {
				buffer.append("\t");
			} else {
				buffer.append(chars[i]);
			}
		}

		return buffer.toString();
	}

	private static String P(Object o) {
		return P(o, 0);
	}

	private static String P(Object o, int tabIndex) {
		String tab = "   ";
		if (o == null) {
			return "<null>";
		}
		StringBuilder b1 = new StringBuilder();
		if (o instanceof Map) {
			if (tabIndex == 0) {// in case if root is Map
				tabIndex = 1;
			}
			/* other smart indentation we can do */
			Map map = (Map) o;
			b1.append("\n");
			for (int x = 0; x < tabIndex - 1; x++) {
				b1.append(tab);
			}
			b1.append("{");
			b1.append("\n");
			Iterator itor = map.keySet().iterator();
			while (itor.hasNext()) {
				Object key1 = itor.next();
				Object val1 = map.get(key1);
				for (int x = 0; x < tabIndex; x++) {
					b1.append(tab);
				}
				b1.append(Fmt.S("%s = %s", P(key1), P(val1, tabIndex + 1)));
				b1.append("\n");
			}
			for (int x = 0; x < tabIndex - 1; x++) {
				b1.append(tab);
			}
			b1.append("}");
		} else if (o instanceof List) {
			/* other smart indentation we can do */
			List list = (List) o;
			if (list.size() > 10) {
				b1.append("\n");
				for (int x = 0; x < tabIndex; x++) {
					b1.append(tab);
				}
				b1.append("[");
				b1.append("\n");
				for (int x = 0; x <= tabIndex; x++) {
					b1.append(tab);
				}
				for (int i = 0; i < list.size(); i++) {
					if (i != 0) {
						b1.append(",");
						b1.append("\n");
						for (int x = 0; x <= tabIndex; x++) {
							b1.append(tab);
						}
					}
					b1.append(P(list.get(i), tabIndex + 1));
				}
				b1.append("\n");
				for (int x = 0; x < tabIndex; x++) {
					b1.append(tab);
				}
				b1.append("]");

			} else {
				b1.append(o.toString());
			}
		} else if (o instanceof Object[]) {
			Object[] objs = (Object[]) o;
			if (objs.length > 5) {
				b1.append("\n");
				for (int x = 0; x < tabIndex; x++) {
					b1.append(tab);
				}
				b1.append("[");
				b1.append("\n");
				for (int x = 0; x <= tabIndex; x++) {
					b1.append(tab);
				}

				for (int i = 0; i < objs.length; i++) {
					if (i != 0) {
						b1.append(",");
						b1.append("\n");
						for (int x = 0; x <= tabIndex; x++) {
							b1.append(tab);
						}
					}
					b1.append(P(objs[i], tabIndex + 1));
				}
				b1.append("\n");
				for (int x = 0; x < tabIndex; x++) {
					b1.append(tab);
				}
				b1.append("]");

			} else  {
				b1.append("[");
				for (int i = 0; i < objs.length; i++) {
					if (i != 0) {
						b1.append(",");
					}

					b1.append(P(objs[i], tabIndex + 1));
				}
				b1.append("]");
			}
		} else if (o instanceof int[]) {
			b1.append("[ ");
			for (int i : (int[]) o) {
				b1.append(i).append(" ");
			}
			b1.append("]");
		} else if (o instanceof long[]) {
			b1.append("[ ");
			for (long i : (long[]) o) {
				b1.append(i).append(" ");
			}
			b1.append("]");
		} else if (o instanceof boolean[]) {
			b1.append("[ ");
			for (boolean i : (boolean[]) o) {
				b1.append(i).append(" ");
			}
			b1.append("]");
		} else if (o instanceof float[]) {
			b1.append("[ ");
			for (float i : (float[]) o) {
				b1.append(i).append(" ");
			}
			b1.append("]");
		} else if (o instanceof double[]) {
			b1.append("[ ");
			for (double i : (double[]) o) {
				b1.append(i).append(" ");
			}
			b1.append("]");
		} else if (o instanceof Throwable) {
			Throwable t = (Throwable) o;
			StackTraceElement[] elems = t.getStackTrace();
			b1.append(Fmt.S("Exception = %s:\n" + "\tMessage: %s\n",
					t.getClass().getName(),
					t
			        .getMessage()));
			for (int i = 0; i < elems.length; i++) {
				b1.append(tab);
				b1.append(elems[i]);
				b1.append("\n");
			}
			if (t.getCause() != null) {
				b1.append(Fmt.S("Exception Cause :%s\n", t.getCause()));
			}
		} else {
			/* unknown object */
			b1.append(o.toString());
		}
		return b1.toString();
	}

	private static void getNullPointerException (int i)
	{
		Object x = null;
		if (i == 15) {
			x.hashCode();
		}
		getNullPointerException(i+1);
	}

	private static void getClassCastException (int i)
	{
		if (i == 15) {
			Exception ie1 = null;
			try { 
				getNullPointerException(0);
			}
			catch (Exception ie) {
				ie1 = ie;
			}
			RuntimeException x = new ClassCastException("This is class cast exception");
			x.initCause(ie1);
			throw x;			
		}
		getClassCastException(i+1);
	}

	public static void main(String[] args) {
		System.out.println(Fmt.S("This is test"));
		System.out.println(Fmt.S("This is test for %s", "String-1"));
		System.out.println(Fmt.S("This is test for %s %s",
		    "String-1",
		    "String-2"));
		System.out.println(Fmt.S("This is test for %s %s %s",
		    "String-1",
		    "String-2",
		    "String-3"));
		System.out.println(Fmt.S("This is test for %s %s %s %s",
		    "String-1",
		    "String-2",
		    "String-3",
		    "String-4"));

		System.out.println(Fmt
		        .S("This is test for out-of-bounds %s %s %s %s %s %s %s",
		            "String-1",
		            "String-2",
		            "String-3",
		            "String-4"));

		System.out.println(Fmt.S("This is test for escape char %s %s %s %% %s",
		    "String-1",
		    "String-2",
		    "String-3",
		    "String-4"));

		Exception e1 = null;
		try { 
			getClassCastException(0);
		}
		catch (Exception e) {
			e1 = e;
		}
		System.out.println(Fmt.S("This is test for exception %s %s %s %s %s %s",
		    "String-1",
		    "String-2",
		    "String-3",
		    "String-4",
		    new RuntimeException(),
		    e1));

		ArrayList list = new ArrayList();
		list.add("String-1");
		list.add("String-2");
		list.add("String-3");
		System.out.println(Fmt.S("This is test for list :%s", list));

		System.out.println(Fmt
		        .S("This is test for array <before>: new String[] :"
		                + new String[] { "String-1", "String-2", "String-3" }));

		System.out.println(Fmt
		        .S("This is test for array <after>: new String[] :%s",
		            new Object[] { "String-Chak", "String-2", "String-3" }));

		System.out
		        .println(Fmt
		                .S("This is test for array <after newline>: new String[] :\n%s",
		                    new Object[] {
		                            "String-Chak",
		                            "String-2",
		                            "String-3" }));

		System.out.println(Fmt.S("tabbed strings :%s\t%s\t\t%s",
		    "String-1",
		    "String-2",
		    "String-3"));

		System.out.println(Fmt.S("strings :%s", new Object[] {
		        "a1",
		        "a2",
		        "a3",
		        "a4",
		        "a5",
		        new Object[] {
		                "a51",
		                "a52",
		                "a53",
		                "a54",
		                "a55",
		                "a56",
		                "a57",
		                new Object[] { "a581", "a582" },
		                "a6",
		                "a7",
		                "a8",
		                "a9" } }));

		HashMap map = new HashMap();
		map.put("k1", "val1");
		map.put("k2", "val2");

		HashMap map2 = new HashMap();
		map2.put("k11", "value11");

		HashMap map3 = new HashMap();
		map3.put("k21", new Object[] {
		        "a51",
		        "a52",
		        "a53",
		        "a54",
		        "a55",
		        "a56",
		        "a57",
		        new Object[] { "a581", "x", "y", "z", map2, list },
		        "a6",
		        "a7",
		        "a8",
		        "a9" });

		map.put("map2", map2);
		map.put("map3", map3);
		System.out.println(Fmt.S(map));

	}
}
