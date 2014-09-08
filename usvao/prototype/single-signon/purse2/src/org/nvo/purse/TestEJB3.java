package org.nvo.purse;

import org.nvo.purse.orm.Player;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.EntityManager;

/** From http://www.censnet.it/articles/cap03.htm */
public class TestEJB3 {
    public static void main(String[] args) {
        EntityManagerFactory emfMySQL = Persistence.createEntityManagerFactory("sample");
        EntityManager emMySQL = emfMySQL.createEntityManager();

        emMySQL.getTransaction().begin();
        Player p;

        p = new Player();
        p.setName("First User");
        emMySQL.persist(p);

        p = new Player();
        p.setName("Second User");
        emMySQL.persist(p);

        emMySQL.getTransaction().commit();
    }
}
