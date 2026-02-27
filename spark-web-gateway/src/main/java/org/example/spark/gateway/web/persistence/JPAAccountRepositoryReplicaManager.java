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
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.spark.authorization.Role;
import org.example.spark.gateway.web.interactors.AccountRepositoryReplicaManager;
import org.example.spark.gateway.web.models.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class JPAAccountRepositoryReplicaManager implements AccountRepositoryReplicaManager {

	private final EntityManagerFactory entityManagerFactory;

	public JPAAccountRepositoryReplicaManager(@Nonnull EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public void addAccount(
		long accountId,
		@Nonnull String name,
		@Nonnull String encodedPassword,
		@Nonnull Role[] roles,
		@Nonnull String idempotenceToken
	) {
		entityManagerFactory.runInTransaction(entityManager -> {
			if (entityManager.find(ProcessedMessage.class, idempotenceToken) != null) return;

			ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
			entityManager.persist(processedMessage);

			AccountEntity account = new AccountEntity(
				accountId,
				name,
				encodedPassword,
				List.of(),
				entityManager.find(AccountStatus.class, Account.Status.ACTIVE.getId())
			);
			ArrayList<AssignedRole> assignedRoles = new ArrayList<>(roles.length);
			for (Role role: roles) {
				assignedRoles.add(new AssignedRole(account, entityManager.find(RoleEntity.class, role.getId())));
			}
			account.setAssignedRoles(assignedRoles);
			entityManager.persist(account);
		});
	}

	@Override
	public void suspendAccount(long accountId) {
		entityManagerFactory.runInTransaction(entityManager -> {
			AccountEntity account = entityManager.find(AccountEntity.class, accountId);
			if (account == null) throw new IllegalArgumentException();
			account.setAccountStatus(entityManager.find(AccountStatus.class, Account.Status.SUSPENDED.getId()));
		});
	}

	@Override
	public void deleteAccount(long accountId) {
		entityManagerFactory.runInTransaction(entityManager -> {
			AccountEntity account = entityManager.find(AccountEntity.class, accountId);
			if (account == null) throw new IllegalArgumentException();
			account.setAccountStatus(entityManager.find(AccountStatus.class, Account.Status.DELETED.getId()));
		});
	}

	@Override
	public void restoreAccount(long accountId) {
		entityManagerFactory.runInTransaction(entityManager -> {
			AccountEntity account = entityManager.find(AccountEntity.class, accountId);
			if (account == null) throw new IllegalArgumentException();
			account.setAccountStatus(entityManager.find(AccountStatus.class, Account.Status.ACTIVE.getId()));
		});
	}

	@Override
	public void changeRoles(long accountId, @Nonnull Role[] roles) {
		entityManagerFactory.runInTransaction(entityManager -> {
			AccountEntity account = entityManager.find(AccountEntity.class, accountId);
			if (account == null) throw new IllegalArgumentException();

			Comparator<Role> roleComparator = (r1, r2) -> (int) (r1.getId() - r2.getId());
			Arrays.sort(roles, roleComparator);
			for (AssignedRole assignedRole: account.getAssignedRoles().stream().toList()) {
				boolean notFound =
					Arrays.binarySearch(roles, Role.fromId(assignedRole.getRoleEntity().getId()), roleComparator) < 0;
				if (notFound) {
					account.getAssignedRoles().remove(assignedRole);
					entityManager.remove(assignedRole);
				}
			}

			long[] sortedAssignedRoleIds = account
				.getAssignedRoles()
				.stream()
				.mapToLong(a -> a.getRoleEntity().getId())
				.sorted()
				.toArray();
			for (Role role: roles) {
				boolean notFound = Arrays.binarySearch(sortedAssignedRoleIds, role.getId()) < 0;
				if (notFound) {
					AssignedRole newAssignedRole = new AssignedRole(account, entityManager.find(RoleEntity.class, role.getId()));
					entityManager.persist(newAssignedRole);
					account.getAssignedRoles().add(newAssignedRole);
				}
			}
		});
	}
}
