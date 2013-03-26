package util;

import java.util.ArrayList;
import java.util.List;


public class W3cBranchNode extends W3cNode {
	private static final String __CVS_VERSION = "@{#}CVS versionInfo: $Id: W3cBranchNode.java 104874 2010-09-30 21:30:17Z cc $";

	public W3cBranchNode(org.w3c.dom.Element w3cNode) {
		super(w3cNode);
	}

	public INode getNode(String name) {
		INode[] nodes = getNodes(name);
		if( nodes.length != 1 )
//			return new NullNode(name,"[error "+nodes.length+" nodes]");
			return null;
		return nodes[0];
	}

	private INode[] getNodes(org.w3c.dom.NodeList list) {
		List nodes = new ArrayList();
		int len = list.getLength();
		for( int i=0; i<len; i++ ) {
			org.w3c.dom.Node t = list.item(i);
			if( t.getNodeType()!=org.w3c.dom.Node.ELEMENT_NODE )
				continue;
			nodes.add( makeNode((org.w3c.dom.Element)t) );
		}
		return (INode[])nodes.toArray(new INode[0]);
	}

	public INode[] getNodes(String name) {
//		return getNodes(w3cNode.getElementsByTagName(name));
		org.w3c.dom.NodeList list = w3cNode.getChildNodes();
		List nodes = new ArrayList();
		int len = list.getLength();
		for( int i=0; i<len; i++ ) {
			org.w3c.dom.Node t = list.item(i);
			if( t.getNodeType()!=org.w3c.dom.Node.ELEMENT_NODE )
				continue;
			if( !t.getNodeName().equals(name) )
				continue;
			nodes.add( makeNode((org.w3c.dom.Element)t) );
		}
		return (INode[])nodes.toArray(new INode[0]);
	}

	public INode[] getNodes() {
		return getNodes(w3cNode.getChildNodes());
	}

	public String toString() {
		return DomUtils.makeXml(this);
	}

	public String getValue() {
		return DomUtils.makeXml(this);
	}

	public boolean isLeaf() {
		return false;
	}

/*
	public void toXml(StringBuffer buf,int indent) {
		for( int i=0; i<indent; i++ )
			buf.append('\t');
		buf.append("<");
		buf.append(getName());
		buf.append(">\n");

		org.w3c.dom.NodeList nodes = w3cNode.getChildNodes();
		int len = nodes.getLength();
		for( int i=0; i<len; i++ ) {
			org.w3c.dom.Node t = nodes.item(i);
			if( t.getNodeType()!=org.w3c.dom.Node.ELEMENT_NODE )
				continue;
			makeNode((org.w3c.dom.Element)t).toXml(buf,indent+1);
		}

		for( int i=0; i<indent; i++ )
			buf.append('\t');
		buf.append("</");
		buf.append(getName());
		buf.append(">\n");
	}
*/
}
