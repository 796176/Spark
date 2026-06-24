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
import com.rabbitmq.client.Connection;
import jakarta.annotation.Nonnull;
import org.example.spark.authorization.Role;
import org.example.spark.inventory.sagas.OrderServiceProxy;
import org.example.spark.inventory.sagas.SagaState;

import java.util.Map;

public class RMQOrderService implements OrderServiceProxy {

	private final Connection connection;

	private final String replyChannel;

	public RMQOrderService(@Nonnull Connection connection, @Nonnull String replyChannel) {
		this.connection = connection;
		this.replyChannel = replyChannel;
	}

	@Override
	public boolean invalidateItem(
		@Nonnull SagaState state, long itemId, @Nonnull String correlationId
	) throws Exception {
		String encodedRole = Long.toString(Role.SERVICE.getId());
		AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.contentType("application/json")
			.correlationId(correlationId)
			.messageId(state.getIdempotenceToken())
			.type("org.example.spark.order.invalidate-item")
			.replyTo(replyChannel)
			.headers(Map.of("Version", "1.0", "Caller-Id", "-1", "Caller-Roles", encodedRole))
			.build();
		try (Channel channel = connection.createChannel()) {
			channel.confirmSelect();
			// TODO decouple this service proxy from command formating
			channel.basicPublish(
				"commands",
				"spark-order-service",
				basicProperties,
				("{\"item_id\": \"" + itemId + "\"}").getBytes()
			);
			channel.waitForConfirms();
			return false;
		}
	}
}
