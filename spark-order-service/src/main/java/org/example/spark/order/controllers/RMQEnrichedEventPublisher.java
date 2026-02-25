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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Nonnull;

import java.util.Map;

public class RMQEnrichedEventPublisher implements EnrichedEventPublisher {

	private final Channel channel;

	public RMQEnrichedEventPublisher(@Nonnull Channel channel) {
		this.channel = channel;
	}

	@Override
	public void publish(
		@Nonnull String eventType,
		@Nonnull String contentType,
		@Nonnull String version,
		@Nonnull String messageId,
		@Nonnull byte[] body
	) throws Exception {
		do {
			AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
				.deliveryMode(2)
				.contentType(contentType)
				.type(eventType)
				.messageId(messageId)
				.headers(Map.of("Version", version))
				.build();
			channel.basicPublish("spark-order-service-enriched-events", "", properties, body);
		} while (!channel.waitForConfirms());
	}
}
