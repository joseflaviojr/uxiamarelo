
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

package com.joseflavio.uxiamarelo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.servlet.http.HttpServletRequest;

import com.google.protobuf.ByteString;
import com.ibm.etcd.api.KeyValue;
import com.ibm.etcd.api.RangeResponse;
import com.ibm.etcd.client.EtcdClient;
import com.joseflavio.copaiba.CopaibaConexao;
import com.joseflavio.copaiba.CopaibaException;
import com.joseflavio.copaiba.Erro;
import com.joseflavio.urucum.json.JSON;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Uxi-amarelo: fachada Web para {@link com.joseflavio.copaiba.Copaiba Copaíba}s.
 * @author José Flávio de Souza Dias Júnior
 */
@Startup
@Singleton
@DependsOn("Registrador")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class UxiAmarelo {
	
	private String etcdEndereco;
	private int    etcdPorta;
	private String etcdUsuario;
	private String etcdSenha;

	private String                  copaibas;
	private Map<String,CopaibaConf> copaibasConf;

	private String autorizacaoCopaiba;
	private String autorizacaoClasse;
	
	private String diretorio;

	private boolean     cookieEnviar;
	private String      cookieBloquear;
    private Set<String> cookiesBloqueados;

	private boolean encapsulamentoAutomatico;
	private String  encapsulamentoSeparador;

	private String      comandoPermitir;
    private Set<String> comandosPermitidos;

	@EJB
	private Registrador reg;

	@Resource
	private TimerService timerService;

	private final ReentrantLock lock_atualizacao = new ReentrantLock();
	private final ReentrantLock lock_limpeza     = new ReentrantLock();
	
	@PostConstruct
	public void inicializar() {
		TimerConfig tc = new TimerConfig();
		tc.setPersistent( false );
		timerService.createIntervalTimer( 0, 5 * 60 * 1000, tc ); // 5 minutos
	}

	@Timeout
	public void atualizar() {

		// Carregando propriedades

		lock_atualizacao.lock();
		
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
			
			reg.getLog().log( Level.SEVERE, e.getMessage(), e );

		}finally{

			lock_atualizacao.unlock();

		}

		// Apagando arquivos temporários

		lock_limpeza.lock();

		try{
			
			limparTemporario();

		}catch( Exception e ){

			reg.getLog().log( Level.SEVERE, e.getMessage(), e );

		}finally{

			lock_limpeza.unlock();

		}

	}
	
	private void carregar( InputStream is ) throws IOException {
		
		Properties props = new Properties();
		props.load( is );

		// Propriedades básicas ---------------------------------------------------------

		synchronized( this ){

			etcdEndereco = props.getProperty( "etcd.endereco", "etcd-host" );
			etcdPorta    = Integer.parseInt( props.getProperty( "etcd.porta", "2379" ) );
			etcdUsuario  = props.getProperty( "etcd.usuario", "" );
			etcdSenha    = props.getProperty( "etcd.senha", "" );
			
			copaibas = props.getProperty( "copaibas", "" );
			
			autorizacaoCopaiba = props.getProperty( "autorizacao.copaiba", "" );
			autorizacaoClasse  = props.getProperty( "autorizacao.classe" , "" );
			
			diretorio = props.getProperty( "diretorio", "uxiamarelo" );
			
			if( diretorio.endsWith( File.separator ) ){
				diretorio = diretorio.substring( 0, diretorio.length() - 1 );
			}
			
			cookieEnviar   = Boolean.parseBoolean( props.getProperty( "cookie.enviar", "true" ) );
			cookieBloquear = props.getProperty( "cookie.bloquear", "sid, sessaoid, sessionid" );
			
			cookiesBloqueados = new HashSet<>();
	        for( String s : cookieBloquear.split( "," ) ){
                cookiesBloqueados.add( s.trim() );
            }
			
			encapsulamentoAutomatico = Boolean.parseBoolean( props.getProperty( "encapsulamento.automatico", "true" ) );
			encapsulamentoSeparador  = props.getProperty( "encapsulamento.separador", "__" );

			comandoPermitir = props.getProperty( "comando.permitir", "redirecionar, base64" );

			comandosPermitidos = new HashSet<>();
	        for( String s : comandoPermitir.split( "," ) ){
                comandosPermitidos.add( s.trim() );
            }
			
		}

		// Obtendo as propriedades das Copaíbas -----------------------------------------

		Map<String,CopaibaConf> ccMap = new HashMap<>();

		EtcdClient.Builder etcdBuilder = EtcdClient.forEndpoint( etcdEndereco, etcdPorta ).withPlainText();
		if( etcdUsuario.length() > 0 ) etcdBuilder.withCredentials( etcdUsuario, etcdSenha );
		
		try( EtcdClient etcd = etcdBuilder.build() ){

			for( String espec : copaibas.split( "," ) ){
				
				CopaibaConf cc = new CopaibaConf();
	
				String[] partes  = espec.split( "@" );
				String   id      = partes[0].trim();
				String   prefixo = partes[1].trim();
				
				RangeResponse etcdResp = etcd
					.getKvClient()
					.get( ByteString.copyFromUtf8( prefixo + "." ) )
					.asPrefix()
					.sync();
			
				for( KeyValue kv : etcdResp.getKvsList() ){
					
					String chave = kv.getKey().toStringUtf8();
					String valor = kv.getValue().toStringUtf8();
	
					if(      chave.endsWith( ".Endereco"           ) ) cc.endereco           = valor;
					else if( chave.endsWith( ".Porta"              ) ) cc.porta              = Integer.parseInt( valor );
					else if( chave.endsWith( ".Segura"             ) ) cc.segura             = Boolean.parseBoolean( valor );
					else if( chave.endsWith( ".IgnorarCertificado" ) ) cc.ignorarCertificado = Boolean.parseBoolean( valor );
					else if( chave.endsWith( ".Usuario"            ) ) cc.usuario            = valor;
					else if( chave.endsWith( ".Senha"              ) ) cc.senha              = valor;
					else if( chave.endsWith( ".Expressa"           ) ) cc.expressa           = Boolean.parseBoolean( valor );
	
				}
	
				ccMap.put( id, cc );
		
			}
	
			synchronized( this ){
				this.copaibasConf = ccMap;
			}

		}

	}

	/**
	 * {@link Files#delete(Path) Apaga} os {@link File arquivos} temporários,
	 * subidos através de {@link com.joseflavio.uxiamarelo.servlet.UxiAmareloServlet},
	 * e que não foram movidos para diretórios de persistência.
	 * Prazo máximo de permanência do arquivo temporário: 1 hora, em relação
	 * à sua data de modificação.
	 */
	private void limparTemporario() throws IOException {

		final Path tmp = Paths.get( getDiretorio(), "tmp" );
		if( ! tmp.isAbsolute() || ! Files.exists( tmp ) ) return;

		final Path systmp = Paths.get( System.getProperty( "java.io.tmpdir" ) );
		if( tmp.equals( systmp ) ) return;

		final long ctm = System.currentTimeMillis();

		try( DirectoryStream<Path> ds = Files.newDirectoryStream(tmp) ) {
			for( Path p : ds ){
				if( Files.isDirectory( p ) ) continue;
				long lmt = Files.getLastModifiedTime( p ).toMillis();
				if( ( ctm - lmt ) >= ( 1 * 60 * 60 * 1000 ) ){ // 1 hora
					Files.delete( p );
				}
			}
		}

	}

	/**
	 * Conecta a uma {@link com.joseflavio.copaiba.Copaiba Copaíba}.
	 * @param id Identificação da {@link com.joseflavio.copaiba.Copaiba Copaíba} desejada.
	 * @see CopaibaConexao#CopaibaConexao(String, int, boolean, boolean, boolean)
	 */
	public CopaibaConexao conectarCopaiba( String id ) throws CopaibaException {
		
		CopaibaConf cc;

		synchronized( this ){
			cc = copaibasConf.get( id );
		}

		if( cc == null ){
			throw new CopaibaException( Erro.ARGUMENTO_INVALIDO, id );
		}else if( cc.expressa ){
			return new CopaibaConexao( cc.endereco, cc.porta, cc.segura, cc.ignorarCertificado, cc.expressa );
		}else{
			return new CopaibaConexao( cc.endereco, cc.porta, cc.segura, cc.ignorarCertificado, cc.usuario, cc.senha );
		}

	}

	/**
	 * Nome da {@link CopaibaConexao Copaíba} do sistema de controle de acesso a recursos.
	 */
	public synchronized String getAutorizacaoCopaiba() {
		return autorizacaoCopaiba;
	}

	/**
	 * Nome da classe-serviço do sistema de controle de acesso a recursos.
	 */
	public synchronized String getAutorizacaoClasse() {
		return autorizacaoClasse;
	}

	/**
	 * Repositório de arquivos.<br>
	 * Endereço local completo ("/home/user/upload") ou relativo ao diretório raiz da aplicação web.
	 */
	private synchronized String getDiretorio() {
		return diretorio;
	}

	/**
	 * {@link #getDiretorio()} com informação completa de endereço.
	 */
	public synchronized String getDiretorioCompleto( HttpServletRequest requisicao ) {

		String dirStr = diretorio;

		if( dirStr.charAt( 0 ) != File.separatorChar ){
			String real = requisicao.getServletContext().getRealPath("");
			dirStr =
				real +
				( real.charAt( real.length() - 1 ) == File.separatorChar ? "" : File.separatorChar ) +
				dirStr;
		}

		return dirStr;

	}
	
	/**
	 * Habilita o envio dos {@link javax.servlet.http.Cookie Cookies} como membros do {@link JSON}.
	 */
	public synchronized boolean isCookieEnviar() {
		return cookieEnviar;
	}
    
    /**
	 * Habilita o autoencapsulamento de valores em {@link JSONObject objetos} e {@link JSONArray arrays}.
	 */
	public synchronized boolean isEncapsulamentoAutomatico() {
		return encapsulamentoAutomatico;
	}
	
	/**
	 * Separador de endereçamento de objetos no encapsulamento.
	 */
	public synchronized String getEncapsulamentoSeparador() {
		return encapsulamentoSeparador;
	}
	
	/**
	 * Verifica se um determinado {@link javax.servlet.http.Cookie Cookie} não deve ser enviado como membro do {@link JSON}.
	 */
    public synchronized boolean cookieBloqueado( String nome ) {
        return cookiesBloqueados.contains( nome );
	}

	/**
	 * Verifica se um comando (uxicmd) está permitido.
	 */
    public synchronized boolean comandoPermitido( String nome ) {
        return comandosPermitidos.contains( nome );
	}
	
	/**
	 * Configuração necessária para se conectar a uma {@link com.joseflavio.copaiba.Copaiba Copaíba}.
	 */
	public static class CopaibaConf {

		private String  endereco           = "localhost";
		private int     porta              = 8884;
		private boolean segura             = false;
		private boolean ignorarCertificado = false;
		private String  usuario            = "";
		private String  senha              = "";
		private boolean expressa           = true;

		public String getEndereco() {
			return endereco;
		}

		public int getPorta() {
			return porta;
		}

		public boolean isSegura() {
			return segura;
		}

		public boolean isIgnorarCertificado() {
			return ignorarCertificado;
		}

		public String getUsuario() {
			return usuario;
		}

		public String getSenha() {
			return senha;
		}

		public boolean isExpressa() {
			return expressa;
		}

	}
    
}
