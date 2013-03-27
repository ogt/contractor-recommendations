package util;


public interface INode {
//	public static final String __CVS_VERSION = "@{#}CVS versionInfo: $Id: INode.java 104874 2010-09-30 21:30:17Z cc $";
		public String getName();
	public INode getNode(String name);
//	public boolean hasNode(String name);
//	public INode getNodeExact(String name) throws MakeFromDomException;
	public INode[] getNodes(String name);
	public INode[] getNodes();  // gets all children
	public INode getNode();  // assumes one node
	public String toString();
	public String getValue();
//	public boolean isNull();
	public boolean isLeaf();
	public boolean equals(Object obj);
	public String getAttribute(String name);
	public String[] getAttributeNames();

	// queries
//	public INode getNode(String nodeName,String attrName,String attrValue);
//	public INode getNode(String nodeName,Map attrs);
//	public INode[] getNodes(String nodeName,String attrName,String attrValue);
//	public INode[] getNodes(String nodeName,Map attrs);
	
	public String getText();
}