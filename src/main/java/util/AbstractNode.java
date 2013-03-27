package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



abstract class AbstractNode extends QNode {
	private static final String __CVS_VERSION = "@{#}CVS versionInfo: $Id: AbstractNode.java 104874 2010-09-30 21:30:17Z cc $";
		String name;
	List attrList = new ArrayList();
	Map attrMap = new HashMap();

	public AbstractNode(String name) {
		this.name = name;
	}

	public final String getName() {
		return name;
	}

	public final String getAttribute(String name) {
		return (String)attrMap.get(name);
	}

	public final void setAttribute(String name,String value) {
		attrList.add(name);
		attrMap.put(name,value);
	}

	public String[] getAttributeNames() {
		return (String[])attrList.toArray(new String[0]);
	}

	public INode getNode() {
		INode[] nodes = getNodes();
		return nodes.length==1 ? nodes[0] : null;
	}

/*
	final void startTag(StringBuffer buf) {
		buf.append("<");
		buf.append(name);
		String[] names = (String[])attrList.toArray(new String[0]);
		for( int i=0; i<names.length; i++ ) {
			String s = names[i];
			buf.append(" ");
			buf.append(s);
			buf.append("=\"");
			buf.append(getAttribute(s));
			buf.append("\"");
		}
		buf.append(">");
	}

	final void endTag(StringBuffer buf) {
		buf.append("</");
		buf.append(name);
		buf.append(">");
	}
*/
}
