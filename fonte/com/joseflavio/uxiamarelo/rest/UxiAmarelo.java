
/*
 *  Copyright (C) 2016-2018 Jos� Fl�vio de Souza Dias J�nior
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
 *  Direitos Autorais Reservados (C) 2016-2018 Jos� Fl�vio de Souza Dias J�nior
 * 
 *  Este arquivo � parte de Uxi-amarelo - <http://joseflavio.com/uxiamarelo/>.
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

package com.joseflavio.uxiamarelo.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.joseflavio.unhadegato.UnhaDeGato;

/**
 * Interface {@link Application REST} para {@link UnhaDeGato}.
 * @author Jos� Fl�vio de Souza Dias J�nior
 */
@ApplicationPath("/rest")
public class UxiAmarelo extends Application {
	
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<>();
		classes.add( Servico.class );
		return classes;
	}
	
	@Override
	public Set<Object> getSingletons() {
		return Collections.emptySet();
	}
	
	@Override
	public Map<String,Object> getProperties() {
		return new HashMap<>();
	}

}
