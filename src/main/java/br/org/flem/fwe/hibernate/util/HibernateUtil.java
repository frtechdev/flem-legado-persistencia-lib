 /*
 * HibernateUtil.java
 *
 * Created on 6 de Setembro de 2006, 17:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package br.org.flem.fwe.hibernate.util;


import br.org.flem.fwe.exception.AcessoDadosException;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

/**
 *
 * @author mjpereira
 */
public class HibernateUtil {
    
	private static boolean sessaoUnica = false;

	private static final SessionFactory sessionFactory;

     


	/**
	 * Variavel "thread-safe" que mant�m uma refer�ncia para a sess�o do
	 * hibernate
	 */
	private static final ThreadLocal<Session> threadSession = new ThreadLocal<Session>();

	private static Session session;

	/**
	 * Vari�vel "thread-safe" que mant�m uma refer�ncia para a transa��o do
	 * hibernate
	 */
	private static final ThreadLocal<Transaction> threadTransaction = new ThreadLocal<Transaction>();

	private static Transaction transaction;

	/**
	 * Vari�vel "thread-safe" que mant�m um interceptor
	 */
	private static final ThreadLocal<Interceptor> threadInterceptor = new ThreadLocal<Interceptor>();

	private static Interceptor interceptor;

    static {

        
            try {
                // instancia um sessionFactory
                Configuration conf = new AnnotationConfiguration();
                sessionFactory = conf.configure("hibernate.cfg.xml").buildSessionFactory();
            } catch (HibernateException ex) {
                System.err.println("Initial SessionFactory creation failed. " + ex);
                throw new ExceptionInInitializerError(ex);
            }
       
 

        
    }

	public static void setSessaoUnica(boolean valor) {
		sessaoUnica = valor;
	}

	/**
	 * Obtem uma inst�ncia do SessionFactory.
	 * 
	 * @return Inst�ncia do SessinFactory.
	 */
    public static SessionFactory getSessionFactory() {
            return sessionFactory;    
    }

	/**
	 * Abre uma nova sess�o, se a thread ainda n�o possuir nenhuma
	 * 
	 * @return A sess�o criada
	 * @throws AcessoDadosException
	 */
	public static Session getSession() throws AcessoDadosException {
		Session s = sessaoUnica ? session : threadSession.get();
		Interceptor i = sessaoUnica ? interceptor : threadInterceptor.get();
                    
		try {
			if (s == null) {
				if (i == null)
					s = sessionFactory.openSession();
				else
					s = sessionFactory.openSession(i);

				threadSession.set(s);
				session = s;
			}
		} catch (HibernateException ex) {
			throw new AcessoDadosException(ex);
		}

		return s;
	}

	/**
	 * Fecha a sess�o, se a thread possuir uma e esta estiver aberta
	 * 
	 * @throws AcessoDadosException
	 */
	public static void closeSession() throws AcessoDadosException {
		try {
			Session s = sessaoUnica ? session : threadSession.get();

			threadSession.set(null);
			session = null;

			if (s != null && s.isOpen()) {
				commitTransaction();
				s.close();
			}
		} catch (HibernateException ex) {
			throw new AcessoDadosException(ex);
		}
	}

	/**
	 * Inicia uma transa��o. Se a sess�o do hibernate n�o estiver iniciada, cria
	 * uma nova.
	 * 
	 * @return TRUE se uma nova transa��o for iniciada. FALSE se utilizar uma
	 *         transa��o j� existente.
	 * @throws AcessoDadosException
	 */
	public static boolean beginTransaction() throws AcessoDadosException {
		Transaction tx = sessaoUnica ? transaction : threadTransaction.get();
		try {
			if (tx == null) {
				tx = getSession().beginTransaction();
				threadTransaction.set(tx);
				transaction = tx;
				return true;
			}

			return false;
		} catch (HibernateException ex) {
			throw new AcessoDadosException(ex);
		}
	}

	/**
	 * Comita a transa��o. Este m�todo utiliza a transa��o global iniciada nesta
	 * thread pelo m�todo HibernateUtil.beginTransaction()
	 * 
	 * @throws AcessoDadosException
	 */
	public static void commitTransaction() throws AcessoDadosException {
		Transaction tx = sessaoUnica ? transaction : threadTransaction.get();
		try {
			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack())
				tx.commit();

			threadTransaction.set(null);
			transaction = null;

		} catch (HibernateException ex) {
			rollbackTransaction();
			throw new AcessoDadosException(ex);
		}
	}

	/**
	 * Desfaz a transa��o
	 * 
	 * @throws AcessoDadosException
	 */
	public static void rollbackTransaction() throws AcessoDadosException {
		Transaction tx = sessaoUnica ? transaction : threadTransaction.get();
		try {
			threadTransaction.set(null);
			transaction = null;

			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack())
				tx.rollback();

		} catch (HibernateException ex) {
			throw new AcessoDadosException(ex);
		} finally {
			closeSession();
		}
	}
        
        /**
	 * Desfaz a transa��o
	 * 
	 * @throws AcessoDadosException
	 */
	public static void rollbackTransactionAndKeepSession() throws AcessoDadosException {
		Transaction tx = sessaoUnica ? transaction : threadTransaction.get();
		try {
			threadTransaction.set(null);
			transaction = null;

			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack())
				tx.rollback();

		} catch (HibernateException ex) {
			throw new AcessoDadosException(ex);
		}
	}

	/**
	 * Registra um interceptor na thread atual. As pr�ximas sess�es do hibernate
	 * que forem abertas, utilizar�o este interceptor.
	 * 
	 * @param interceptor
	 *            Interceptor a ser registrado.
	 * @throws AcessoDadosException
	 */
	public static void registrarInterceptor(Interceptor intcptr)
			throws AcessoDadosException {

		try {
			threadInterceptor.set(intcptr);
			interceptor = intcptr;
		} finally {
			closeSession();
			getSession();
		}

	}

}
