package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


abstract class QNode implements INode {
	private static final String __CVS_VERSION = "@{#}CVS versionInfo: $Id: QNode.java 104874 2010-09-30 21:30:17Z cc $";

	public INode getNode(String nodeName,String attrName,String attrValue) {
		Map map = new HashMap();
		map.put(attrName,attrValue);
		return getNode(nodeName,map);
	}

	public INode getNode(String nodeName,Map attrs) {
		INode[] nodes = getNodes(nodeName,attrs);
		if( nodes.length==0 ) {
			return new NullNode(getName());
		}
		if( nodes.length > 1 ) {
			return new NullNode(getName(),"[error "+nodes.length+" nodes]");
		}
		return (INode)nodes[0];
	}

	public INode[] getNodes(String nodeName,String attrName,String attrValue) {
		Map map = new HashMap();
		map.put(attrName,attrValue);
		return getNodes(nodeName,map);
	}

	public INode[] getNodes(String nodeName,Map attrs) {
		INode[] nodes = getNodes(nodeName);
		List v = new ArrayList();
outer:
		for( int i=0; i<nodes.length; i++ ) {
			INode node = nodes[i];
			String[] keys = node.getAttributeNames();
			for( int j=0; j<keys.length; j++ ) {
				String key = keys[j];
				String s = (String)attrs.get(key);
				if( s!=null && !s.equals(node.getAttribute(key)) ) {
					continue outer;
				}
			}
/*
			for( int j=0; j<attrs.length; j++ ) {
				String[] attr = attrs[j];
				if( !attr[1].equals(node.getAttribute(attr[0])) )
					continue outer;
			}
*/
			v.add(node);
		}
		return (INode[])v.toArray(new INode[0]);
	}

	public String getText() {
		throw new UnsupportedOperationException();
	}	
}
