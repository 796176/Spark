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
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.example.spark.authorization.Role;
import org.example.spark.gateway.web.interactors.AccountDataAccess;
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.AccountEntity;
import org.example.spark.gateway.web.models.AccountEntity_;
import org.example.spark.gateway.web.models.AccountImpl;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
public class JPAAccountDataAccess implements AccountDataAccess {

	private final EntityManagerFactory entityManagerFactory;

	public JPAAccountDataAccess(@Nonnull EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public Account getAccountByName(@Nonnull String name) {
		CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
		CriteriaQuery<AccountEntity> q = cb.createQuery(AccountEntity.class);
		Root<AccountEntity> account = q.from(AccountEntity.class);
		q.where(cb.equal(account.get(AccountEntity_.name), name));
		q.select(account);

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		AccountEntity accountEntity = entityManager.createQuery(q).getSingleResultOrNull();
		entityManager.clear();
		entityManager.close();
		if (accountEntity == null) return null;
		return new AccountImpl(
			accountEntity.getId(),
			accountEntity.getEncodedPassword(),
			accountEntity.getName(),
			accountEntity.getAssignedRoles()
				.stream()
				.map(assignedRole -> Role.fromId(assignedRole.getRoleEntity().getId()))
				.toArray(Role[]::new),
			Account.Status.fromId(accountEntity.getAccountStatus().getId())
		);
	}
}
