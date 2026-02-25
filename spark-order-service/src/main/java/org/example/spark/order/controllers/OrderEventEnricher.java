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

package org.example.spark.order.controllers;

import jakarta.annotation.Nonnull;
import org.example.spark.order.aggregates.OrderAggregate;
import org.example.spark.order.converters.EnrichedEventEncoder;
import org.example.spark.order.converters.OrderEventConverter;
import org.example.spark.order.events.OrderAccepted;
import org.example.spark.order.events.OrderCanceled;
import org.example.spark.order.events.OrderRejected;
import org.example.spark.order.interactors.OrderDataAccess;

public class OrderEventEnricher {

	private final OrderDataAccess orderDataAccess;

	private final OrderEventConverter<String> orderEventConverter;

	private final EnrichedEventPublisher enrichedEventPublisher;

	private final EnrichedEventEncoder enrichedEventEncoder;

	public OrderEventEnricher(
		@Nonnull OrderDataAccess orderDataAccess,
		@Nonnull OrderEventConverter<String> orderEventConverter,
		@Nonnull EnrichedEventPublisher enrichedEventPublisher,
		@Nonnull EnrichedEventEncoder enrichedEventEncoder
	) {
		this.orderDataAccess = orderDataAccess;
		this.orderEventConverter = orderEventConverter;
		this.enrichedEventPublisher = enrichedEventPublisher;
		this.enrichedEventEncoder = enrichedEventEncoder;
	}

	public void enrichEvent(
		@Nonnull String eventType,
		@Nonnull String contentType,
		@Nonnull String version,
		@Nonnull byte[] body,
		@Nonnull String messageId
	) throws Exception {
		OrderAggregate order;
		switch (eventType) {
			case "org.example.spark.order.order-accepted" -> {
				OrderAccepted event =
					orderEventConverter.convert(contentType, version, new String(body), OrderAccepted.class);
				order = orderDataAccess.getOrder(event.getOrderId());
			}
			case "org.example.spark.order.order-rejected" -> {
				OrderRejected event =
					orderEventConverter.convert(contentType, version, new String(body), OrderRejected.class);
				order = orderDataAccess.getOrder(event.getOrderId());
			}
			case "org.example.spark.order.order-canceled" -> {
				OrderCanceled event =
					orderEventConverter.convert(contentType, version, new String(body), OrderCanceled.class);
				order = orderDataAccess.getOrder(event.getOrderId());
			}
			case "org.example.spark.order.order-created" -> {
				enrichedEventPublisher.publish(eventType, contentType, version, messageId, body);
				return;
			}
			default -> {
				return;
			}
		}
		EnrichedEventEncoder.EncodedEnrichedEvent enrichedEvent = enrichedEventEncoder.encode(order);
		enrichedEventPublisher.publish(
			eventType, enrichedEvent.contentType(), enrichedEvent.version(), messageId, enrichedEvent.body()
		);
	}
}
