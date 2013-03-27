package util;


import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;


public class LeafNode extends AbstractNode {
	private String value;

	public LeafNode(String name) {
		this(name, StringUtils.EMPTY);
	}

	public LeafNode(String name, String value) {
		super(name);
		if(value == null) {
			throw new RuntimeException("value is null in LeafNode");
		}
		this.value = StringEscapeUtils.escapeXml(value);
	}

	public INode getNode(String n) {
		return null;
	}

	public INode[] getNodes(String n) {
		return new INode[0];
	}

	public INode[] getNodes() {
		return new INode[0];
	}

	public String toString() {
		return value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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
		startTag(buf);
		buf.append(value);
		endTag(buf);
		buf.append('\n');
	}
*/
}
