
/*
 *  Copyright (C) 2016 José Flávio de Souza Dias Júnior
 *  
 *  This file is part of Uxi-amarelo - <http://www.joseflavio.com/uxiamarelo/>.
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
 *  Direitos Autorais Reservados (C) 2016 José Flávio de Souza Dias Júnior
 * 
 *  Este arquivo é parte de Uxi-amarelo - <http://www.joseflavio.com/uxiamarelo/>.
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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.joseflavio.unhadegato.UnhaDeGato;
import com.joseflavio.uxiamarelo.Configuracao;

/**
 * Interface {@link HttpServlet} para {@link UnhaDeGato#solicitar(String, String, String, String)}.
 * @author José Flávio de Souza Dias Júnior
 */
@WebServlet("/servlet/solicitar")
@MultipartConfig(
	fileSizeThreshold=0,
	maxFileSize=-1L,
	maxRequestSize=1024*1024*1024*1 //1GB
)
public class UxiAmarelo extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final Pattern padrao_url = Pattern.compile( ".{3,5}://.+" );

	@Override
	protected void doPost( HttpServletRequest requisicao, HttpServletResponse resposta ) throws ServletException, IOException {
		
		String tipo = requisicao.getContentType();
		if( tipo == null || tipo.isEmpty() ) tipo = "text/plain";
		
		String codificacao = requisicao.getCharacterEncoding();
		if( codificacao == null || codificacao.isEmpty() ) codificacao = "UTF-8";
		
        resposta.setCharacterEncoding( codificacao );
        PrintWriter saida = resposta.getWriter();
        
        String copaiba = null;
		
		try{
			
			ObjectNode json = new ObjectMapper().createObjectNode();
			
			Enumeration<String> parametros = requisicao.getParameterNames();
			
			while( parametros.hasMoreElements() ){
				String chave = parametros.nextElement();
				String valor = URLDecoder.decode( requisicao.getParameter( chave ), codificacao );
				if( chave.equals( "copaiba" ) ) copaiba = valor;
				else json.put( chave, valor );
			}
			
			if( tipo.contains( "multipart" ) ){
				
				Collection<Part> arquivos = requisicao.getParts();
				
				if( ! arquivos.isEmpty() ){
					
					String diretorioStr = Configuracao.getDiretorio();
					if( ! diretorioStr.startsWith( File.separator ) ){
						diretorioStr =
								requisicao.getServletContext().getRealPath("") +
								File.separator +
								diretorioStr;
					}
					
					File diretorio = new File( diretorioStr );
					if( ! diretorio.exists() ) diretorio.mkdirs();
					
					String url = Configuracao.getDiretorioURL();
					if( ! padrao_url.matcher( url ).matches() ){
						String url_esquema  = requisicao.getScheme();
						String url_servidor = requisicao.getServerName();
						int    url_porta    = requisicao.getServerPort();
						String url_contexto = requisicao.getContextPath();
						url =
								url_esquema + "://" + url_servidor + ":" + url_porta +
								url_contexto + "/" + url;
					}
					
					Map<String,List<String>> mapa_arquivos = new HashMap<>();
					
					for( Part arquivo : arquivos ){
						
						String chave = arquivo.getName();
						String nome = getNome( arquivo, codificacao );
						
						if( nome == null || nome.isEmpty() ){
							String valor = IOUtils.toString( arquivo.getInputStream(), codificacao );
							valor = URLDecoder.decode( valor, codificacao );
							if( chave.equals( "copaiba" ) ) copaiba = valor;
							else json.put( chave, valor );
							continue;
						}
						
						if( new File( diretorioStr + File.separator + nome ).exists() ){
							nome = UUID.randomUUID().toString() + "-" + nome;
						}
						
						arquivo.write( diretorioStr + File.separator + nome );
						
						List<String> lista = mapa_arquivos.get( chave );
						if( lista == null ){
							lista = new LinkedList<>();
							mapa_arquivos.put( chave, lista );
						}
						
						lista.add( url + "/" + nome );
						
					}
					
					for( String chave : mapa_arquivos.keySet() ){
						List<String> lista = mapa_arquivos.get( chave );
						if( lista.size() > 1 ){
							ArrayNode array = json.putArray( chave );
							for( String arquivo : lista ) array.add( arquivo );
						}else{
							json.put( chave, lista.get( 0 ) );
						}
					}
					
				}
				
			}
			
			if( copaiba == null || copaiba.isEmpty() ){
				throw new IllegalArgumentException( "Esperado copaiba = nome@classe@metodo" );
			}

			String resultado = "";
			String[] copaibaParams = copaiba.split( "@" );
			
			if( copaibaParams.length != 3 ){
				throw new IllegalArgumentException( "Esperado copaiba = nome@classe@metodo" );
			}
			
			try( UnhaDeGato udg = new UnhaDeGato( Configuracao.getEndereco(), Configuracao.getPorta() ) ){
				resultado = udg.solicitar( copaibaParams[0], copaibaParams[1], json.toString(), copaibaParams[2] );
			}
			
			resposta.setStatus( 200 );
			resposta.setContentType( "application/json" );
			
			saida.write( resultado );
			
		}catch( Exception e ){
			
			resposta.setStatus( 500 );
			resposta.setContentType( "application/json" );
			
			ObjectNode json = new ObjectMapper().createObjectNode();
			json.put( "classe", e.getClass().getName() );
			json.put( "mensagem", e.getMessage() );
			
			saida.write( json.toString() );
			
		}

        saida.flush();
		
	}
	
	@Override
	protected void doGet( HttpServletRequest requisicao, HttpServletResponse resposta ) throws ServletException, IOException {
		doPost( requisicao, resposta );
	}
	
	/**
	 * @see Part#getSubmittedFileName()
	 */
	private static String getNome( Part arquivo, String codificacao ) throws UnsupportedEncodingException {
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
			nome = URLDecoder.decode( nome, codificacao );
			char sep = nome.indexOf( '/' ) >= 0 ? '/' : nome.indexOf( '\\' ) >= 0 ? '\\' : '#';
			if( sep != '#' ){
				nome = nome.substring( nome.lastIndexOf( sep ) + 1 );
			}
		}
		return nome;
	}
	
}
