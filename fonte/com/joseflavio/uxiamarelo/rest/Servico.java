
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

package com.joseflavio.uxiamarelo.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.joseflavio.unhadegato.UnhaDeGato;
import com.joseflavio.urucum.json.JSON;
import com.joseflavio.uxiamarelo.Configuracao;
import com.joseflavio.uxiamarelo.util.Util;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.Map;

/**
 * @author José Flávio de Souza Dias Júnior
 */
@Path("/")
public class Servico {
	
	@GET
	@Path("teste")
	@Produces(MediaType.TEXT_PLAIN + "; charset=UTF-8")
    public String teste() {
		return "Uxi-amarelo";
	}
	
	/**
	 * {@link UnhaDeGato#executar(String, String, String)}
	 */
	@POST
	@Path("{copaiba}/executar/{linguagem}")
	@Consumes(MediaType.TEXT_PLAIN)
    public Response executar(
    		@PathParam("copaiba") String copaiba,
    		@PathParam("linguagem") String linguagem,
    		String rotina
    	) {
		try( UnhaDeGato udg = Configuracao.getUnhaDeGato() ){
			String resultado = udg.executar( copaiba, linguagem, rotina );
			return respostaOK( resultado );
		}catch( Exception e ){
			return respostaERRO( e );
		}
    }
	
	/**
	 * {@link UnhaDeGato#executar(String, String, String)}
	 */
	@GET
	@Path("{copaiba}/executar/{linguagem}/{rotina}")
	@Consumes(MediaType.TEXT_PLAIN)
    public Response executarGet(
    		@PathParam("copaiba")   String copaiba,
    		@PathParam("linguagem") String linguagem,
    		@PathParam("rotina")    String rotina
    	) {
		return executar( copaiba, linguagem, rotina );
	}
	
	/**
	 * {@link UnhaDeGato#obter(String, String)}
	 */
	@GET
	@Path("{copaiba}/obter/{variavel: [a-zA-Z][a-zA-Z0-9_$]*}")
    public Response obter(
    		@PathParam("copaiba") String copaiba,
    		@PathParam("variavel") String variavel
    	) {
		try( UnhaDeGato udg = Configuracao.getUnhaDeGato() ){
			String resultado = udg.obter( copaiba, variavel );
			return respostaOK( resultado );
		}catch( Exception e ){
			return respostaERRO( e );
		}
    }
	
	/**
	 * {@link UnhaDeGato#solicitar(String, String, String, String)}
	 */
	@POST
	@Path("{copaiba}/solicitar/{classe: [a-zA-Z][a-zA-Z0-9_$.]*}/{metodo: [a-zA-Z][a-zA-Z0-9_$]*}")
	@Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response solicitar(
			@Context HttpHeaders cabecalho,
			@QueryParam("uxicmd") String comando,
    		@PathParam("copaiba") String copaiba,
    		@PathParam("classe")  String classe,
    		@PathParam("metodo")  String metodo,
    		String json
    	) {
		return solicitar0( cabecalho, comando, copaiba, classe, metodo, json );
    }
	
	/**
	 * {@link UnhaDeGato#solicitar(String, String, String, String)}
	 */
	@GET
	@Path("{copaiba}/solicitar/{classe: [a-zA-Z][a-zA-Z0-9_$.]*}/{metodo: [a-zA-Z][a-zA-Z0-9_$]*}/{json}")
	@Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response solicitarGet(
			@Context HttpHeaders cabecalho,
			@QueryParam("uxicmd") String comando,
    		@PathParam("copaiba") String copaiba,
    		@PathParam("classe")  String classe,
    		@PathParam("metodo")  String metodo,
    		@PathParam("json")    String json
    	) {
		return solicitar0( cabecalho, comando, copaiba, classe, metodo, json );
	}

