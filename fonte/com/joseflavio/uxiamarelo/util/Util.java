
/*
 *  Copyright (C) 2016-2018 José Flávio de Souza Dias Júnior
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
 *  Direitos Autorais Reservados (C) 2016-2018 José Flávio de Souza Dias Júnior
 * 
 *  Este arquivo é parte de Uxi-amarelo - <http://joseflavio.com/uxiamarelo/>.
 * 
 *  Uxi-amarelo é software livre: você pode redistribuí-lo e/ou modificá-lo
 *  sob os termos da Licença Pública Menos Geral GNU conforme publicada pela
 *  Free Software Foundation, tanto a versão 3 da Licença, como
 *  (a seu critério) qualquer versão posterior.
 * 
 *  Uxi-amarelo é distribuído na expectativa de que seja útil,
 *  porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 *  COMERCIABILIDADE ou ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a
 *  Licença Pública Menos Geral do GNU para mais detalhes.
 * 
 *  Você deve ter recebido uma cópia da Licença Pública Menos Geral do GNU
 *  junto com Uxi-amarelo. Se não, veja <http://www.gnu.org/licenses/>.
 */

package com.joseflavio.uxiamarelo.util;

import java.util.regex.Pattern;

import com.joseflavio.urucum.comunicacao.Mensagem.Tipo;
import com.joseflavio.urucum.comunicacao.Resposta;
import com.joseflavio.urucum.json.JSON;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.json.JSONTokener;

/**
 * @author José Flávio de Souza Dias Júnior
 */
public class Util {

	private static final Pattern padrao_url = Pattern.compile( ".{3,5}://.+" );
	
	/**
	 * Obtém uma {@link String} de um {@link JSON}, conforme {@code comando}.
	 * @param prefixo Prefixo do {@code comando}.
	 * @param comando Comando de obtenção.
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
	 * @param caminho Endereço da variável interna que receberá o {@code valor}. O último nó poderá ser um número inteiro qualquer indicando que a variável é um {@link JSONArray}.
	 * @param valor Valor a ser atribuído, podendo ser de qualquer tipo compatível com {@link JSON}.
	 * @param raiz {@link JSONObject} raiz.
	 * @return Último {@link JSONObject} da cadeia referente ao {@code caminho}.
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

	public static boolean isURL( String str ) {
		return padrao_url.matcher( str ).matches();
	}
	
	/**
	 * @see Resposta
	 * @see Tipo#ERRO
	 */
	public static JSON gerarRespostaErro( Throwable e ) {

		String classe   = e.getClass().getName();
		String mensagem = e.getMessage();

		return
		new JSON()
		.put( "exito", false )
		.put( "codigo", 999999 )
		.put( "mensagens", new JSONArray( new JSON[]{
			new JSON()
			.put( "tipo", Tipo.ERRO )
			.put( "argumento", classe + ": " + mensagem )
		}))
		.put( "classe", classe )
		.put( "mensagem", mensagem );

	}

}
