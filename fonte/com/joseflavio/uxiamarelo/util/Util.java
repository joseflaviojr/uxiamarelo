
/*
 *  Copyright (C) 2016-2018 Jos� Fl�vio de Souza Dias J�nior
 *  
 *  This file is part of Uxi-amarelo - <http://joseflavio.com/uxiamarelo/>.
 *  
 *  Uxi-amarelo is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Uxi-amarelo is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Uxi-amarelo. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *  Direitos Autorais Reservados (C) 2016-2018 Jos� Fl�vio de Souza Dias J�nior
 * 
 *  Este arquivo � parte de Uxi-amarelo - <http://joseflavio.com/uxiamarelo/>.
 * 
 *  Uxi-amarelo � software livre: voc� pode redistribu�-lo e/ou modific�-lo
 *  sob os termos da Licen�a P�blica Menos Geral GNU conforme publicada pela
 *  Free Software Foundation, tanto a vers�o 3 da Licen�a, como
 *  (a seu crit�rio) qualquer vers�o posterior.
 * 
 *  Uxi-amarelo � distribu�do na expectativa de que seja �til,
 *  por�m, SEM NENHUMA GARANTIA; nem mesmo a garantia impl�cita de
 *  COMERCIABILIDADE ou ADEQUA��O A UMA FINALIDADE ESPEC�FICA. Consulte a
 *  Licen�a P�blica Menos Geral do GNU para mais detalhes.
 * 
 *  Voc� deve ter recebido uma c�pia da Licen�a P�blica Menos Geral do GNU
 *  junto com Uxi-amarelo. Se n�o, veja <http://www.gnu.org/licenses/>.
 */

package com.joseflavio.uxiamarelo.util;

import com.joseflavio.urucum.json.JSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.json.JSONTokener;

/**
 * @author Jos� Fl�vio de Souza Dias J�nior
 */
public class Util {
	
	/**
	 * Obt�m uma {@link String} de um {@link JSON}, conforme {@code comando}.
	 * @param prefixo Prefixo do {@code comando}.
	 * @param comando Comando de obten��o.
	 * @param json {@link JSONObject} ou {@link JSONString}.
	 */
	public static String obterStringDeJSON( String prefixo, String comando, String json ) {
		String inicio = prefixo + ".json.";
		String atributo = comando.startsWith( inicio ) ? comando.substring( inicio.length() ) : null;
		if( atributo != null ) return new JSON( json ).getString( atributo );
		else return new JSONTokener( json ).nextValue().toString();
	}
	
	/**
	 * Encapsula um {@code valor} num determinado {@code caminho} interno de um {@link JSONObject objeto} {@code raiz}.
	 * @param caminho Endere�o da vari�vel interna que receber� o {@code valor}. O �ltimo n� poder� ser um n�mero inteiro qualquer indicando que a vari�vel � um {@link JSONArray}.
	 * @param valor Valor a ser atribu�do, podendo ser de qualquer tipo compat�vel com {@link JSON}.
	 * @param raiz {@link JSONObject} raiz.
	 * @return �ltimo {@link JSONObject} da cadeia referente ao {@code caminho}.
	 */
	public static JSONObject encapsular( String[] caminho, Object valor, JSONObject raiz ) {
		
		if( caminho.length == 1 ){
			
			raiz.put( caminho[0], valor );
			return raiz;
			
		}else if( caminho.length == 2 ){
			
			try{
				
				Integer.parseInt( caminho[1] );
				
				JSONArray array = raiz.optJSONArray( caminho[0] );
				if( array == null ) raiz.put( caminho[0], array = new JSONArray() );
				
				array.put( valor );
				return raiz;
				
			}catch( NumberFormatException e ){
			}
			
		}
		
		JSONObject objeto = raiz.optJSONObject( caminho[0] );
		if( objeto == null ) raiz.put( caminho[0], objeto = new JSON() );
		
		String[] novo = new String[ caminho.length - 1 ];
		System.arraycopy( caminho, 1, novo, 0, novo.length );
		
		return encapsular( novo, valor, objeto );
		
	}
	
}
