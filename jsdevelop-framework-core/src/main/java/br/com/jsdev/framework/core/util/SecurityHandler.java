package br.com.jsdev.framework.core.util;

import java.util.Set;

import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import br.com.jsdev.framework.web.servlet.WebContext;

public class SecurityHandler implements SOAPHandler<SOAPMessageContext> {

	public static final String SECURITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	public static final String SECURITY_PROPERTY = "Security";
	public static final String USERNAME_PROPERTY = "Username";
	public static final String PASSWORD_PROPERTY = "Password";
	public static final String USERNAME_TOKEN_PROPERTY = "UsernameToken";

	
	public boolean handleMessage(SOAPMessageContext context) {
		
        if (isOutbound(context)) {
            try {
                final SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
                SOAPHeader header = envelope.getHeader();
                
                if (header == null){
                    header = envelope.addHeader();
                }
                
                final String prefix = "wsse";
                final SOAPElement security = header.addChildElement(SECURITY_PROPERTY, prefix,SECURITY_NS);
                final SOAPElement usernameToken = security.addChildElement(USERNAME_TOKEN_PROPERTY, prefix);
                usernameToken.addChildElement(USERNAME_PROPERTY, prefix).addTextNode(getCurrentUsername());
                usernameToken.addChildElement(PASSWORD_PROPERTY, prefix).addTextNode(getCurrentPassword());

            } catch (SOAPException e) {
				throw new ProtocolException(e);
			}
        }
        return true;		
	}


	public boolean handleFault(SOAPMessageContext context) {
		return true;
	}


	public void close(MessageContext context) {
	}


	public Set<QName> getHeaders() {
		return null;
	}

	private static Boolean isOutbound(SOAPMessageContext context) {
		return (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
	}
	
	private static String getCurrentPassword() {
		final ServletContext context = WebContext.getCurrentInstance().getContext();
		final String password = context.getInitParameter("jsdev.bpm_password");
		return password;
	}
	
	private static String getCurrentUsername() {
		final ServletContext context = WebContext.getCurrentInstance().getContext();
		final String username = context.getInitParameter("jsdev.bpm_username");
		return username;
	}
	
}
