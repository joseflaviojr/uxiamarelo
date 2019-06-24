# Uxi-amarelo

Fachada Web para [Copaíba](http://joseflavio.com/copaiba)s.

Web facade for [Copaíba](http://joseflavio.com/copaiba)s.

## Descrição

A Uxi-amarelo é uma aplicação Web que permite a comunicação com [Copaíba](http://joseflavio.com/copaiba)s através de protocolos comuns, como HTTP/S e REST.

A figura a seguir ilustra o esquema geral do funcionamento da Uxi-amarelo:

<img src="projeto/EsquemaGeral.png">

## Versão Atual

1.0-A13

Padrão de versionamento: [JFV](http://joseflavio.com/jfv)

## Instalação

Para implantar a Uxi-amarelo, basta instalar o arquivo [uxiamarelo.war](https://github.com/joseflaviojr/uxiamarelo/releases/download/1.0-A13/uxiamarelo.war) num servidor Web que implemente a especificação JavaEE, como o [Apache TomEE](http://tomee.apache.org/).

### Requisitos para instalação

* Java >= 1.8
* Apache TomEE Plus >= 1.7.5 com JDK 8

## Desenvolvimento

Configuração do projeto para Eclipse e IntelliJ IDEA:

```sh
gradle eclipse
gradle cleanIdea idea
```

### Requisitos para desenvolvimento

* Git >= 2.8
* Java >= 1.8
* Gradle >= 4.7

## Compilação

Para compilar o projeto, gerando o arquivo WAR, executar no terminal:

```sh
gradle clean build
```

## Licença

### Português

Direitos Autorais Reservados &copy; 2016-2018 [José Flávio de Souza Dias Júnior](http://joseflavio.com)

Este arquivo é parte de Uxi-amarelo - [http://joseflavio.com/uxiamarelo](http://joseflavio.com/uxiamarelo).

Uxi-amarelo é software livre: você pode redistribuí-lo e/ou modificá-lo
sob os termos da [Licença Pública Menos Geral GNU](https://www.gnu.org/licenses/lgpl.html) conforme publicada pela
Free Software Foundation, tanto a versão 3 da Licença, como
(a seu critério) qualquer versão posterior.

Uxi-amarelo é distribuído na expectativa de que seja útil,
porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
COMERCIABILIDADE ou ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a
Licença Pública Menos Geral do GNU para mais detalhes.

Você deve ter recebido uma cópia da Licença Pública Menos Geral do GNU
junto com Uxi-amarelo. Se não, veja [https://www.gnu.org/licenses/lgpl.html](https://www.gnu.org/licenses/lgpl.html).

### English

Copyright &copy; 2016-2018 [José Flávio de Souza Dias Júnior](http://joseflavio.com)

This file is part of Uxi-amarelo - [http://joseflavio.com/uxiamarelo](http://joseflavio.com/uxiamarelo).

Uxi-amarelo is free software: you can redistribute it and/or modify
it under the terms of the [GNU Lesser General Public License](https://www.gnu.org/licenses/lgpl.html) as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Uxi-amarelo is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Uxi-amarelo. If not, see [https://www.gnu.org/licenses/lgpl.html](https://www.gnu.org/licenses/lgpl.html).
