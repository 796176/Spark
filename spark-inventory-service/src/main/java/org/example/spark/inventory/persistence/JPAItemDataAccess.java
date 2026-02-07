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
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.example.spark.inventory.aggregates.ItemAggregate;
import org.example.spark.inventory.aggregates.ItemAggregateImpl;
import org.example.spark.inventory.controllers.ItemEventConverter;
import org.example.spark.inventory.events.ItemCreated;
import org.example.spark.inventory.events.ItemCreatedImpl;
import org.example.spark.inventory.events.ItemEvent;
import org.example.spark.inventory.interactors.ItemDataAccess;
import org.example.spark.inventory.models.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class JPAItemDataAccess implements ItemDataAccess {

	private final EntityManagerFactory entityManagerFactory;

	private final ItemEventConverter<String> converter;

	public JPAItemDataAccess(
		@Nonnull EntityManagerFactory entityManagerFactory,
	 	@Nonnull ItemEventConverter<String> converter
	) {
		this.entityManagerFactory = entityManagerFactory;
		this.converter = converter;
	}

	@Override
	public ItemAggregate getItem(long id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		ItemEntity item = entityManager.find(ItemEntity.class, id);
		entityManager.clear();
		entityManager.close();
		return toItemAggregate(item);
	}

	@Override
	public ItemAggregate[] getItems() {
		// SELECT * FROM items;
		CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> q = cb.createQuery(ItemEntity.class);
		Root<ItemEntity> item = q.from(ItemEntity.class);
		q.select(item);

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		TypedQuery<ItemEntity> typedQuery = entityManager.createQuery(q);
		List<ItemEntity> itemEntityList = typedQuery.getResultList();
		entityManager.clear();
		entityManager.close();

		return itemEntityList.stream().map(this::toItemAggregate).toArray(ItemAggregate[]::new);
	}

	private ItemAggregate toItemAggregate(ItemEntity item) {
		return new ItemAggregateImpl(
			item.getId(),
			item.getName(),
			new Money(item.getEmbeddablePrice().getCurrencyAmount(), item.getEmbeddablePrice().getCentAmount()),
			item.getAmount(),
			ItemAggregate.Status.fromId(item.getItemStatus().getId())
		);
	}

	@Override
	public void persist(
		@Nonnull ItemAggregate item, @Nullable String idempotenceToken, @Nonnull ItemEvent... itemEvents
	) {
		entityManagerFactory.runInTransaction(entityManager -> {
			if (idempotenceToken != null) {
				ProcessedMessage processedMessage = entityManager.find(ProcessedMessage.class, idempotenceToken);
				if (processedMessage != null) return;
				processedMessage = new ProcessedMessage(idempotenceToken);
				entityManager.persist(processedMessage);
			}

			ItemEntity itemEntity = entityManager.find(ItemEntity.class, item.getId());
			itemEntity.setAmount(item.getAmount());
			itemEntity.setItemStatus(entityManager.find(ItemStatus.class, item.getStatus().getId()));

			for (ItemEvent event: itemEvents) {
				ItemEventConverter<String>.EncodedEventProperties properties = converter.convert(event);
				EventEntity eventEntity = new EventEntity(
					event.getType(), properties.getContentType(), properties.getVersion(), properties.getBody()
				);
				entityManager.persist(eventEntity);
			}
		});
	}

	@Override
	public ItemAggregate addItem(@Nonnull String name, @Nonnull String idempotenceToken, @Nonnull Money price, int amount) {
		AtomicLong atomicLong = new AtomicLong();
		entityManagerFactory.runInTransaction(entityManager -> {
			if (entityManager.find(ProcessedMessage.class, idempotenceToken) != null) {
				//SELECT id FROM items WHERE item.name = name;
				CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
				CriteriaQuery<Long> q = cb.createQuery(Long.class);
				Root<ItemEntity> account = q.from(ItemEntity.class);
				q.where(cb.equal(account.get(ItemEntity_.name), name));
				q.select(account.get(ItemEntity_.id));
				TypedQuery<Long> typedQuery = entityManager.createQuery(q);
				long accountEntityId = typedQuery.getSingleResultOrNull();
				atomicLong.set(accountEntityId);
				return;
			}

			ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
			entityManager.persist(processedMessage);

			ItemEntity item = new ItemEntity(
				name, price, amount, entityManager.find(ItemStatus.class, ItemAggregate.Status.CREATED.getId())
			);
			entityManager.persist(item);
			atomicLong.set(item.getId());

			ItemCreated itemCreated = new ItemCreatedImpl(item.getId(), name, amount, price);
			ItemEventConverter<String>.EncodedEventProperties properties = converter.convert(itemCreated);
			EventEntity event = new EventEntity(
				itemCreated.getType(), properties.getContentType(), properties.getVersion(), properties.getBody()
			);
			entityManager.persist(event);
		});

		return new ItemAggregateImpl(atomicLong.get(), name, price, amount, ItemAggregate.Status.CREATED);
	}
}
