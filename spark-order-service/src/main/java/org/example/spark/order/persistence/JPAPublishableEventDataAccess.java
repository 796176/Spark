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
import jakarta.persistence.criteria.*;
import org.example.spark.order.interactors.PublishableEventDataAccess;
import org.example.spark.order.models.EventEntity;
import org.example.spark.order.models.EventEntity_;
import org.example.spark.order.models.PublishableEvent;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = false)
public class JPAPublishableEventDataAccess implements PublishableEventDataAccess {

	private final EntityManagerFactory entityManagerFactory;

	public JPAPublishableEventDataAccess(@Nonnull EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
	@Override
	public PublishableEvent[] retrieveInChronologicalOrder() {
		// SELECT * FROM outbox ORDER BY seqnum ASC;
		CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
		CriteriaQuery<EventEntity> q = cb.createQuery(EventEntity.class);
		Root<EventEntity> event = q.from(EventEntity.class);
		q.orderBy(cb.asc(event.get(EventEntity_.seqnum)));
		q.select(event);

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		TypedQuery<EventEntity> typedQuery = entityManager.createQuery(q);
		List<EventEntity> eventEntityList = typedQuery.getResultList();
		entityManager.clear();
		entityManager.close();

		return eventEntityList.stream().map(eventEntity -> {
				return new PublishableEvent(
					eventEntity.getEventType(),
					eventEntity.getContentType(),
					eventEntity.getVersion(),
					eventEntity.getEventId(),
					eventEntity.getEventBody().getBytes(StandardCharsets.UTF_8)
				);
			}
		).toArray(PublishableEvent[]::new);
	}

	@Override
	public void delete(@Nonnull PublishableEvent... events) {
		if (events.length == 0) return;

		// DELETE FROM outbox WHERE event_id = events[0].getEventId() OR ...;
		CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
		CriteriaDelete<EventEntity> q = cb.createCriteriaDelete(EventEntity.class);
		Root<EventEntity> eventEntity = q.from(EventEntity.class);
		Predicate[] predicates = new Predicate[events.length];
		for (int i = 0; i < predicates.length; i++) {
			predicates[i] = cb.equal(eventEntity.get(EventEntity_.eventId), events[i].getEventId());
		}
		q.where(cb.or(predicates));

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.createQuery(q).executeUpdate();
		});
	}
}
