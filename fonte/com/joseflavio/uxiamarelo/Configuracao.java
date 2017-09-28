
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

package com.joseflavio.uxiamarelo;

import com.joseflavio.unhadegato.UnhaDeGato;
import com.joseflavio.urucum.json.JSON;
import com.joseflavio.uxiamarelo.rest.UxiAmarelo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author José Flávio de Souza Dias Júnior
 */
public class Configuracao {
	
	private static String  endereco                 = "localhost";
	private static int     porta                    = 8885;
	private static boolean segura                   = false;
	private static boolean ignorarCertificado       = false;
	private static String  diretorio                = "uxiamarelo";
	private static String  diretorioURL             = "uxiamarelo";
	private static String  arquivoNome              = "uuid";
	private static boolean cookieEnviar             = true;
	private static String  cookieBloquear           = "sid, sessaoid, sessionid";
	private static boolean encapsulamentoAutomatico = true;
	private static String  encapsulamentoSeparador  = "__";
    
    private static Set<String> cookiesBloqueados;
	
	static {
	    
		try{
			
		    String dir  = System.getenv( "UXIAMARELO" );
			File   conf = dir != null ? new File( dir, "uxiamarelo.conf" ) : null;
			
			if( conf != null && conf.exists() ){
				try( InputStream is = new FileInputStream( conf ) ){
					carregar( is );
				}
			}else{
				try( InputStream is = UxiAmarelo.class.getResourceAsStream( "/uxiamarelo.conf" ) ){
					carregar( is );
				}
			}
			
		}catch( Exception e ){
			e.printStackTrace();
		}
		
	}
	
	private static void carregar( InputStream is ) throws IOException {
		
		Properties p = new Properties();
		p.load( is );
		
		endereco = p.getProperty( "unhadegato.endereco", "localhost" );
		porta = Integer.parseInt( p.getProperty( "unhadegato.porta", "8885" ) );
		segura = Boolean.parseBoolean( p.getProperty( "unhadegato.segura", "false" ) );
		ignorarCertificado = Boolean.parseBoolean( p.getProperty( "unhadegato.certificado.ignorar", "false" ) );
		diretorio = p.getProperty( "diretorio", "uxiamarelo" );
		diretorioURL = p.getProperty( "diretorio.url", "uxiamarelo" );
		arquivoNome = p.getProperty( "arquivo.nome", "uuid" );
		cookieEnviar = Boolean.parseBoolean( p.getProperty( "cookie.enviar", "true" ) );
		cookieBloquear = p.getProperty( "cookie.bloquear", "sid, sessaoid, sessionid" );
		encapsulamentoAutomatico = Boolean.parseBoolean( p.getProperty( "encapsulamento.automatico", "true" ) );
		encapsulamentoSeparador = p.getProperty( "encapsulamento.separador", "__" );
		
	}
	
	/**
	 * Endereço IP do {@link UnhaDeGato}.
	 * @see UnhaDeGato#UnhaDeGato(String, int, boolean, boolean)
	 */
	public static String getEndereco() {
		return endereco;
	}
	
	/**
	 * Porta TCP do {@link UnhaDeGato}.
	 * @see UnhaDeGato#UnhaDeGato(String, int, boolean, boolean)
	 */
	public static int getPorta() {
		return porta;
	}
	
	/**
	 * @see UnhaDeGato#UnhaDeGato(String, int, boolean, boolean)
	 */
	public static boolean isSegura() {
		return segura;
	}
	
	/**
	 * @see UnhaDeGato#UnhaDeGato(String, int, boolean, boolean)
	 */
	public static boolean isIgnorarCertificado() {
		return ignorarCertificado;
	}
	
	/**
	 * @see UnhaDeGato#UnhaDeGato(String, int, boolean, boolean)
	 */
	public static UnhaDeGato getUnhaDeGato() {
		return new UnhaDeGato( endereco, porta, segura, ignorarCertificado );
	}
	
	/**
	 * Endereço para depósito dos arquivos obtidos por upload.<br>
	 * Pode ser absoluto local ("/home/user/upload", "/tmp", etc.) ou relativo ao diretório raiz da aplicação web.
	 */
	public static String getDiretorio() {
		return diretorio;
	}
	
	/**
	 * {@link #getDiretorio()} na forma de {@link URL}, para acesso externo.<br>
	 * Pode ser absoluto prefixado ("http://", "file://", etc.) ou relativo à URL base da aplicação web.
	 */
	public static String getDiretorioURL() {
		return diretorioURL;
	}

	/**
	 * Padrão do {@link File#getName() nome} dos arquivos obtidos por upload: "uuid" ({@link java.util.UUID}) ou "original" (se possível).
	 */
	public static String getArquivoNome() {
		return arquivoNome;
	}

	/**
	 * Habilita o envio dos {@link javax.servlet.http.Cookie Cookies} como membros do {@link JSON}.
	 */
	public static boolean isCookieEnviar() {
		return cookieEnviar;
	}
    
    /**
     * {@link javax.servlet.http.Cookie Cookies} que não devem ser enviados como membros do {@link JSON}.
     */
    public static String getCookieBloquear() {
        return cookieBloquear;
    }
    
    /**
	 * Habilita o autoencapsulamento de valores em {@link JSONObject objetos} e {@link JSONArray arrays}.
	 */
	public static boolean isEncapsulamentoAutomatico() {
		return encapsulamentoAutomatico;
	}
	
	/**
	 * Separador de endereçamento de objetos no encapsulamento.
	 */
	public static String getEncapsulamentoSeparador() {
		return encapsulamentoSeparador;
	}
    
    public static boolean cookieBloqueado( String nome ) {
	    if( cookiesBloqueados == null ){
            cookiesBloqueados = new HashSet<>();
	        for( String s : cookieBloquear.split( "," ) ){
                cookiesBloqueados.add( s.trim() );
            }
        }
        return cookiesBloqueados.contains( nome );
    }
    
}
