package util;


public class W3cLeafNode extends W3cNode {
	private static final String __CVS_VERSION = "@{#}CVS versionInfo: $Id: W3cLeafNode.java 104874 2010-09-30 21:30:17Z cc $";

	public W3cLeafNode(org.w3c.dom.Element w3cNode) {
		super(w3cNode);
	}

	public INode getNode(String name) {
//		return new NullNode(name);
		return null;
	}

	public boolean hasNode(String name) {
		return false;
	}

	public INode[] getNodes(String name) {
		return new INode[0];
	}

	public INode[] getNodes() {
		return new INode[0];
	}

	public String toString() {
//		org.w3c.dom.Node child = w3cNode.getFirstChild();
//		return child==null ? null : child.getNodeValue();
		return getValue();
	}

	public String getValue() {
		org.w3c.dom.Node child = w3cNode.getFirstChild();
		return child==null ? "" : child.getNodeValue();
	}

	public boolean isLeaf() {
		return true;
	}

	public boolean equals(Object obj) {
		if( obj==null )
			return false;
		if( obj instanceof String )
			return toString().equals(obj);
		if( obj instanceof INode ) {
			INode node = (INode)obj;
			if( !node.isLeaf() )
				return false;
			return toString().equals(node.toString());
		}
		return super.equals(obj);
	}

/*
	public void toXml(StringBuffer buf,int indent) {
		for( int i=0; i<indent; i++ )
			buf.append('\t');
		buf.append("<");
		buf.append(getName());
		buf.append(">");
		buf.append(toString());
		buf.append("</");
		buf.append(getName());
		buf.append(">\n");
	}
*/
}
