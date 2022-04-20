/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.flem.fwe.bo;

import br.org.flem.fwe.exception.AplicacaoException;
import br.org.flem.fwe.hibernate.dao.base.BaseDAOAb;
import br.org.flem.fwe.hibernate.dto.base.BaseDTOAb;
import java.util.Collection;

/**
 *
 * @author fcsilva
 * Business Object Abstrato td dd
 */
public abstract class BaseBOAb<T extends BaseDTOAb> {

        protected BaseDAOAb dao;

        public BaseBOAb(BaseDAOAb dao) throws AplicacaoException {
                this.dao = dao;
        }

        public void inserirOuAlterar(T objeto) throws AplicacaoException {
                dao.inserirOuAlterar(objeto);
        }

        public Object inserir(T objeto) throws AplicacaoException {
                return dao.inserir(objeto);
        }

        public void alterar(T objeto) throws AplicacaoException {
                dao.alterar(objeto);
        }

        public void excluir(T objeto) throws AplicacaoException {
                dao.excluir(objeto);
        }

        public void excluir(Collection<T> objetos) throws AplicacaoException {
                dao.excluir(objetos);
        }

        public T obterPorPk(T objeto) throws AplicacaoException {
                return (T) dao.obterPorPk(objeto);
        }

        public T obterPorPk(Integer id) throws AplicacaoException {
                return (T) dao.obterPorPk(id);
        }
        
        public T obterPorPkNull(T objeto) throws AplicacaoException {
                return (T) dao.obterPorPkNull(objeto);
        }

        public T obterPorPkNull(Integer id) throws AplicacaoException {
                return (T) dao.obterPorPkNull(id);
        }

        public Collection<T> obterTodos() throws AplicacaoException {
                return dao.obterTodos();
        }

        public Collection<T> obterPorFiltro(T objeto) throws AplicacaoException {
            return dao.obterPorFiltro(objeto);
        }
}