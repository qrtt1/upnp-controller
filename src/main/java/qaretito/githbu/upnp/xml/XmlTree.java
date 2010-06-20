package qaretito.githbu.upnp.xml;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlTree {

    static SAXParserFactory factory;
    static {
        factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
    }

    static class TreeHandler extends DefaultHandler {

        private Node currentNode;
        private StringBuilder direction = new StringBuilder();
        
        public INode getTree(){
            return currentNode;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            direction.append('<');
            currentNode = new Node(currentNode);
            currentNode.setName(localName);
            currentNode.setAttributes(attributes);

            if (currentNode.getParent() != null) {
                currentNode.getParent().addChild(currentNode);
            }
        }

        @Override
        public void endDocument() throws SAXException {
            while (currentNode.getParent() != null) {
                currentNode = currentNode.getParent();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            try {
                if (currentNode != null) {
                    String s = new String(ch).substring(start, start + length);
                    currentNode.setText(s.trim());
                }
            } catch (Exception ignored) {
            }

        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            direction.append('>');
            if (direction.length() >= 2) {
                boolean seeDimon = (direction.charAt(direction.length() - 2) == '<')
                        && direction.charAt(direction.length() - 1) == '>';
                if (seeDimon && currentNode.getParent() != null) {
                    currentNode = currentNode.getParent();
                    direction.setLength(direction.length() - 2);
                }
            } 
        }
    }
    
    public static INode makeTree(String xml) throws Exception{
        SAXParser parser = XmlTree.factory.newSAXParser();
        TreeHandler handle = new TreeHandler();
        parser.parse(new InputSource(new StringReader(xml)), handle);
        return handle.getTree();
    }
}
