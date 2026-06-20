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

package org.example.spark.gateway.web.messaging;

import com.rabbitmq.client.AMQP;
import jakarta.annotation.Nonnull;
import org.example.spark.authorization.Role;
import org.example.spark.gateway.web.controllers.MessageDispatcher;
import org.example.spark.gateway.web.converters.InventoryServiceCommandEncoder;
import org.example.spark.gateway.web.converters.RoleEncoder;
import org.example.spark.gateway.web.models.RemoteCallResult;
import org.example.spark.gateway.web.proxies.UserInventoryServiceProxy;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RMQUserInventoryServiceProxy implements UserInventoryServiceProxy {

	private final MessageDispatcher messageDispatcher;

	private final RMQConsumer rmqConsumer;

	private final InventoryServiceCommandEncoder inventoryServiceCommandEncoder;

	public RMQUserInventoryServiceProxy(
		@Nonnull MessageDispatcher messageDispatcher,
		@Nonnull RMQConsumer rmqConsumer,
		@Nonnull InventoryServiceCommandEncoder inventoryServiceCommandEncoder
	) {
		this.messageDispatcher = messageDispatcher;
		this.rmqConsumer = rmqConsumer;
		this.inventoryServiceCommandEncoder = inventoryServiceCommandEncoder;
	}

	@Override
	public void getInventory(
		long callerId, @Nonnull Role[] roles, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception {
		String correlationId = UUID.randomUUID().toString();
		rmqConsumer.register(correlationId, callResultConsumer);
		InventoryServiceCommandEncoder.EncodedCommand encodedCommand =
			inventoryServiceCommandEncoder.encodeGettingItemsCommand();
		String encodedRoles = RoleEncoder.encode(roles);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.contentType(encodedCommand.contentType())
			.type("org.example.spark.inventory.get-items")
			.headers(Map.of(
				"Caller-Id", Long.toString(callerId),
				"Version", encodedCommand.version(),
				"Caller-Roles", encodedRoles
			))
			.messageId(UUID.randomUUID().toString())
			.replyTo("spark-web-gateway")
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-inventory-service", properties, encodedCommand.body()
		);
	}
}
