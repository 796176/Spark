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

package org.example.spark.inventory.persistence;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.example.spark.inventory.aggregates.ItemAggregate;
import org.example.spark.inventory.aggregates.VersionedItemAggregate;
import org.example.spark.inventory.sagas.Saga;
import org.example.spark.inventory.interactors.SagaDataAccess;
import org.example.spark.inventory.models.*;
import org.example.spark.inventory.sagas.SagaFactory;
import org.example.spark.inventory.sagas.SagaType;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = false)
public class JPASagaDataAccess implements SagaDataAccess {

	private final JPAItemDataAccess itemDataAccess;

	private final EntityManagerFactory entityManagerFactory;

	private final SagaFactory sagaFactory;

	public JPASagaDataAccess(
		@Nonnull JPAItemDataAccess itemDataAccess,
		@Nonnull EntityManagerFactory entityManagerFactory,
		@Nonnull SagaFactory sagaFactory
	) {
		this.itemDataAccess = itemDataAccess;
		this.entityManagerFactory = entityManagerFactory;
		this.sagaFactory = sagaFactory;
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
	@Override
	public Saga[] getSagas() {
		// SELECT * FROM sagas;
		CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
		CriteriaQuery<SagaEntity> q = cb.createQuery(SagaEntity.class);
		Root<SagaEntity> saga = q.from(SagaEntity.class);
		q.select(saga);

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		TypedQuery<SagaEntity> typedQuery = entityManager.createQuery(q);
		List<SagaEntity> sagaEntityList = typedQuery.getResultList();
		entityManager.clear();
		entityManager.close();
		return sagaEntityList
			.stream()
			.map(sagaEntity -> {
				return sagaFactory.instantiateSaga(
					sagaEntity.getId(),
					itemDataAccess.getItem(sagaEntity.getItem().getId()),
					sagaEntity.getIdempotenceToken(),
					SagaType.fromId(sagaEntity.getSagaType()),
					sagaEntity.getState()
				);
			})
			.filter(Objects::nonNull)
			.toArray(Saga[]::new);
	}

	@Override
	public Saga newSaga(
		@Nonnull ItemAggregate item,
		@Nonnull String idempotenceToken,
		@Nonnull SagaType sagaType
	) {
		AtomicReference<Saga> atomicSaga = new AtomicReference<>();

		entityManagerFactory.runInTransaction(entityManager -> {
			if (entityManager.find(ProcessedMessage.class, idempotenceToken) != null) {
				// SELECT * FROM sagas WHERE item_id = item.getId();
				CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
				CriteriaQuery<SagaEntity> q = cb.createQuery(SagaEntity.class);
				Root<SagaEntity> saga = q.from(SagaEntity.class);
				q.where(cb.equal(saga.get(SagaEntity_.item).get(ItemEntity_.id), item.getId()));
				q.select(saga);

				TypedQuery<SagaEntity> typedQuery = entityManager.createQuery(q);
				SagaEntity sagaEntity = typedQuery.getSingleResultOrNull();


				Saga instantiatedSaga = sagaFactory.instantiateSaga(
					sagaEntity.getId(),
					itemDataAccess.getItem(sagaEntity.getItem().getId()),
					sagaEntity.getIdempotenceToken(),
					SagaType.fromId(sagaEntity.getSagaType()),
					sagaEntity.getState()
				);
				atomicSaga.set(instantiatedSaga);
				return;
			}

			ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
			entityManager.persist(processedMessage);

			VersionedItemAggregate versionedItem = itemDataAccess.getVersionedItem(item.getId());
			versionedItem.item().setStatus(ItemAggregate.Status.BUSY);
			itemDataAccess.persist(versionedItem.item(), versionedItem.version(),null);

			SagaEntity sagaEntity = new SagaEntity(
				sagaFactory.getInitialState(sagaType).getId(),
				entityManager.find(ItemEntity.class, item.getId()),
				sagaType.getId()
			);
			entityManager.persist(sagaEntity);

			Saga saga = sagaFactory
				.instantiateSaga(sagaEntity.getId(), item, sagaEntity.getIdempotenceToken(), sagaType);
			atomicSaga.set(saga);
		});

		return atomicSaga.get();
	}

	@Override
	public String updateState(long sagaId, long newState) {
		AtomicReference<String> atomicString = new AtomicReference<>();
		entityManagerFactory.runInTransaction(entityManager -> {
			SagaEntity saga = entityManager.find(SagaEntity.class, sagaId);
			saga.setState(newState);
			saga.generateNewIdempotenceToken();
			atomicString.set(saga.getIdempotenceToken());
		});

		return atomicString.get();
	}

	@Override
	public void deleteSaga(long sagaId) {
		entityManagerFactory.runInTransaction(entityManager -> {
			// DELETE FROM sagas WHERE sagas.id = sagaId;
			CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
			CriteriaDelete<SagaEntity> q = cb.createCriteriaDelete(SagaEntity.class);
			Root<SagaEntity> saga = q.from(SagaEntity.class);
			q.where(cb.equal(saga.get(SagaEntity_.id), sagaId));
			entityManager.createQuery(q).executeUpdate();
		});
	}
}
