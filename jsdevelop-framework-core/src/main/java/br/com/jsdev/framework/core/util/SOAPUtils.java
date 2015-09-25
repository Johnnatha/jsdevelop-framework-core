package br.com.jsdev.framework.core.util;

import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.handler.Handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import br.com.jsdev.framework.web.servlet.WebContext;

@SuppressWarnings("restriction")
public class SOAPUtils {

	
	public static <E extends Service> E createService(Class<E> clazz) {
		try {
			WebServiceClient client = clazz.getAnnotation(WebServiceClient.class);
			Constructor<E> constructor = clazz.getConstructor(URL.class, QName.class);

			final URL address = new URL(client.wsdlLocation());
			final URL newAddress = translateAddress(address, true);
			final QName qName = new QName(client.targetNamespace(), client.name());

			return constructor.newInstance(newAddress, qName);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	public static <E> E register(E provider) {
		try {

			final BindingProvider bindingProvider = (BindingProvider) provider;
			final URL address = new URL((String) bindingProvider.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
			final URL newAddress = translateAddress(address, false);
			bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, newAddress.toString());
			
			List<Handler> newHandlers = new LinkedList<Handler>();
			
			/* WS Security Authentication*/
			newHandlers.add(new SecurityHandler());

			List<Handler> oldHandlers = bindingProvider.getBinding().getHandlerChain();
			if (oldHandlers != null) {
				newHandlers.addAll(oldHandlers);
			}
			bindingProvider.getBinding().setHandlerChain(newHandlers);
			
			return provider;

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	private static URL translateAddress(final URL address, final boolean convertPath) throws MalformedURLException {		
		final ServletContext context = WebContext.getCurrentInstance().getContext();
		final String protocol = StringUtils.defaultIfEmpty(context.getInitParameter("jsdev.bpm_protocol"), address.getProtocol());
		final String host = StringUtils.defaultIfEmpty(context.getInitParameter("jsdev.bpm_host"), address.getHost());
		final int port = NumberUtils.toInt(context.getInitParameter("jsdev.bpm_port"), address.getPort());

		final String path;
		path = address.getPath();

		final URL newAddress = new URL(protocol, host, port, path + (StringUtils.isNotBlank(address.getQuery()) ? "?" + address.getQuery() : ""));
		return newAddress;
	}
}