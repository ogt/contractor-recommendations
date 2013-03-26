package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class BranchNode extends AbstractNode {
	private static final String __CVS_VERSION = "@{#}CVS versionInfo: $Id: BranchNode.java 104874 2010-09-30 21:30:17Z cc $";
		List list = new ArrayList();
	Map children = new HashMap();

	public BranchNode(String name) {
		super(name);
	}

	public void add(INode node) {
		list.add(node);
		String name = node.getName();
		List nodes = (List)children.get(name);
		if( nodes==null ) {
			nodes = new ArrayList();
			children.put(name,nodes);
		}
		nodes.add(node);
	}

	public INode getNode(String name) {
		List nodes = (List)children.get(name);
		if( nodes == null )
//			return new NullNode(name);
			return null;
		if( nodes.size() != 1 )
//			return new NullNode(name,"[error "+nodes.size()+" nodes]");
			return null;
		return (INode)nodes.get(0);
	}

/*
	public INode getNodeExact(String name)
		throws MakeFromDomException
	{
		List nodes = (List)children.get(name);
		if( nodes == null )
			throw new MakeFromDomException("malformed '"+getName()+"' tag: "+name+" not found");
		if( nodes.size() != 1 )
			throw new MakeFromDomException("malformed '"+getName()+"' tag: "+nodes.size()+" nodes found for "+name);
		return (INode)nodes.get(0);
	}
*/
	public INode[] getNodes(String name) {
		List nodes = (List)children.get(name);
		return nodes!=null ? (INode[])nodes.toArray(new INode[0]) : new INode[0];
	}

	public INode[] getNodes() {
		return (INode[])list.toArray(new INode[0]);
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

}

