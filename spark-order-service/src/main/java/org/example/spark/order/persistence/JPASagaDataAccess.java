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

package org.example.spark.order.persistence;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.example.spark.order.aggregates.OrderAggregate;
import org.example.spark.order.interactors.OrderDataAccess;
import org.example.spark.order.interactors.SagaDataAccess;
import org.example.spark.order.models.*;
import org.example.spark.order.sagas.Saga;
import org.example.spark.order.sagas.SagaFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = false)
public class JPASagaDataAccess implements SagaDataAccess {

	private final EntityManager entityManagerFactory;

	private final SagaFactory sagaFactory;

	public JPASagaDataAccess(
		@Nonnull EntityManager entityManagerFactory,
		@Nonnull SagaFactory sagaFactory
	) {
		this.entityManagerFactory = entityManagerFactory;
		this.sagaFactory = sagaFactory;
	}

	@Override
	public Saga newSaga(@Nonnull OrderAggregate order, @Nonnull String sagaType, @Nullable String idempotenceToken) throws Exception {
		long stateId = sagaFactory.getInitialState(sagaType).getId();
		AtomicLong sagaId = new AtomicLong();
		AtomicReference<String> stateIdempotenceToken = new AtomicReference<>();
		var entityManager = entityManagerFactory;
		if (idempotenceToken != null) {
			if (entityManager.find(ProcessedMessage.class, idempotenceToken) != null) {
				// SELECT * FROM sagas WHERE sagas.order_id = orderId;
				CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
				CriteriaQuery<SagaEntity> q = cb.createQuery(SagaEntity.class);
				Root<SagaEntity> saga = q.from(SagaEntity.class);
				q.where(cb.equal(saga.get(SagaEntity_.ORDER).get(OrderEntity_.ID), order.getId()));
				q.select(saga);

				TypedQuery<SagaEntity> typedQuery = entityManager.createQuery(q);
				SagaEntity sagaEntity = typedQuery.getSingleResultOrNull();
				sagaId.set(sagaEntity.getId());
				stateIdempotenceToken.set(sagaEntity.getIdempotenceToken());
				return null;
			}
			ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
			entityManager.persist(processedMessage);
		}

		SagaEntity saga = new SagaEntity(entityManager.find(OrderEntity.class, order.getId()), stateId, sagaType);
		entityManager.persist(saga);
		sagaId.set(saga.getId());
		stateIdempotenceToken.set(saga.getIdempotenceToken());

		return sagaFactory.instantiateSaga(
			sagaId.get(), order, stateIdempotenceToken.get(), sagaType
		);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
	@Override
	public Saga[] getSagas(@Nonnull OrderDataAccess orderDataAccess) throws Exception {
		// SELECT * FROM sagas;
		CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
		CriteriaQuery<SagaEntity> q = cb.createQuery(SagaEntity.class);
		Root<SagaEntity> saga = q.from(SagaEntity.class);
		q.select(saga);

		TypedQuery<SagaEntity> typedQuery = entityManagerFactory.createQuery(q);
		List<SagaEntity> sagaEntityList = typedQuery.getResultList();
		Saga[] sagas = new Saga[sagaEntityList.size()];
		Iterator<SagaEntity> sagaEntityIterator = sagaEntityList.iterator();
		for (int i = 0; i < sagas.length; i++) {
			SagaEntity sagaEntity = sagaEntityIterator.next();
			sagas[i] = sagaFactory.instantiateSaga(
				sagaEntity.getId(),
				orderDataAccess.getOrder(sagaEntity.getOrder().getId()),
				sagaEntity.getIdempotenceToken(),
				sagaEntity.getType(),
				sagaEntity.getState()
			);
		}
		return sagas;
	}

	@Override
	public String updateSagaState(long sagaId, long sagaStateId) {
		SagaEntity saga = entityManagerFactory.find(SagaEntity.class, sagaId);
		saga.setState(sagaStateId);
		saga.generateNewIdempotenceToken();
		return saga.getIdempotenceToken();
	}

	@Override
	public void deleteSaga(long sagaId) {
		SagaEntity saga = entityManagerFactory.find(SagaEntity.class, sagaId);
		entityManagerFactory.remove(saga);
	}
}
