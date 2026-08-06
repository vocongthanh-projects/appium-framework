package vn.ots.qc.mobile.evidence;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

/** Redacts secure input values from Appium XML without mutating the live application. */
public final class PageSourceRedactor {
    public static final String MASK = "<redacted>";

    public String redact(String xml) {
        if (xml == null || xml.isBlank()) return xml == null ? "" : xml;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            var builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
                @Override public void error(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
                    throw exception;
                }
                @Override public void fatalError(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
                    throw exception;
                }
            });
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            redact(document.getDocumentElement());
            TransformerFactory transformers = TransformerFactory.newInstance();
            transformers.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            var transformer = transformers.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter output = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(output));
            return output.toString();
        } catch (Exception ignored) {
            return xml.replaceAll("(?i)(password|token)(\\s*[:=]\\s*[\\\"']?)[^\\s\\\"'&,;]+", "$1$2" + MASK);
        }
    }

    private void redact(Node node) {
        if (node instanceof Element element && sensitive(element)) {
            for (String attribute : new String[]{"value", "text", "label"}) {
                if (element.hasAttribute(attribute)) element.setAttribute(attribute, MASK);
            }
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) redact(child);
    }

    private boolean sensitive(Element element) {
        return truthy(element, "password") || truthy(element, "secure") || truthy(element, "data-sensitive")
                || element.getAttribute("type").toLowerCase().contains("securetextfield");
    }

    private boolean truthy(Element element, String name) {
        return "true".equalsIgnoreCase(element.getAttribute(name));
    }
}
