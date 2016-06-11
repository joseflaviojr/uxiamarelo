
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

package com.joseflavio.uxiamarelo.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.joseflavio.unhadegato.UnhaDeGato;
import com.joseflavio.uxiamarelo.Configuracao;

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
		try( UnhaDeGato udg = new UnhaDeGato( Configuracao.getEndereco(), Configuracao.getPorta() ) ){
			String retorno = udg.executar( copaiba, linguagem, rotina );
			return respostaOK( retorno );
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
		try( UnhaDeGato udg = new UnhaDeGato( Configuracao.getEndereco(), Configuracao.getPorta() ) ){
			String retorno = udg.obter( copaiba, variavel );
			return respostaOK( retorno );
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
    		@PathParam("copaiba") String copaiba,
    		@PathParam("classe")  String classe,
    		@PathParam("metodo")  String metodo,
    		String json
    	) {
		try( UnhaDeGato udg = new UnhaDeGato( Configuracao.getEndereco(), Configuracao.getPorta() ) ){
			String retorno = udg.solicitar( copaiba, classe, json, metodo );
			return respostaOK( retorno );
		}catch( Exception e ){
			return respostaERRO( e );
		}
    }
	
	/**
	 * {@link UnhaDeGato#solicitar(String, String, String, String)}
	 */
	@GET
	@Path("{copaiba}/solicitar/{classe: [a-zA-Z][a-zA-Z0-9_$.]*}/{metodo: [a-zA-Z][a-zA-Z0-9_$]*}/{json}")
	@Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response solicitarGet(
    		@PathParam("copaiba") String copaiba,
    		@PathParam("classe")  String classe,
    		@PathParam("metodo")  String metodo,
    		@PathParam("json")    String json
    	) {
		return solicitar( copaiba, classe, metodo, json );
	}
	
	private Response respostaOK( String retorno ) {
		return Response
		.status( Status.OK )
		.type( MediaType.APPLICATION_JSON + "; charset=UTF-8" )
		.entity( retorno )
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
