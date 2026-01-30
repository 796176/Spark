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

package org.example.spark.account.persistence;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.example.spark.account.events.AccountCreated;
import org.example.spark.account.events.AccountCreatedImpl;
import org.example.spark.account.events.AccountEvent;
import org.example.spark.account.events.AccountEventConverter;
import org.example.spark.account.aggregates.Account;
import org.example.spark.account.aggregates.AccountImpl;
import org.example.spark.authorization.Role;
import org.example.spark.account.intaractors.AccountDataAccess;
import org.example.spark.account.models.*;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
public class JPAAccountDataAccess implements AccountDataAccess {

	private final EntityManagerFactory entityManagerFactory;

	private final AccountEventConverter<String> accountEventConverter;

	public JPAAccountDataAccess(
		@Nonnull EntityManagerFactory entityManagerFactory,
		@Nonnull AccountEventConverter<String> accountEventConverter
	) {
		this.entityManagerFactory = entityManagerFactory;
		this.accountEventConverter = accountEventConverter;
	}

	@Override
	public Account getAccount(long id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		AccountEntity accountEntity = entityManager.find(AccountEntity.class, id);
		entityManager.clear();
		entityManager.close();
		return toAccount(accountEntity);
	}

	@Override
	public Account[] getAccounts() {
		CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
		CriteriaQuery<AccountEntity> q = cb.createQuery(AccountEntity.class);
		Root<AccountEntity> accountEntityRoot = q.from(AccountEntity.class);
		q.select(accountEntityRoot);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		TypedQuery<AccountEntity> typedQuery = entityManager.createQuery(q);
		Stream<AccountEntity> accountEntityStream = typedQuery.getResultStream();
		entityManager.clear();
		entityManager.close();
		return accountEntityStream.map(this::toAccount).toArray(Account[]::new);
	}

	private Account toAccount(AccountEntity accountEntity) {
		Role[] roles = accountEntity
			.getAssignedRoles()
			.stream()
			.filter(AssignedRole::isActive)
			.map(assignedRole -> Role.fromId(assignedRole.getRoleEntity().getId()))
			.toArray(Role[]::new);
		return new AccountImpl(
			accountEntity.getId(),
			accountEntity.getName(),
			accountEntity.getEncodedPassword(),
			roles,
			Account.Status.fromId(accountEntity.getAccountStatus().getId())
		);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = false)
	@Override
	public void persist(@Nonnull Account account, @Nullable UUID idempotenceToken, @Nonnull AccountEvent... events) {
		entityManagerFactory.runInTransaction(entityManager -> {
			if (idempotenceToken != null) {
				if (entityManager.find(ProcessedMessage.class, idempotenceToken) != null) return;

				ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
				entityManager.persist(processedMessage);
			}

			AccountEntity accountEntity = entityManager.find(AccountEntity.class, account.getId());
			for (AssignedRole assignedRole: accountEntity.getAssignedRoles()) {
				assignedRole.setActive(false);
			}
			ArrayList<AssignedRole> newAssignedRoles = new ArrayList<>();
			for (Role role: account.getRoles()) {
				AssignedRole assignedRole = find(accountEntity.getAssignedRoles(), role);
				if (assignedRole != null) assignedRole.setActive(true);
				else {
					newAssignedRoles.add(
						new AssignedRole(accountEntity, entityManager.find(RoleEntity.class, role.getId()), true)
					);
				}
			}
			newAssignedRoles.addAll(accountEntity.getAssignedRoles());
			accountEntity.setAssignedRoles(newAssignedRoles);

			accountEntity.setAccountStatus(entityManager.find(AccountStatus.class, account.getStatus().getId()));

			for (AccountEvent event: events) {
				EventEntity eventEntity = new EventEntity(
					event.getType(),
					accountEventConverter.getEncodingFormat(),
					accountEventConverter.convert(event)
				);
				entityManager.persist(eventEntity);
			}
		});
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = false)
	@Override
	public Account createAccount(
		@Nonnull String name, @Nonnull String encodedPassword, @Nonnull UUID idempotenceToken, @Nonnull Role... roles
	) {
		AtomicLong atomicLong = new AtomicLong();
		entityManagerFactory.runInTransaction(entityManager -> {
			if (entityManager.find(ProcessedMessage.class, idempotenceToken) != null) {
				//SELECT id FROM accounts WHERE accounts.name = name;
				CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
				CriteriaQuery<Long> q = cb.createQuery(Long.class);
				Root<AccountEntity> account = q.from(AccountEntity.class);
				q.where(cb.equal(account.get(AccountEntity_.name), name));
				q.select(account.get(AccountEntity_.ID));
				TypedQuery<Long> typedQuery = entityManager.createQuery(q);
				long accountEntityId = typedQuery.getSingleResultOrNull();
				atomicLong.set(accountEntityId);
				return;
			}

			ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
			entityManager.persist(processedMessage);

			AccountEntity accountEntity = new AccountEntity(
				name, encodedPassword, List.of(), entityManager.find(AccountStatus.class, Account.Status.ACTIVE.getId())
			);
			ArrayList<AssignedRole> assignedRoles = new ArrayList<>(roles.length);
			for (Role role: roles) {
				assignedRoles.add(
					new AssignedRole(accountEntity, entityManager.find(RoleEntity.class, role.getId()), true)
				);
			}
			accountEntity.setAssignedRoles(assignedRoles);
			entityManager.persist(accountEntity);

			AccountCreated accountCreatedEvent = new AccountCreatedImpl(
				accountEntity.getId(), name, encodedPassword, Arrays.stream(roles).mapToLong(Role::getId).toArray()
			);
			EventEntity event = new EventEntity(
				accountCreatedEvent.getType(),
				accountEventConverter.getEncodingFormat(),
				accountEventConverter.convert(accountCreatedEvent)
			);
			entityManager.persist(event);

			atomicLong.set(accountEntity.getId());
		});

		return new AccountImpl(atomicLong.get(), name, encodedPassword, roles, Account.Status.ACTIVE);
	}


	private AssignedRole find(Collection<AssignedRole> assignedRoles, Role role) {
		for (AssignedRole assignedRole: assignedRoles) {
			if (assignedRole.getRoleEntity().getId() == role.getId()) return assignedRole;
		}
		return null;
	}
}
