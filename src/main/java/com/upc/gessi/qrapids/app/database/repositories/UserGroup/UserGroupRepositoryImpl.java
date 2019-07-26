package com.upc.gessi.qrapids.app.database.repositories.UserGroup;

import com.upc.gessi.qrapids.app.domain.repositories.UserGroup.CustomUserGroupRepository;
import com.upc.gessi.qrapids.app.domain.models.UserGroup;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Component("userGroupRepositoryImpl")
public class UserGroupRepositoryImpl implements CustomUserGroupRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserGroup findName(String name) {

        UserGroup result = null;

        List<UserGroup> routes = this.entityManager.createQuery("FROM UserGroup AS u WHERE u.name = :name", UserGroup.class)
                .setParameter("name", name)
                .getResultList();

        if( routes.size() > 0 ) {
            result = routes.get(0);
        }

        return result;
    }

    @Override
    public UserGroup findDefaultUserGroup() {

        UserGroup result = null;

        try {

            result = this.entityManager.createQuery("FROM UserGroup AS u WHERE u.default_group = true", UserGroup.class)
                    .getSingleResult();

        } catch (NoResultException e) {
            System.out.println(e.toString());
        }

        return result;
    }

    @Override
    public boolean hasDefaultGroup() {

        Long groups = Long.parseLong(
                this.entityManager.createNativeQuery("SELECT COUNT(id) FROM UserGroup AS u WHERE u.default_group = true")
                        .getSingleResult().toString()
        );

        System.out.println(groups + " ------- ");

        return ( groups >= 1L );

    }

    @Transactional
    @Override
    public boolean updateUserGroupDefault(long id) {
        System.out.println("HOLA MUNDO");

        int clean, update;

        try {


            clean = this.entityManager.createQuery("UPDATE UserGroup SET default_group = false where default_group = true")
                .executeUpdate();

            update = this.entityManager.createQuery("UPDATE UserGroup SET default_group = true where id = :user_id")
                    .setParameter("user_id", id)
                    .executeUpdate();

        } catch (Exception e) {
            System.out.println(e);
            return false;
        }

        return ( (clean + update) >= 2  );
    }


}