	private Response solicitar0( HttpHeaders cabecalho, String comando, String copaiba, String classe, String metodo, String json ) {
		
		try{
		
			JSON objeto = null;
			
			if( Configuracao.isCookieEnviar() ){
				Map<String, Cookie> cookies = cabecalho.getCookies();
				if( cookies.size() > 0 ){
					if( objeto == null ) objeto = new JSON( json );
					for( Cookie cookie : cookies.values() ){
						String nome = cookie.getName();
						if( Configuracao.cookieBloqueado( nome ) ) continue;
						if( ! objeto.has( nome ) ){
							try{
								objeto.put( nome, URLDecoder.decode( cookie.getValue(), "UTF-8" ) );
							}catch( UnsupportedEncodingException e ){
								objeto.put( nome, cookie.getValue() );
							}
						}
					}
				}
			}
			
			if( Configuracao.isEncapsulamentoAutomatico() ){
				if( objeto == null ) objeto = new JSON( json );
				String separador = Configuracao.getEncapsulamentoSeparador();
				for( String chave : objeto.keySet().toArray( new String[0] ) ){
					String[] caminho = chave.split( separador );
					if( caminho.length > 1 ){
						Util.encapsular( caminho, objeto.remove( chave ), objeto );
					}
				}
			}
			
			if( objeto != null ) json = objeto.toString();
			
			String resultado;
			
			if( comando == null ){
				try( UnhaDeGato udg = Configuracao.getUnhaDeGato() ){
					resultado = udg.solicitar( copaiba, classe, json, metodo );
					if( resultado == null ) resultado = "";
				}
			}else if( comando.equals( "voltar" ) ){
				resultado = json;
				comando   = null;
			}else{
				resultado = "";
			}
			
			if( comando == null ){
				
				return respostaOK( resultado );
				
			}else if( comando.startsWith( "redirecionar" ) ){
				
				return Response
					.temporaryRedirect( new URI( Util.obterStringDeJSON( "redirecionar", comando, resultado ) ) )
					.build();

			}else if( comando.startsWith( "base64" ) ){
				
				String url = comando.substring( "base64.".length() );
				
				return Response
					.temporaryRedirect( new URI( url + Base64.getUrlEncoder().encodeToString( resultado.getBytes( "UTF-8" ) ) ) )
					.build();
				
			}else if( comando.startsWith( "html_url" ) ){
				
				HttpURLConnection con = (HttpURLConnection) new URL( Util.obterStringDeJSON( "html_url", comando, resultado ) ).openConnection();
				con.setRequestProperty( "User-Agent", "Uxi-amarelo" );
				
				if( con.getResponseCode() != HttpServletResponse.SC_OK ) throw new IOException( "HTTP = " + con.getResponseCode() );
				
				String conteudo = null;
				try( InputStream is = con.getInputStream() ){
					conteudo = IOUtils.toString( is );
				}
				
				con.disconnect();
				
				return Response
					.status( Status.OK )
					.type( MediaType.TEXT_HTML + "; charset=UTF-8" )
					.entity( conteudo )
					.build();
				
			}else if( comando.startsWith( "html" ) ){
				
				return Response
					.status( Status.OK )
					.type( MediaType.TEXT_HTML + "; charset=UTF-8" )
					.entity( Util.obterStringDeJSON( "html", comando, resultado ) )
					.build();
				
			}else{
				
				throw new IllegalArgumentException( comando );
				
			}
			
		}catch( Exception e ){
			return respostaERRO( e );
		}
		
	}
	
	private Response respostaOK( String resultado ) {
		return Response
			.status( Status.OK )
			.type( MediaType.APPLICATION_JSON + "; charset=UTF-8" )
			.entity( resultado )
			.build();
	}
	
	private Response respostaERRO( Throwable e ) {
		
		ObjectNode json = new ObjectMapper().createObjectNode();
		json.put( "classe", e.getClass().getName() );
		json.put( "mensagem", e.getMessage() );
		
		return Response
			.status( Status.INTERNAL_SERVER_ERROR )
			.type( MediaType.APPLICATION_JSON + "; charset=UTF-8" )
			.entity( json.toString() )
			.build();
		
	}
	
}
