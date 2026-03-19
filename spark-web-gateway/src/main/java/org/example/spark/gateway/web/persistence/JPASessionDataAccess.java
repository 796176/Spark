/*
 * Spark - The inventory management application
 * Copyright (C) 2026 Yegore Vlussove
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.example.spark.gateway.web.persistence;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import org.example.spark.authorization.Role;
import org.example.spark.gateway.web.interactors.SessionDataAccess;
import org.example.spark.gateway.web.models.*;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(isolation = Isolation.SERIALIZABLE)
public class JPASessionDataAccess implements SessionDataAccess {

	private final EntityManagerFactory entityManagerFactory;

	public JPASessionDataAccess(@Nonnull EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
	@Override
	public Session getSession(@Nonnull String id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		SessionEntity sessionEntity = entityManager.find(SessionEntity.class, id);
		entityManager.clear();
		entityManager.close();

		return sessionEntity == null ? null : toSession(sessionEntity);
	}

	private Session toSession(SessionEntity sessionEntity) {
		AccountEntity account = sessionEntity.getAccountEntity();
		return new Session(
			sessionEntity.getId(),
			new AccountImpl(
				account.getId(),
				account.getEncodedPassword(),
				account.getName(),
				account.getAssignedRoles()
					.stream()
					.map(role -> Role.fromId(role.getRoleEntity().getId()))
					.toArray(Role[]::new),
				Account.Status.fromId(account.getAccountStatus().getId())
			)
		);
	}

	@Override
	public void invalidateSession(@Nonnull Session session) {
		entityManagerFactory.runInTransaction(entityManager -> {
			SessionEntity sessionEntity = entityManager.find(SessionEntity.class, session.getId());
			if (sessionEntity != null) entityManager.remove(sessionEntity);
		});
	}

	@Override
	public void invalidateSessionsForAccount(long accountId) {
		CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
		CriteriaDelete<SessionEntity> q = cb.createCriteriaDelete(SessionEntity.class);
		Root<SessionEntity> session = q.from(SessionEntity.class);
		q.where(cb.equal(session.get(SessionEntity_.accountEntity).get(AccountEntity_.id), accountId));
		entityManagerFactory.runInTransaction(entityManager -> entityManager.createQuery(q).executeUpdate());
	}

	@Override
	public void persist(@Nonnull Session session) {
		entityManagerFactory.runInTransaction(entityManager -> {
			SessionEntity sessionEntity = new SessionEntity(
				session.getId(), entityManager.find(AccountEntity.class, session.getAccount().getId())
			);
			entityManager.persist(sessionEntity);
		});
	}

	@Override
	public void replaceSession(@Nonnull Session oldSession, @Nonnull Session newSession) {
		CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
		CriteriaUpdate<SessionEntity> q = cb.createCriteriaUpdate(SessionEntity.class);
		Root<SessionEntity> session = q.from(SessionEntity.class);
		q.where(cb.equal(session.get(SessionEntity_.id), oldSession.getId()));
		q.set(SessionEntity_.id, newSession.getId());
		entityManagerFactory.runInTransaction(entityManager -> entityManager.createQuery(q).executeUpdate());
	}
}
