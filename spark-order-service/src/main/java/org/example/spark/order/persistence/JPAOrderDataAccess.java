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
import org.example.spark.order.aggregates.OrderAggregateImpl;
import org.example.spark.order.aggregates.VersionedOrderAggregate;
import org.example.spark.order.converters.OrderEventConverter;
import org.example.spark.order.events.OrderEvent;
import org.example.spark.order.interactors.OrderDataAccess;
import org.example.spark.order.interactors.SagaDataAccess;
import org.example.spark.order.models.*;
import org.example.spark.order.sagas.Saga;
import org.example.spark.order.sagas.SagaTypes;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = false)
public class JPAOrderDataAccess implements OrderDataAccess {

	private final EntityManager entityManager;

	private final OrderEventConverter<String> converter;

	public JPAOrderDataAccess(@Nonnull EntityManager entityManager, @Nonnull OrderEventConverter<String> converter) {
		this.entityManager = entityManager;
		this.converter = converter;
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
	@Override
	public OrderAggregate getOrder(long id) {
		OrderEntity order = entityManager.find(OrderEntity.class, id);
		return toOrderAggregate(order);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
	@Override
	public VersionedOrderAggregate getVersionedOrder(long id) {
		OrderEntity order = entityManager.find(OrderEntity.class, id);
		return toVersionedOrderAggregate(order);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
	@Override
	public VersionedOrderAggregate[] getVersionedOrdersByAccount(long accountId) {
		// SELECT * FROM orders WHERE orders.account_id = accountId;
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<OrderEntity> q = cb.createQuery(OrderEntity.class);
		Root<OrderEntity> order = q.from(OrderEntity.class);
		q.where(cb.equal(order.get(OrderEntity_.accountId), accountId));
		q.select(order);

		TypedQuery<OrderEntity> typedQuery = entityManager.createQuery(q);
		List<OrderEntity> orderEntityList = typedQuery.getResultList();
		return orderEntityList.stream().map(this::toVersionedOrderAggregate).toArray(VersionedOrderAggregate[]::new);
	}

	private VersionedOrderAggregate toVersionedOrderAggregate(OrderEntity order) {
		return new VersionedOrderAggregate(toOrderAggregate(order), order.getVersion());
	}

	private OrderAggregate toOrderAggregate(OrderEntity order) {
		LineItem[] lineItems = order
			.getLineItems()
			.stream()
			.map(lineItemEntity -> new LineItem(lineItemEntity.getItemId(), lineItemEntity.getAmount()))
			.toArray(LineItem[]::new);
		return new OrderAggregateImpl(
			order.getId(),
			order.getAccountId(),
			order.getTimestamp(),
			lineItems,
			OrderAggregate.Status.fromId(order.getOrderStatus().getId())
		);
	}

	@Override
	public void persist(
		@Nonnull OrderAggregate order, long version, @Nullable String idempotenceToken, @Nonnull OrderEvent... events
	) {
		if (idempotenceToken != null) {
			if (entityManager.find(ProcessedMessage.class, idempotenceToken) != null) return;
			ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
			entityManager.persist(processedMessage);
		}

		OrderEntity orderEntity = entityManager.find(OrderEntity.class, order.getId());
		if (orderEntity.getVersion() != version) throw new IllegalStateException();
		orderEntity.setOrderStatus(entityManager.find(OrderStatus.class, order.getStatus().getId()));
		orderEntity.setVersion(orderEntity.getVersion() + 1);

		for (OrderEvent event: events) {
			OrderEventConverter<String>.EncodedEventProperties properties = converter.convert(event);
			EventEntity eventEntity = new EventEntity(
				event.getType(), properties.getContentType(), properties.getVersion(), properties.getBody()
			);
			entityManager.persist(eventEntity);
		}
	}

	@Override
	public Saga placeOrder(
		long accountId,
		long timestamp,
		@Nonnull String idempotenceToken,
		@Nonnull SagaDataAccess sagaDataAccess,
		@Nonnull LineItem... lineItems
	) {
		AtomicReference<OrderAggregate> returnedOrder = new AtomicReference<>();
		if (entityManager.find(ProcessedMessage.class, idempotenceToken) != null) {
			// SELECT * FROM orders WHERE orders.timestamp = timestamp;
			CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
			CriteriaQuery<OrderEntity> q = cb.createQuery(OrderEntity.class);
			Root<OrderEntity> order = q.from(OrderEntity.class);
			q.where(cb.equal(order.get(OrderEntity_.timestamp), timestamp));
			q.select(order);

			TypedQuery<OrderEntity> typedQuery = entityManager.createQuery(q);
			OrderEntity orderEntity = typedQuery.getSingleResultOrNull();
			return sagaDataAccess.getSagaByOrder(toOrderAggregate(orderEntity));
		}

		ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
		entityManager.persist(processedMessage);

		OrderEntity orderEntity = new OrderEntity(
			accountId,
			timestamp,
			List.of(),
			entityManager.find(OrderStatus.class, OrderAggregate.Status.PLACING.getId())
		);
		ArrayList<LineItemEntity> lineItemEntities = new ArrayList<>(lineItems.length);
		for (LineItem lineItem: lineItems) {
			lineItemEntities.add(new LineItemEntity(orderEntity, lineItem.itemId(), lineItem.amount()));
		}
		orderEntity.setLineItems(lineItemEntities);
		entityManager.persist(orderEntity);
		returnedOrder.set(toOrderAggregate(orderEntity));

		try {
			return sagaDataAccess.newSaga(returnedOrder.get(), SagaTypes.PLACE_ORDERED, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Saga restoreOrder(
		@Nonnull OrderAggregate order,
		long version,
		@Nonnull String idempotenceToken,
		@Nonnull SagaDataAccess sagaDataAccess
	) {
		if (entityManager.find(ProcessedMessage.class, idempotenceToken) != null) {
			return sagaDataAccess.getSagaByOrder(order);
		}

		ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
		entityManager.persist(processedMessage);

		order.setTransientStatus(OrderAggregate.Status.PLACING);
		persist(order, version, null);

		try {
			return sagaDataAccess.newSaga(order, SagaTypes.PLACE_ORDERED, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
