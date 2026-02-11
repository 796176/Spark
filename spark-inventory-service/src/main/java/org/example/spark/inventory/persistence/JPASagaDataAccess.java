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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class JPASagaDataAccess implements SagaDataAccess {

	private final JPAItemDataAccess itemDataAccess;

	private final EntityManagerFactory entityManagerFactory;

	public JPASagaDataAccess(
		@Nonnull JPAItemDataAccess itemDataAccess, @Nonnull EntityManagerFactory entityManagerFactory
	) {
		this.itemDataAccess = itemDataAccess;
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public SagaProperties[] getSagas() {
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
				try {
					return new SagaProperties(
						sagaEntity.getId(),
						sagaEntity.getIdempotenceToken(),
						sagaEntity.getState(),
						sagaEntity.getItem().getId(),
						(Class<? extends Saga>) Class.forName(sagaEntity.getClassName())
					);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return null;
				}
			})
			.filter(Objects::nonNull)
			.toArray(SagaProperties[]::new);
	}

	@Override
	public SagaProperties newSaga(
		@Nonnull ItemAggregate item,
		int initialState,
		@Nonnull String idempotenceToken,
		@Nonnull Class<? extends Saga> clazz
	) {
		AtomicReference<SagaProperties> atomicSaga = new AtomicReference<>();

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

				SagaProperties sagaProperties = new SagaProperties(
					sagaEntity.getId(),
					sagaEntity.getIdempotenceToken(),
					sagaEntity.getState(),
					sagaEntity.getItem().getId(),
					clazz
				);
				atomicSaga.set(sagaProperties);
				return;
			}

			ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
			entityManager.persist(processedMessage);

			VersionedItemAggregate versionedItem = itemDataAccess.getVersionedItem(item.getId());
			versionedItem.item().setStatus(ItemAggregate.Status.BUSY);
			itemDataAccess.persist(versionedItem.item(), versionedItem.version(),null);

			SagaEntity saga = new SagaEntity(initialState, entityManager.find(ItemEntity.class, item.getId()), clazz);
			entityManager.persist(saga);

			SagaProperties sagaProperties = new SagaProperties(
				saga.getId(),
				saga.getIdempotenceToken(),
				saga.getState(),
				saga.getItem().getId(),
				clazz
			);
			atomicSaga.set(sagaProperties);
		});

		return atomicSaga.get();
	}

	@Override
	public String updateState(long sagaId, int newState) {
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
