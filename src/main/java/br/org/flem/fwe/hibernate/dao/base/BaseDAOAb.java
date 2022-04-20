 /*
 * BaseDAOAb.java
 *
 * Created on 10 de Setembro de 2006, 12:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package br.org.flem.fwe.hibernate.dao.base;

import br.org.flem.fwe.exception.AcessoDadosException;
import br.org.flem.fwe.hibernate.dto.base.BaseDTOAb;
import br.org.flem.fwe.hibernate.util.HibernateUtil;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;

/**
 *
 * @author mario
 */
public abstract class BaseDAOAb<T extends BaseDTOAb> {

    /**
     * Refer�ncia para a sess�o atual do hibernate, obtida na inicializa��o de
     * uma inst�ncia desta classe
     */
    protected Session session;
    /**
     * Comparator utilizado na convers�o de List para TreeMap
     */
    private Comparator<Serializable> compMap = new Comparator<Serializable>() {

        @SuppressWarnings("unchecked")
        @Override
        public int compare(Serializable o1, Serializable o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null && o2 != null) {
                return -1;
            } else if (o1 != null && o2 == null) {
                return 1;
            } else {
                return ((Comparable<Serializable>) o1).compareTo(o2);
            }
        }
    };

    /* ---------------------------------------------------------------------- */
    /**
     * Instancia o DAO obtendo uma refer�ncia para a sess�o atual do hibernate e
     * iniciando uma transa��o(caso uma n�o esteja iniciada)
     *
     * @throws AcessoDadosException
     */
    public BaseDAOAb() throws AcessoDadosException {
        
        session = HibernateUtil.getSession();
        HibernateUtil.beginTransaction();
    }

    /**
     * Instancia o DAO utilizando a sess�o passada como par�metro
     *
     * @param s
     *            Sess�o que ser� utilizada pelos m�todos do DAO
     */
    public BaseDAOAb(Session s) {
        session = s;
    }

    /* ---------------------------------------------------------------------- */
    /**
     * Insere um registro no banco de acordo com tipo objeto passado no
     * par�metro.
     *
     * @param dto
     *            Objeto com os dados a serem persistidos no banco
     * @throws AcessoDadosException
     */
    public Object inserir(T dto) throws AcessoDadosException {
        Object obj = null;
        try {
            this.preencherPk(dto);
            obj = session.save(dto);
            session.flush();
        } catch (HibernateException e) {
            throw new AcessoDadosException(e);
        }
        return obj;

    }

    /**
     * Insere uma lista de registros no banco de acordo com tipo da lista passada no
     * parametro.
     *
     * @param lista
     * Lista com os objetos a serem persistidos no banco
     * @throws AcessoDadosException
     */
    public Set<T> inserirOuAlterar(Set<T> lista) throws AcessoDadosException {
        if (lista != null && !lista.isEmpty()) {
            try {
                for (T dto : lista) {
                    inserirOuAlterar(dto);
                }
            } catch (HibernateException e) {
                throw new AcessoDadosException(e);
            }
        }
        return lista;
    }

    /**
     * Altera os dados no banco do registro correspondente ao objeto passado no
     * par�metro.
     *
     * @param dto
     *            Objeto com os dados a serem alterados no banco
     * @throws AcessoDadosException
     */
    public void alterar(T dto) throws AcessoDadosException {
        try {
            session.update(dto);
            session.flush();
        } catch (HibernateException e) {
            throw new AcessoDadosException(e);
        }

    }

    /**
     * Insere ou Altera os dados no banco do registro correspondente ao objeto
     * passado no par�metro a depender do valor de sua PK. Se este valor j�
     * existir no banco, atualiza. Caso contr�rio insere.
     *
     * @param dto
     *            Objeto com os dados a serem alterados no banco
     * @throws AcessoDadosException
     */
    public void inserirOuAlterar(T dto) throws AcessoDadosException {

        try {
            this.preencherPk(dto);
            session.saveOrUpdate(dto);
            session.flush();
        } catch (HibernateException e) {
            throw new AcessoDadosException(e);
        }

    }

    /**
     * Exclui o registro do banco correspondente ao objeto passado no par�metro.
     *
     * @param dto
     *            Objeto com os dados do registro a ser excluido do banco.
     * @throws AcessoDadosException
     */
    public void excluir(T dto) throws AcessoDadosException {

        try {
            session.delete(dto);
            session.flush();
        } catch (HibernateException e) {
            throw new AcessoDadosException(e);
        }

    }

    /**
     * Exclui uma cole��o de registros do banco. Caso algum registro n�o possa
     * ser excluido, toda a transa��o � desfeita.
     *
     * @param itens
     *            Cole��o de itens a serem excluidos do banco
     * @throws AcessoDadosException
     */
    public void excluir(Collection<T> itens) throws AcessoDadosException {
        HibernateUtil.beginTransaction();

        try {
            for (T item : itens) {
                item = this.obterPorPk(item);
                // excluir um item sem ter que
                // obte-lo do banco
                this.excluir(item);
            }
        } catch (AcessoDadosException e) {
            HibernateUtil.rollbackTransaction();
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Obtem os dados do banco do registro correspondente ao objeto passado no
     * par�metro. O atributo pk deste objeto deve estar preenchido para que o
     * Hibernate saiba qual registro obter do banco.
     *
     * @param dto
     *            Objeto que ter� seus dados preenchidos a partir do dados
     *            guardados no banco.
     * @return
     * @throws AcessoDadosException
     */
    public T obterPorPk(T dto) throws AcessoDadosException {
        return this.obterPorPk(dto.getPk());
    }
    
    public T obterPorPkNull(T dto) throws AcessoDadosException {
        return this.obterPorPkNull(dto.getPk());
    }

    /**
     * Obtem os dados do banco do registro correspondente � chave passada no
     * par�metro.
     *
     * @param chave
     *            Valor da PK do registro que ser� obtido do banco.
     * @return
     * @throws AcessoDadosException
     */
    @SuppressWarnings("unchecked")
    public T obterPorPk(Serializable chave) throws AcessoDadosException {
        try {
            return (T) session.load(this.getClasseDto(), chave);
        } catch (HibernateException e) {
            throw new AcessoDadosException(e);
        }

    }
    
    @SuppressWarnings("unchecked")
    public T obterPorPkNull(Serializable chave) throws AcessoDadosException {
        try {
            return (T) session.get(this.getClasseDto(), chave);
        } catch (HibernateException e) {
            throw new AcessoDadosException(e);
        }

    }

    /**
     * Obtem todos os registros do tipo correspondente ao retorno do m�todo
     * getClass(), que deve ser implementado pelas subclasses.
     *
     * @return Lista com todos os registros do tipo correspondente ao retorno do
     *         m�todo getClass().
     * @throws AcessoDadosException
     */
    @SuppressWarnings("unchecked")
    public List<T> obterTodos() throws AcessoDadosException {
        try {
            return session.createCriteria(this.getClasseDto()).list();
        } catch (HibernateException e) {
            throw new AcessoDadosException(e);
        }
    }

    /**
     * Obtem todos os registros do tipo correspondente ao retorno do m�todo
     * getClass(), que deve ser implementado pelas subclasses.
     *
     * @return Lista com todos os registros do tipo correspondente ao retorno do
     *         m�todo getClass().
     * @throws AcessoDadosException
     */
    @SuppressWarnings("unchecked")
    public List<T> obterTodosOrdenadoPorCampo(String nomeCampo) throws AcessoDadosException {
        try {
            Criteria criteria = getNovoCriterio();
            criteria.addOrder(Order.asc(nomeCampo));
            return criteria.list();
        } catch (HibernateException e) {
            throw new AcessoDadosException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> obterTodosOrdenadoPorCampo(String[] campos) throws AcessoDadosException {
        try {
            Criteria criteria = getNovoCriterio();
            for (int i = 0; i < campos.length; i++) {
                criteria.addOrder(Order.asc(campos[i]));
            }
            return criteria.list();
        } catch (HibernateException e) {
            throw new AcessoDadosException(e);
        }
    }

    /**
     * Obtem um map com todos os registros correspondente ao retorno do m�todo
     * getClass(). A chave do map � a pk dos registros retornados.
     *
     * @return Map com todos os registros correspondente ao retorno do m�todo
     *         getClass()
     * @throws AcessoDadosException
     */
    public Map<Serializable, T> obterTodosMap() throws AcessoDadosException {
        List<T> temp = this.obterTodos();
        return this.convertToMap(temp);
    }

    @SuppressWarnings("unchecked")
    public List<T> obterPorFiltro(T objeto) {
        Criteria criteria = session.createCriteria(getClasseDto());

        Example example = Example.create(objeto);
        example = example.enableLike(MatchMode.ANYWHERE);
        example = example.ignoreCase();
        acessarExample(example);

        criteria.add(example);
        adicionarAgregacoesCriteria(criteria, objeto);
        adicionarOrdemCriteria(criteria);
        return criteria.list();
    }

    protected void adicionarAgregacoesCriteria(Criteria c, T objeto) {
    }

    protected void adicionarOrdemCriteria(Criteria c) {
    }
    
    /*
     * Permite acessar o Criterion Example a ser utilizado no metodo obterPorFiltro*/
    protected void acessarExample(Example e) {
    }

    /**
     * Converte uma List em um TreeMap. A chave dos itens do map � o valor do
     * atributo referente � PK.
     *
     * @param lista
     *            Lista a ser convertida
     * @return Map contendo os mesmo itens da lista, onde a chave � a PK de cada
     *         item da lista e o valor do map s�o os pr�prios itens da lista
     */
    public Map<Serializable, T> convertToMap(List<T> lista) {
        Map<Serializable, T> itens = new TreeMap<Serializable, T>(compMap);

        for (T item : lista) {
            itens.put(item.getPk(), item);
        }

        return itens;
    }

    /* ---------------------------------------------------------------------- */
    /**
     * Retorna a classe DTO que este DAO faz refer�ncia. Este m�todo deve ser
     * implementado pelas subclasses e retornar a classe DTO relativa ao DAO.
     *
     * @return Classe DTO referenciada pelo DAO
     */
    protected abstract Class<T> getClasseDto();

    /**
     * Preenche o valor da pk do dto passado no parametro. Este m�todo �
     * invocado antes de salvar o dto no banco. A implementa��o padr�o deste
     * m�todo n�o altera o valor da pk, deixando para que o hibernate fa�a-o no
     * caso do dto possuir algum tipo de gerador associado. Este m�todo pode ser
     * sobrescrito pelas subclasses que necessitem definir um valor para a pk
     * antes do dto ser persistido.
     *
     * @param dto
     *            DTO que ser� persistido.
     */
    protected void preencherPk(T dto) {
        // n�o faz nada
    }

    protected Criteria getNovoCriterio() {
        return session.createCriteria(getClasseDto());
    }
}
