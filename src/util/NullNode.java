package util;

class NullNode extends LeafNode {
	private static final String __CVS_VERSION = "@{#}CVS versionInfo: $Id: NullNode.java 104874 2010-09-30 21:30:17Z cc $";

	NullNode(String name) {
//		super(name,"[null]");
		super(name,null);
	}

	NullNode(String name,String value) {
		super(name,value);
	}

	public boolean isNull() {
		return true;
	}

	public boolean equals(Object obj) {
		return false;
	}

}