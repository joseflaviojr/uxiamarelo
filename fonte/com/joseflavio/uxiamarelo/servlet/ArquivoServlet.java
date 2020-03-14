
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

package com.joseflavio.uxiamarelo.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.joseflavio.copaiba.CopaibaConexao;
import com.joseflavio.urucum.arquivo.ArquivoUtil;
import com.joseflavio.urucum.json.JSON;
import com.joseflavio.urucum.texto.StringUtil;
import com.joseflavio.uxiamarelo.UxiAmarelo;
import com.joseflavio.uxiamarelo.util.Util;

import org.omnifaces.servlet.FileServlet;

/**
 * Download de {@link File arquivo}.
 * @author José Flávio de Souza Dias Júnior
 */
@WebServlet(name="arquivo", urlPatterns={"/arquivo/*", "/_/*"})
public class ArquivoServlet extends FileServlet {
    
    private static final long serialVersionUID = 1L;

    @EJB
    private UxiAmarelo uxiAmarelo;

    private static final String HTTP_CODIGO = "_uxi_amarelo_http_codigo_";
    
    @Override
    protected File getFile( HttpServletRequest requisicao ) {

        try{

            // Arquivo desejado

            String dirStr = uxiAmarelo.getDiretorioCompleto( requisicao );

            String arqStr = URLDecoder.decode(
                requisicao.getRequestURI().substring(
                    requisicao.getContextPath().length() +
                    requisicao.getServletPath().length()
                ),
                Util.CODIF
            );

            int tamanho = arqStr.length();

            if( tamanho <= 1 ){
                requisicao.setAttribute( HTTP_CODIGO, HttpServletResponse.SC_BAD_REQUEST );
                return null;
            }

            if( arqStr.contains( "../" ) ){
                requisicao.setAttribute( HTTP_CODIGO, HttpServletResponse.SC_FORBIDDEN );
                return null;
            }

            if( arqStr.charAt( tamanho - 1 ) == '/' ){
                arqStr = arqStr.substring( 0, tamanho - 1 );
            }

            String arqStrSO;

            if( File.separatorChar != '/' ){
                arqStrSO = arqStr.replace( '/', File.separatorChar );
            }else{
                arqStrSO = arqStr;
            }

            File arquivo = new File( dirStr + arqStrSO );

            if( ! arquivo.exists() ){
                requisicao.setAttribute( HTTP_CODIGO, HttpServletResponse.SC_NOT_FOUND );
                return null;
            }

            // Identificação do usuário

            String sid = requisicao.getParameter( "sid" );

            if( StringUtil.tamanho( sid ) == 0 && uxiAmarelo.isCookieEnviar() && ! uxiAmarelo.cookieBloqueado( "sid" ) ){
				Cookie[] cookies = requisicao.getCookies();
				if( cookies != null ){
					for( Cookie cookie : cookies ){
                        String nome = cookie.getName();
                        if( nome.equals( "sid" ) ){
                            sid = URLDecoder.decode( cookie.getValue(), Util.CODIF );
                            break;
                        }
					}
				}
			}

            // Autorização de acesso ao arquivo

            String autorizador = uxiAmarelo.getAutorizacaoCopaiba();

            if( ! autorizador.isEmpty() ){
                try( CopaibaConexao cc = uxiAmarelo.conectarCopaiba( autorizador ) ){
					String permitido = cc.solicitar(
                        uxiAmarelo.getAutorizacaoClasse(),
                        new JSON()
                            .put( "sid"    , sid    )
                            .put( "recurso", arqStr )
                            .toString(),
                        "executar"
                    );
					if( ! permitido.equals( "true" ) ){
                        requisicao.setAttribute( HTTP_CODIGO, HttpServletResponse.SC_FORBIDDEN );
                        return null;
                    }
				}
            }

            // Retornando arquivo autorizado

            return arquivo;
            
        }catch( IllegalArgumentException e ){
            throw e;
        }catch( Exception e ){
            requisicao.setAttribute( HTTP_CODIGO, HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            return null;
        }

    }

    @Override
    protected void handleFileNotFound( HttpServletRequest requisicao, HttpServletResponse resposta ) throws IOException {
        resposta.sendError( (int) requisicao.getAttribute( HTTP_CODIGO ) );
    }

    @Override
    protected String getContentType( HttpServletRequest requisicao, File arquivo ) {
        String tipo = ArquivoUtil.getTipo( arquivo );
        return tipo != null ? tipo : super.getContentType( requisicao, arquivo );
    }

    @Override
    protected boolean isAttachment( HttpServletRequest requisicao, String tipo ) {
        
        if( tipo == null ) return true;
        
        if( tipo.startsWith( "text/"  ) ) return false;
        if( tipo.startsWith( "image/" ) ) return false;
        if( tipo.startsWith( "audio/" ) ) return false;
        if( tipo.startsWith( "video/" ) ) return false;

        if( tipo.endsWith( "+xml"  ) ) return false;
        if( tipo.endsWith( "/pdf"  ) ) return false;
        if( tipo.endsWith( "/json" ) ) return false;

        return super.isAttachment( requisicao, tipo );

    }

}