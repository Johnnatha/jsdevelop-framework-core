package br.com.jsdev.framework.core.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Classe com finalidade de retornar objetos locale especificos. Quando o programa precisa
 * de um recurso como label, campos de relatorios e mensagens, estes recursos sao obtidos atraves 
 * desta classe. 
 *  
 * 
 * */
public class Translate {

	private static ResourceBundle bundle = ResourceBundle.getBundle("bundle_ptBR");
	
	public static String get(final String key){
		return bundle.getString(key);
	}
	
	public static String get(final String key, final String argument){
		return Translate.get(key, new String[]{argument});
	}
	
	public static String get(final String key, final String ... arguments){
		MessageFormat temp = new MessageFormat(Translate.get(key));
        return temp.format(arguments);
	}
}