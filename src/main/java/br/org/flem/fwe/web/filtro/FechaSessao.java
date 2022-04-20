/*
 * FechaSessao.java
 *
 * Created on 6 de Setembro de 2006, 17:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package br.org.flem.fwe.web.filtro;

import br.org.flem.fwe.exception.AcessoDadosException;
import br.org.flem.fwe.web.filtro.base.BaseServletFilterAb;
import br.org.flem.fwe.hibernate.util.HibernateUtil;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 *  Filtro responsável pela garantia do fechamento da conexão ao banco de dados
 * após toda a requisição processada e a resposta enviada ao cliente. Este
 * filtro deve ser o último a ser executado.
 *
 * @author mjpereira
 */
public class FechaSessao extends BaseServletFilterAb{

	/**
	 * Comita qualquer transação que ainda esteja aberta e fecha a conexão com o
	 * banco de dados.
	 */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
                        chain.doFilter(request, response);
			HibernateUtil.commitTransaction();
		} catch (AcessoDadosException e) {
                        e.printStackTrace();
			throw new ServletException("Erro de acesso a dados - Commit ",e);
		} finally {
			try {
				HibernateUtil.closeSession();
			} catch (AcessoDadosException e) {
                                e.printStackTrace();
				throw new ServletException("Erro de acesso a dados - Close Session ",e);
			}
		}
	}

}
