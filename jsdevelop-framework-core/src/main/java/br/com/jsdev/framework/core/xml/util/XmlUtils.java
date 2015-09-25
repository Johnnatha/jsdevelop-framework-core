package br.com.jsdev.framework.core.xml.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

public class XmlUtils {
    public static String buildXml(Document document)
                    throws TransformerException {

            DOMSource domSource = new DOMSource(document);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            
            //final String xmlMeta = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
            return writer.toString();
    }
    
    public static Document buildDocument(String xmlFile) throws ParserConfigurationException, SAXException, IOException{
            
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(xmlFile.getBytes());

            Document doc = docBuilder.parse(stream);
           
            
            return doc;
    }
}
