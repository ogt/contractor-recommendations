package util;

import java.util.Map;


public interface SupportsDom {
	public static final String __CVS_VERSION = "@{#}CVS versionInfo: $Id: SupportsDom.java 104874 2010-09-30 21:30:17Z cc $";
		public void construct(Map classMap,INode node) throws MakeFromDomException;
	public INode toNode();
}
