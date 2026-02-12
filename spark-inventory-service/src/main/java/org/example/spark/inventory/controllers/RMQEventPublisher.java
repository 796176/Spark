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

package org.example.spark.inventory.controllers;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Nonnull;
import org.example.spark.inventory.models.PublishableEvent;

import java.util.Map;

public class RMQEventPublisher implements EventPublisher {

	private final Channel channel;

	public RMQEventPublisher(@Nonnull Channel channel) {
		this.channel = channel;
	}

	@Override
	public void publish(@Nonnull PublishableEvent... publishableEvents) throws Exception {
		if (publishableEvents.length == 0) return;

		do {
			for (PublishableEvent event: publishableEvents) {
				AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
					.deliveryMode(2)
					.contentType(event.getContentType())
					.type(event.getEventType())
					.messageId(event.getEventId())
					.headers(Map.of("Version", event.getVersion()))
					.build();
				channel.basicPublish("spark-inventory-service-events", "", properties, event.getBody());
			}
		} while (!channel.waitForConfirms());
	}
}
