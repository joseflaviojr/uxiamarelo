
/*
 *  Copyright (C) 2016-2019 José Flávio de Souza Dias Júnior
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
 *  Direitos Autorais Reservados (C) 2016-2019 José Flávio de Souza Dias Júnior
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

package com.joseflavio.uxiamarelo.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.joseflavio.copaiba.CopaibaConexao;
import com.joseflavio.urucum.json.JSON;
import com.joseflavio.urucum.texto.StringUtil;
import com.joseflavio.uxiamarelo.UxiAmarelo;
import com.joseflavio.uxiamarelo.util.Util;

import org.apache.commons.io.IOUtils;

/**
 * Fachada {@link HttpServlet} para {@link CopaibaConexao#solicitar(String, String, String)}.
 * @author José Flávio de Souza Dias Júnior
 */
@WebServlet({"/servlet/solicitar", "/solicitar"})
@MultipartConfig(
	fileSizeThreshold=0,
	maxFileSize=-1L,
	maxRequestSize=1024*1024*1024*1 //1GB
)
public class UxiAmareloServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@EJB
	private UxiAmarelo uxiAmarelo;

	@Override
	protected void doPost( HttpServletRequest requisicao, HttpServletResponse resposta ) throws ServletException, IOException {
		
		String tipo = requisicao.getContentType();
		if( tipo == null || tipo.isEmpty() ) tipo = "text/plain";

		String codif_str = requisicao.getCharacterEncoding();
		if( codif_str == null || codif_str.isEmpty() ) codif_str = Util.CODIF_STR;
		final Charset codif = Charset.forName( codif_str );
		
        resposta.setCharacterEncoding( codif_str );
        PrintWriter saida = resposta.getWriter();
        
		try{
			
			JSON json;

			if( tipo.contains( "json" ) ){
				json = new JSON( IOUtils.toString( requisicao.getInputStream(), codif ) );
			}else{
				json = new JSON();
			}
			
			Enumeration<String> parametros = requisicao.getParameterNames();
			
			while( parametros.hasMoreElements() ){
				String chave = parametros.nextElement();
				String valor = URLDecoder.decode( requisicao.getParameter( chave ), codif );
				json.put( chave, valor );
			}

			if( tipo.contains( "multipart" ) ){
				
				Collection<Part> arquivos = requisicao.getParts();
				
				if( ! arquivos.isEmpty() ){

					File dirFile = new File( uxiAmarelo.getDiretorioCompleto( requisicao ), "tmp" );
					if( ! dirFile.exists() ) dirFile.mkdirs();
					
					Map<String,List<JSON>> mapa_arquivos = new HashMap<>();
					
					for( Part arquivo : arquivos ){
						
						String chave = arquivo.getName();
						String nome_original = getNome( arquivo, codif );
						String nome = nome_original;

						if( nome == null || nome.isEmpty() ){
							try( InputStream is = arquivo.getInputStream() ){
								String valor = IOUtils.toString( is, codif );
								valor = URLDecoder.decode( valor, codif );
								json.put( chave, valor );
								continue;
							}
						}

						File arqFile = new File( dirFile, nome );

						while( arqFile.exists() ){
							nome = UUID.randomUUID().toString() + "~" + nome_original;
							arqFile = new File( dirFile, nome );
						}
						
						arquivo.write( arqFile.getAbsolutePath() );
						
						List<JSON> lista = mapa_arquivos.get( chave );

						if( lista == null ){
							lista = new LinkedList<>();
							mapa_arquivos.put( chave, lista );
						}
						
						lista.add(
							(JSON) new JSON()
							.put( "nome", nome_original )
							.put( "endereco", "/tmp/" + nome )
						);
						
					}
					
					for( Entry<String,List<JSON>> entrada : mapa_arquivos.entrySet() ){
						List<JSON> lista = entrada.getValue();
						if( lista.size() > 1 ){
							json.put( entrada.getKey(), lista );
						}else{
							json.put( entrada.getKey(), lista.get( 0 ) );
						}
					}
					
				}
				
			}
			
			if( uxiAmarelo.isCookieEnviar() ){
				Cookie[] cookies = requisicao.getCookies();
				if( cookies != null ){
					for( Cookie cookie : cookies ){
						String nome = cookie.getName();
						if( uxiAmarelo.cookieBloqueado( nome ) ) continue;
						if( ! json.has( nome ) ){
							json.put( nome, URLDecoder.decode( cookie.getValue(), Util.CODIF ) );
						}
					}
				}
			}
			
			if( uxiAmarelo.isEncapsulamentoAutomatico() ){
				final String sepstr = uxiAmarelo.getEncapsulamentoSeparador();
				final char   sep0   = sepstr.charAt(0);
				for( String chave : new HashSet<>( json.keySet() ) ){
					if( chave.indexOf( sep0 ) == -1 ) continue;
					String[] caminho = chave.split( sepstr );
					if( caminho.length > 1 ){
						Util.encapsular( caminho, json.remove( chave ), json );
					}
				}
			}

			String copaiba = (String) json.remove( "copaiba" );
			if( StringUtil.tamanho( copaiba ) == 0 ){
				throw new IllegalArgumentException( "copaiba = nome@classe@metodo" );
			}

			String[] copaibaParam = copaiba.split( "@" );
			if( copaibaParam.length != 3 ){
				throw new IllegalArgumentException( "copaiba = nome@classe@metodo" );
			}
			
			String comando = (String) json.remove( "uxicmd" );
			if( StringUtil.tamanho( comando ) == 0 ) comando = null;
			
			String resultado;

			if( comando != null && comando.equals( "voltar" ) ){
				resultado = json.toString();
				comando   = null;
			}else{
				try( CopaibaConexao cc = uxiAmarelo.conectarCopaiba( copaibaParam[0] ) ){
					resultado = cc.solicitar( copaibaParam[1], json.toString(), copaibaParam[2] );
					if( resultado == null ) resultado = "";
				}
			}
			
			if( comando == null ){
				
				resposta.setStatus( HttpServletResponse.SC_OK );
				resposta.setContentType( "application/json" );
				
				saida.write( resultado );
				
			}else if( comando.startsWith( "redirecionar" ) ){

				if( ! uxiAmarelo.comandoPermitido( "redirecionar" ) ){
					throw new SecurityException( "redirecionar" );
				}
				
				resposta.sendRedirect( Util.obterStringDeJSON( "redirecionar", comando, resultado ) );
				
			}else if( comando.startsWith( "base64" ) ){

				if( ! uxiAmarelo.comandoPermitido( "base64" ) ){
					throw new SecurityException( "base64" );
				}
				
				String url = comando.substring( "base64.".length() );
				
				resposta.sendRedirect( url + Base64.getUrlEncoder().encodeToString( resultado.getBytes( Util.CODIF ) ) );
				
			}else if( comando.startsWith( "html_url" ) ){

				if( ! uxiAmarelo.comandoPermitido( "html_url" ) ){
					throw new SecurityException( "html_url" );
				}
				
				HttpURLConnection con = (HttpURLConnection) new URL( Util.obterStringDeJSON( "html_url", comando, resultado ) ).openConnection();
				con.setRequestProperty( "User-Agent", "Uxi-amarelo" );
				
				if( con.getResponseCode() != HttpServletResponse.SC_OK ) throw new IOException( "HTTP = " + con.getResponseCode() );
				
				resposta.setStatus( HttpServletResponse.SC_OK );
				resposta.setContentType( "text/html" );
				
				try( InputStream is = con.getInputStream() ){
					saida.write( IOUtils.toString( is, Util.CODIF ) );
				}
				
				con.disconnect();
				
			}else if( comando.startsWith( "html" ) ){

				if( ! uxiAmarelo.comandoPermitido( "html" ) ){
					throw new SecurityException( "html" );
				}
				
				resposta.setStatus( HttpServletResponse.SC_OK );
				resposta.setContentType( "text/html" );
				
				saida.write( Util.obterStringDeJSON( "html", comando, resultado ) );
				
			}else{
				
				throw new IllegalArgumentException( comando );
				
			}
		
		}catch( SecurityException e ){
			enviarErro( e, resposta, HttpServletResponse.SC_FORBIDDEN, saida );
		}catch( Exception e ){
			enviarErro( e, resposta, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, saida );
		}

        saida.flush();
		
	}
	
	@Override
	protected void doGet( HttpServletRequest requisicao, HttpServletResponse resposta ) throws ServletException, IOException {
		doPost( requisicao, resposta );
	}

	private static void enviarErro( Throwable e, HttpServletResponse resposta, int status, PrintWriter saida ) {
		resposta.setStatus( status );
		resposta.setContentType( "application/json" );
		saida.write( Util.gerarRespostaErro( e ).toString() );
	}
	
	/**
	 * @see Part#getSubmittedFileName()
	 */
	private static String getNome( Part arquivo, Charset codif ) {
		String nome = null;
		String content_disposition = arquivo.getHeader( "content-disposition" );
		if( content_disposition == null ) return null;
		for( String p : content_disposition.split( ";" ) ){
			p = p.trim();
			if( p.startsWith( "filename" ) ){
				nome = p.substring( p.indexOf( "=" ) + 2, p.length() - 1 );
				break;
			}
		}
		if( nome != null ){
			nome = URLDecoder.decode( nome, codif );
			char sep = nome.indexOf( '/' ) >= 0 ? '/' : nome.indexOf( '\\' ) >= 0 ? '\\' : '#';
			if( sep != '#' ){
				nome = nome.substring( nome.lastIndexOf( sep ) + 1 );
			}
		}
		return nome;
	}
	
}
