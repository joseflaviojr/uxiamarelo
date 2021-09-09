
/*
 *  Copyright (C) 2016-2020 José Flávio de Souza Dias Júnior
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
 *  Direitos Autorais Reservados (C) 2016-2020 José Flávio de Souza Dias Júnior
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.joseflavio.copaiba.CopaibaConexao;
import com.joseflavio.urucum.json.JSON;
import com.joseflavio.uxiamarelo.UxiAmarelo;
import com.joseflavio.uxiamarelo.util.Util;

import org.apache.commons.io.IOUtils;

/**
 * Fachada REST para {@link com.joseflavio.copaiba.Copaiba Copaíba}s.
 * @author José Flávio de Souza Dias Júnior
 */
@Path("/")
public class UxiAmareloREST {

	@EJB
	private UxiAmarelo uxiAmarelo;

	@GET
	@Path("teste")
	@Produces(MediaType.TEXT_PLAIN + "; charset=" + Util.CODIF_STR)
    public String teste() {
		return "Uxi-amarelo";
	}
	
	/**
	 * {@link CopaibaConexao#executar(String, String, java.io.Writer, boolean)}
	 */
	@POST
	@Path("{copaiba}/executar/{linguagem}")
	@Consumes(MediaType.TEXT_PLAIN)
    public Response executar(
    		@PathParam("copaiba")   String copaiba,
    		@PathParam("linguagem") String linguagem,
    		String rotina
    	) {
		try( CopaibaConexao cc = uxiAmarelo.conectarCopaiba( copaiba ) ){
			String resultado = (String) cc.executar( linguagem, rotina, null, true );
			return respostaEXITO( resultado );
		}catch( Exception e ){
			return respostaERRO( e, Status.INTERNAL_SERVER_ERROR );
		}
    }
	
	/**
	 * {@link CopaibaConexao#executar(String, String, java.io.Writer, boolean)}
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
	 * {@link CopaibaConexao#obter(String, boolean)}
	 */
	@GET
	@Path("{copaiba}/obter/{variavel: [a-zA-Z][a-zA-Z0-9_$]*}")
    public Response obter(
    		@PathParam("copaiba")  String copaiba,
    		@PathParam("variavel") String variavel
    	) {
		try( CopaibaConexao cc = uxiAmarelo.conectarCopaiba( copaiba ) ){
			String resultado = (String) cc.obter( variavel, true );
			return respostaEXITO( resultado );
		}catch( Exception e ){
			return respostaERRO( e, Status.INTERNAL_SERVER_ERROR );
		}
    }
	
	/**
	 * {@link CopaibaConexao#solicitar(String, String, String)}
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
	 * {@link CopaibaConexao#solicitar(String, String, String)}
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

	private Response solicitar0( HttpHeaders cabecalho, String comando, String copaiba, String classe, String metodo, String jsonStr ) {
		
		try{
		
			JSON json = null;
			
			if( uxiAmarelo.isCookieEnviar() ){
				Map<String, Cookie> cookies = cabecalho.getCookies();
				if( cookies.size() > 0 ){
					if( json == null ) json = new JSON( jsonStr );
					for( Cookie cookie : cookies.values() ){
						String nome = cookie.getName();
						if( uxiAmarelo.cookieBloqueado( nome ) ) continue;
						if( ! json.has( nome ) ){
							json.put( nome, URLDecoder.decode( cookie.getValue(), Util.CODIF ) );
						}
					}
				}
			}
			
			if( uxiAmarelo.isEncapsulamentoAutomatico() ){
				if( json == null ) json = new JSON( jsonStr );
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
			
			if( json != null ) jsonStr = json.toString();
			
			String resultado;
			
			if( comando != null && comando.equals( "voltar" ) ){
				resultado = jsonStr;
				comando   = null;
			}else{
				try( CopaibaConexao cc = uxiAmarelo.conectarCopaiba( copaiba ) ){
					resultado = cc.solicitar( classe, jsonStr, metodo );
					if( resultado == null ) resultado = "";
				}
			}
			
			if( comando == null ){
				
				return respostaEXITO( resultado );
				
			}else if( comando.startsWith( "redirecionar" ) ){

				if( ! uxiAmarelo.comandoPermitido( "redirecionar" ) ){
					throw new SecurityException( "redirecionar" );
				}
				
				return Response
					.temporaryRedirect( new URI( Util.obterStringDeJSON( "redirecionar", comando, resultado ) ) )
					.build();

			}else if( comando.startsWith( "base64" ) ){

				if( ! uxiAmarelo.comandoPermitido( "base64" ) ){
					throw new SecurityException( "base64" );
				}
				
				String url = comando.substring( "base64.".length() );
				
				return Response
					.temporaryRedirect( new URI( url + Base64.getUrlEncoder().encodeToString( resultado.getBytes( Util.CODIF ) ) ) )
					.build();
				
			}else if( comando.startsWith( "html_url" ) ){

				if( ! uxiAmarelo.comandoPermitido( "html_url" ) ){
					throw new SecurityException( "html_url" );
				}
				
				HttpURLConnection con = (HttpURLConnection) new URL( Util.obterStringDeJSON( "html_url", comando, resultado ) ).openConnection();

				try{

					con.setRequestProperty( "User-Agent", "Uxi-amarelo" );
					con.setRequestProperty( "Accept-Charset", Util.CODIF_STR );
					
					if( con.getResponseCode() != HttpServletResponse.SC_OK ) throw new IOException( "HTTP = " + con.getResponseCode() );
					
					String conteudo = null;
					try( InputStream is = con.getInputStream() ){
						conteudo = IOUtils.toString( is, Util.CODIF );
					}
					
					return Response
						.status( Status.OK )
						.type( MediaType.TEXT_HTML + "; charset=" + Util.CODIF_STR )
						.entity( conteudo )
						.build();

				}finally{

					if( con != null ) con.disconnect();
					
				}
				
			}else if( comando.startsWith( "html" ) ){

				if( ! uxiAmarelo.comandoPermitido( "html" ) ){
					throw new SecurityException( "html" );
				}
				
				return Response
					.status( Status.OK )
					.type( MediaType.TEXT_HTML + "; charset=" + Util.CODIF_STR )
					.entity( Util.obterStringDeJSON( "html", comando, resultado ) )
					.build();
				
			}else{
				
				throw new IllegalArgumentException( comando );
				
			}
		
		}catch( SecurityException e ){
			return respostaERRO( e, Status.FORBIDDEN );
		}catch( Exception e ){
			return respostaERRO( e, Status.INTERNAL_SERVER_ERROR );
		}
		
	}
	
	private Response respostaEXITO( String resultado ) {
		return Response
			.status( Status.OK )
			.type( MediaType.APPLICATION_JSON + "; charset=" + Util.CODIF_STR )
			.entity( resultado )
			.build();
	}
	
	private Response respostaERRO( Throwable e, Status status ) {
		return Response
			.status( status )
			.type( MediaType.APPLICATION_JSON + "; charset=" + Util.CODIF_STR )
			.entity( Util.gerarRespostaErro( e ).toString() )
			.build();
	}
	
}
