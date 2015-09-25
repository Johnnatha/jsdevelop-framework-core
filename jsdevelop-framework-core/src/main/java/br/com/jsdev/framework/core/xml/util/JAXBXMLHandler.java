package br.com.jsdev.framework.core.xml.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

@SuppressWarnings("restriction")
public class JAXBXMLHandler<T> {
	
	
	public T unmarshal(String xml, Class<T> clazz) throws JAXBException, InstantiationException, IllegalAccessException{		
		
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    StringReader reader = new StringReader(xml);
	    
	    @SuppressWarnings("unchecked")
		T result = (T) jaxbUnmarshaller.unmarshal(reader);
	    
	    return result;
	}
	
	public static String marshal(Object obj) throws JAXBException{
		
		JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter stringWriter = new StringWriter();        
        
        marshaller.marshal(obj, stringWriter);
        
        final String xmlMeta = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        
        return stringWriter.toString().replace(xmlMeta, "");
	}
}
 