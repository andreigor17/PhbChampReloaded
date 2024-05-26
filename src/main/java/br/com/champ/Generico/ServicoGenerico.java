/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Generico;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author andre
 */
public class ServicoGenerico<T> {

    private Class<T> entity;
    @PersistenceContext
    EntityManager entityManager;

    public ServicoGenerico(Class<T> entity) {
        this.entity = entity;

    }

    public void save(T entity) {

    }

    public void remove(T entity) {
        update(entity);
    }

    public void update(T entity) {
        getEntityManager().merge(entity);
        getEntityManager().flush();
    }

    public T find(Long entityID) {
        T objeto = getEntityManager().find(entity, entityID);
        getEntityManager().refresh(objeto);
        return objeto;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
