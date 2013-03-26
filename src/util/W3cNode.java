package util;

public abstract class W3cNode extends QNode {
	private static final String __CVS_VERSION = "@{#}CVS versionInfo: $Id: W3cNode.java 104874 2010-09-30 21:30:17Z cc $";
		org.w3c.dom.Element w3cNode;

	public W3cNode(org.w3c.dom.Element w3cNode) {
		this.w3cNode = w3cNode;
	}

	public String getName() {
		return w3cNode.getTagName();
	}

	public String getAttribute(String name) {
		return w3cNode.getAttribute(name);
	}

	public String[] getAttributeNames() {
		org.w3c.dom.NamedNodeMap map = w3cNode.getAttributes();
		String[] rtn = new String[map.getLength()];
		for( int i=0; i<rtn.length; i++ ) {
			rtn[i] = map.item(i).getNodeName();
		}
		return rtn;
	}

	public static INode makeNode(org.w3c.dom.Element w3cNode) {
		return DomUtils.isLeaf(w3cNode) 
			? (INode)new W3cLeafNode(w3cNode)
			: (INode)new W3cBranchNode(w3cNode)
	;
	}

	public INode getNode() {
		INode[] nodes = getNodes();
		return nodes.length==1 ? nodes[0] : null;
	}

	public String getText() {
		org.w3c.dom.NodeList children = w3cNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			org.w3c.dom.Node child = children.item(i);
			if (child.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
				return child.getNodeValue(); 
		}
		// No text children
		return null;
	}
}
