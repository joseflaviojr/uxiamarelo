
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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.joseflavio.unhadegato.UnhaDeGato;
import com.joseflavio.uxiamarelo.rest.UxiAmarelo;

/**
 * @author José Flávio de Souza Dias Júnior
 */
public class Configuracao {
	
	private static String  endereco     = "localhost";
	private static int     porta        = 8885;
	private static String  diretorio    = "uxiamarelo";
	private static String  diretorioURL = "uxiamarelo";
	private static String  arquivoNome  = "uuid";
	private static boolean cookieEnviar = true;
	
	static {
		try( InputStream is = UxiAmarelo.class.getResourceAsStream( "/uxiamarelo.conf" ) ){
			Properties p = new Properties();	
			p.load( is );
			endereco     = p.getProperty( "unhadegato.endereco", "localhost" );
			porta        = Integer.parseInt( p.getProperty( "unhadegato.porta", "8885" ) );
			diretorio    = p.getProperty( "diretorio", "uxiamarelo" );
			diretorioURL = p.getProperty( "diretorio.url", "uxiamarelo" );
			arquivoNome  = p.getProperty( "arquivo.nome", "uuid" );
			cookieEnviar = Boolean.parseBoolean( p.getProperty( "cookie.enviar", "true" ) );
		}catch( Exception e ){
		}
	}
	
	/**
	 * Endereço IP do {@link UnhaDeGato}.
	 */
	public static String getEndereco() {
		return endereco;
	}
	
	/**
	 * Porta TCP do {@link UnhaDeGato}.
	 */
	public static int getPorta() {
		return porta;
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
	 * Habilita o envio dos {@link javax.servlet.http.Cookie Cookies} como membros do JSON.
	 */
	public static boolean isCookieEnviar() {
		return cookieEnviar;
	}

}
