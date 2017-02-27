
/*
 *  Copyright (C) 2016 Jos� Fl�vio de Souza Dias J�nior
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
 *  Direitos Autorais Reservados (C) 2016 Jos� Fl�vio de Souza Dias J�nior
 * 
 *  Este arquivo � parte de Uxi-amarelo - <http://www.joseflavio.com/uxiamarelo/>.
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

package com.joseflavio.uxiamarelo;

import com.joseflavio.unhadegato.UnhaDeGato;
import com.joseflavio.uxiamarelo.rest.UxiAmarelo;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author Jos� Fl�vio de Souza Dias J�nior
 */
public class Configuracao {
	
	private static String  endereco           = "localhost";
	private static int     porta              = 8885;
	private static boolean segura             = false;
	private static boolean ignorarCertificado = false;
	private static String  diretorio          = "uxiamarelo";
	private static String  diretorioURL       = "uxiamarelo";
	private static String  arquivoNome        = "uuid";
	private static boolean cookieEnviar       = true;
	
	static {
		try( InputStream is = UxiAmarelo.class.getResourceAsStream( "/uxiamarelo.conf" ) ){
			Properties p = new Properties();	
			p.load( is );
			endereco           = p.getProperty( "unhadegato.endereco", "localhost" );
			porta              = Integer.parseInt( p.getProperty( "unhadegato.porta", "8885" ) );
			segura             = Boolean.parseBoolean( p.getProperty( "unhadegato.segura", "false" ) );
			ignorarCertificado = Boolean.parseBoolean( p.getProperty( "unhadegato.certificado.ignorar", "false" ) );
			diretorio          = p.getProperty( "diretorio", "uxiamarelo" );
			diretorioURL       = p.getProperty( "diretorio.url", "uxiamarelo" );
			arquivoNome        = p.getProperty( "arquivo.nome", "uuid" );
			cookieEnviar       = Boolean.parseBoolean( p.getProperty( "cookie.enviar", "true" ) );
		}catch( Exception e ){
		}
	}
	
	/**
	 * Endere�o IP do {@link UnhaDeGato}.
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
	 * Endere�o para dep�sito dos arquivos obtidos por upload.<br>
	 * Pode ser absoluto local ("/home/user/upload", "/tmp", etc.) ou relativo ao diret�rio raiz da aplica��o web.
	 */
	public static String getDiretorio() {
		return diretorio;
	}
	
	/**
	 * {@link #getDiretorio()} na forma de {@link URL}, para acesso externo.<br>
	 * Pode ser absoluto prefixado ("http://", "file://", etc.) ou relativo � URL base da aplica��o web.
	 */
	public static String getDiretorioURL() {
		return diretorioURL;
	}

	/**
	 * Padr�o do {@link File#getName() nome} dos arquivos obtidos por upload: "uuid" ({@link java.util.UUID}) ou "original" (se poss�vel).
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
