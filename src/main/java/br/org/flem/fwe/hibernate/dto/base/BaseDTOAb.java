/*
 * BaseDTOAb.java
 *
 * Created on 10 de Setembro de 2006, 10:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package br.org.flem.fwe.hibernate.dto.base;

import java.io.Serializable;

/**
 *
 * @author mario
 */
public abstract class BaseDTOAb implements Serializable, Comparable {

	/**
	 * Compara apenas os campos chave do DTO, através do método getPk().
	 *
	 * @param o
	 *            Objeto a ser comparado.
	 * @return Um valor negativo, zero ou positivo, caso este objeto seja menor
	 *         que, igual ou maior que o do parâmetro
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(Object o) {

                if(o == null){
                    return -1;
                }

		BaseDTOAb obj = (BaseDTOAb) o;
                if (this.getPk() != null && obj.getPk() != null) {
                    return ((Comparable) this.getPk()).compareTo(obj.getPk());
                }
                else {
                    return -1;
                }
	}

	/**
	 * Obtém o valor do atributo que simboliza a PK deste DTO. Caso a PK seja
	 * composta, deve-se retornar o próprio objeto (this).
	 *
	 * @return O valor do atributo que simboliza a PK deste DTO.
	 */
	public abstract Serializable getPk();

	/**
	 * Compara a igualdade entre os objetos apenas considerando o valor da PK
	 * dos DTOs.
	 *
	 * @param o
	 *            Objeto a ser comparado
	 * @return True se os objetos possuirem o mesmo valor de PK. False caso
	 *         contrário
	 */
	@Override
	public boolean equals(Object o) {
                if(o == null){
                    return false;
                }

		if (o instanceof BaseDTOAb) {
			BaseDTOAb ob = (BaseDTOAb) o;
			return this.getPk() == null ? false : this.getPk().equals(ob.getPk());
		}

                else {
			return false;
		}
	}

	/**
	 * Compara todos os atributos do objeto
	 *
	 * @param obj
	 *            Objeto a ser comparado
	 * @return true se os objetos possuirem o valores iguais de todos os
	 *         atributos. False caso contrário.
	 */
	public boolean equalsCompleto(BaseDTOAb obj) {
		return false;
	}

	@Override
	public int hashCode() {
		Serializable hc = this.getPk();
		return hc == null ? Integer.MIN_VALUE : hc.hashCode();
	}
}
