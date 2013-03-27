package util;



import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.lang3.StringUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xerces.parsers.DOMParser;

public class DomUtils {
	public static final String __CVS_VERSION = "@{#}CVS versionInfo: $Id: DomUtils.java 104874 2010-09-30 21:30:17Z cc $";

    private static Logger log = Logger.getLogger(DomUtils.class.getName());

	/**
	 * <p>A shared (static) parser, used by {@link #makeNode(Reader)}.  This
	 * avoids creating a new parser every time (as was previously done) in that
	 * method and instead the parser is just reset before each use.
	 * In profiling tests, creating the parser took ~25msecs while
	 * resetting it takes ~4 msecs.
	 * <p>It is safe to have only 1 shared static instance of the parser,
	 * as the makeNode() method using it is static and synchronized.
	 */
	private static DOMParser parser = new DOMParser();

	public static DocumentBuilderFactory getXercesDocFactory() {
		try {
            Class clazz = Class.forName(
					"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
            return (DocumentBuilderFactory)clazz.newInstance();
        } catch  (ClassNotFoundException cnfe) {
	    	throw new FactoryConfigurationError(cnfe);
		} catch (IllegalAccessException iae) {
		    throw new FactoryConfigurationError(iae);
		} catch (InstantiationException ie) {
		    throw new FactoryConfigurationError(ie);
		}
	}

	public static INode makeNode(String xml)
		throws IOException, org.xml.sax.SAXException {
		INode result = null;
		try {
			result = makeNode(new StringReader(xml));
		}
		catch(NullPointerException ex) {
			log.info("NullPointerException for xml: "+ xml);
		}
		catch(org.xml.sax.SAXException saxEx) {
			log.info("SAXException for xml: "+ xml);
			throw saxEx;
		}
		catch(IOException ioEx) {
			log.info("IOException for xml: "+ xml);
			throw ioEx;
		}
		return result;
	}

	public static synchronized  INode makeNode(Reader reader)
		throws IOException, org.xml.sax.SAXException
	{
		parser.reset();

		parser.parse(new org.xml.sax.InputSource(reader));

		return W3cNode.makeNode( parser.getDocument().getDocumentElement() );
	}

	static boolean isLeaf(org.w3c.dom.Node n) {
		org.w3c.dom.NodeList list = n.getChildNodes();
		int len = list.getLength();
		for( int i=0; i<len; i++ ) {
			if( list.item(i).getNodeType()==org.w3c.dom.Node.ELEMENT_NODE )
				return false;
		}
		return true;
	}

	public static void makeXml(INode node,StringBuffer buf, int indent) {
		makeXml(node, buf, indent, false);
	}

	public static void makeXml(INode node,StringBuffer buf, int indent, boolean compactMode) {
		String name = node.getName();
		for( int i=0; i<indent; i++ )
			buf.append('\t');
		buf.append("<");
		buf.append(name);
		String[] names = node.getAttributeNames();
		for( int i=0; i<names.length; i++ ) {
			String s = names[i];
			buf.append(" ");
			buf.append(s);
			buf.append("=\"");
			buf.append(node.getAttribute(s));
			buf.append("\"");
		}

		if(!compactMode 
				|| (node.isLeaf() && StringUtils.isNotBlank(node.getValue())) 
				|| node.getNodes().length > 0) {
			buf.append(">");
			if( node.isLeaf() ) {
				buf.append(node.toString());
			} else {
				if (indent!=-1) buf.append("\r\n");
				INode[] nodes = node.getNodes();
				for( int i=0; i<nodes.length; i++ ) {
					makeXml(nodes[i], buf, indent != -1 ? indent + 1 : -1, compactMode);
				}
				for( int i=0; i<indent; i++ )
					buf.append('\t');
			}
			buf.append("</");
			buf.append(name);
			buf.append(">");
		} else {
			buf.append("/>");
		}

		if (indent!=-1) buf.append("\r\n");
	}

	public static String makeXml(INode node) {
		return makeXml(node, false);
	}

	public static String makeXml(INode node, boolean compactMode) {
		StringBuffer buf = new StringBuffer();
		makeXml(node, buf, 0, compactMode);
		return buf.toString();
	}

	public static String makeXml(SupportsDom obj) {
		return makeXml(obj.toNode());
	}

	public static SupportsDom makeFromDom(Map classMap,INode node)
		throws MakeFromDomException
	{
		String nodeName = node.getName();
		Class cls = (Class)classMap.get(nodeName);
		if (cls==null) {
			throw new MakeFromDomException("unknown tag "+nodeName);
		}
		SupportsDom obj;
		try {
			obj = (SupportsDom)cls.newInstance();
		} catch (Exception e) {
			log.info(e.getMessage());
			RuntimeException rte = new RuntimeException(e.getMessage());
			rte.initCause(e);
			throw rte;
		}
		obj.construct(classMap,node);
		return obj;
	}

	public static SupportsDom makeFromXml(Map classMap,String xml)
		throws MakeFromDomException
	{
		return makeFromXml(classMap,xml,null);
	}

	public static SupportsDom makeFromXml(Map classMap,String xml,String outerTag)
		throws MakeFromDomException
	{
		INode node;
		try {
			node = makeNode(xml);
		} catch(org.xml.sax.SAXException e) {
			log.info(e.getMessage());
			MakeFromDomException mfde = new MakeFromDomException(e.getMessage());
			mfde.initCause(e);
			throw mfde;
		} catch(IOException e) {
			log.info(e.getMessage());
			MakeFromDomException mfde = new MakeFromDomException(e.getMessage());
			mfde.initCause(e);
			throw new MakeFromDomException(e.getMessage());
		}
		if( outerTag != null ) {
			String name = node.getName();
			if( !name.equals(outerTag) )
				throw new MakeFromDomException(
					"expected '"+outerTag+"' for outer tag but got '"+name+"'"
				);
			INode[] nodes = node.getNodes();
			if( nodes.length != 1 )
				throw new MakeFromDomException("malformed '"+name+"' tag: "+nodes.length+" nodes found");
			node = nodes[0];
		}
		return makeFromDom(classMap,node);
	}



	public static DocumentBuilder getDocumentBuilder(boolean validating) {
		return getDocumentBuilder(validating, false);
	}

	public static DocumentBuilder getDocumentBuilder(boolean validating, boolean verbose) {
		DocumentBuilder result = null;
		for(int i = 0; i < 10 && result == null; i++) {
			try {
				DocumentBuilderFactory dbfact =
						getXercesDocFactory();
				dbfact.setValidating(validating);
				dbfact.setNamespaceAware(false);
				result = dbfact.newDocumentBuilder();
			}
			catch(Exception ex) {
				result = null;
				if (verbose)
					ex.printStackTrace();
				else if(i >= 9) //if couldn't get the DocumentBuilder after trying 10 times, log the stack trace for diagnosis
					ex.printStackTrace();
			}
		}
		return result;
	}

}

