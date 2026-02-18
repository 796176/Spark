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
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.example.spark.order.interactors.ItemDataAccess;
import org.example.spark.order.models.*;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = false)
public class JPAItemDataAccess implements ItemDataAccess {

	private final EntityManagerFactory entityManagerFactory;

	public JPAItemDataAccess(@Nonnull EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
	@Override
	public Item getItem(long itemId) {
		// SELECT * FROM pending_reduction_items WHERE pending_reduction_items.item_id = itemId;
		CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
		CriteriaQuery<PendingReductionItem> q = cb.createQuery(PendingReductionItem.class);
		Root<PendingReductionItem> pendingReductionItem = q.from(PendingReductionItem.class);
		q.where(cb.equal(pendingReductionItem.get(PendingReductionItem_.item).get(ItemEntity_.id), itemId));
		q.select(pendingReductionItem);

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		ItemEntity item = entityManager.find(ItemEntity.class, itemId);
		TypedQuery<PendingReductionItem> typedQuery = entityManager.createQuery(q);
		List<PendingReductionItem> pendingReductionItemList = typedQuery.getResultList();
		entityManager.clear();
		entityManager.close();

		int itemActualAmount = item.getAmount();
		for (PendingReductionItem pri: pendingReductionItemList) {
			itemActualAmount -= pri.getAmount();
		}
		return new Item(itemId, itemActualAmount);
	}

	@Override
	public void reserve(@Nonnull Item[] items, @Nonnull String idempotenceToken) {
		entityManagerFactory.runInTransaction(entityManager -> {
			if (entityManager.find(ProcessedMessage.class, idempotenceToken) != null) return;

			ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
			entityManager.persist(processedMessage);

			for (Item item: items) {
				PendingReductionItem pendingReductionItem = new PendingReductionItem(entityManager.find(ItemEntity.class, item.itemId()), item.amount());
				entityManager.persist(pendingReductionItem);
			}
		});
	}
}
