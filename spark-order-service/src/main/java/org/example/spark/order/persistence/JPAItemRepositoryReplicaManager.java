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
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.example.spark.order.interactors.ItemRepositoryReplicaManager;
import org.example.spark.order.models.ItemEntity;
import org.example.spark.order.models.PendingReductionItem;
import org.example.spark.order.models.PendingReductionItem_;
import org.example.spark.order.models.ProcessedMessage;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(isolation = Isolation.SERIALIZABLE)
public class JPAItemRepositoryReplicaManager implements ItemRepositoryReplicaManager {

	private final EntityManagerFactory entityManagerFactory;

	public JPAItemRepositoryReplicaManager(@Nonnull EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public void addNewItem(long itemId, int amount, @Nonnull String idempotenceToken) {
		entityManagerFactory.runInTransaction(entityManager -> {
			if (entityManager.find(ProcessedMessage.class, idempotenceToken) != null) return;

			ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
			entityManager.persist(processedMessage);

			ItemEntity itemEntity = new ItemEntity(itemId, amount);
			entityManager.persist(itemEntity);
		});
	}

	@Override
	public void updateAmount(long itemId, int delta, @Nonnull String idempotenceToken) {
		entityManagerFactory.runInTransaction(entityManager -> {
			if (entityManager.find(ProcessedMessage.class, idempotenceToken) != null) return;

			ProcessedMessage processedMessage = new ProcessedMessage(idempotenceToken);
			entityManager.persist(processedMessage);

			ItemEntity item = entityManager.find(ItemEntity.class, itemId);
			if (item == null) {
				System.out.println("log error");
				return;
			}
			item.setAmount(item.getAmount() + delta);

			if (delta < 0) {
				// SELECT * FROM pending_reduction_items ORDER BY id ASC;
				CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
				CriteriaQuery<PendingReductionItem> q = cb.createQuery(PendingReductionItem.class);
				Root<PendingReductionItem> pendingReductionItem = q.from(PendingReductionItem.class);
				q.where(cb.equal(pendingReductionItem.get(PendingReductionItem_.amount), Math.abs(delta)));
				q.orderBy(cb.asc(pendingReductionItem.get(PendingReductionItem_.id)));
				q.select(pendingReductionItem);

				TypedQuery<PendingReductionItem> typedQuery = entityManager.createQuery(q);
				typedQuery.setMaxResults(1);
				PendingReductionItem result = typedQuery.getSingleResultOrNull();
				if (result != null) entityManager.remove(result);
			}
		});
	}

	@Override
	public void deleteItem(long itemId) {
		entityManagerFactory.runInTransaction(entityManager -> {
			ItemEntity item = entityManager.find(ItemEntity.class, itemId);
			if (item == null) {
				System.out.println("log error");
				return;
			}
			entityManager.remove(item);
		});
	}
}
