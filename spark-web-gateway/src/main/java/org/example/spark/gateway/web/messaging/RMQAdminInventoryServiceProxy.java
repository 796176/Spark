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
import jakarta.annotation.Nullable;
import org.example.spark.gateway.web.controllers.MessageDispatcher;
import org.example.spark.gateway.web.converters.InventoryServiceCommandEncoder;
import org.example.spark.gateway.web.converters.RoleEncoder;
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.Money;
import org.example.spark.gateway.web.models.RemoteCallResult;
import org.example.spark.gateway.web.proxies.AdminInventoryServiceProxy;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RMQAdminInventoryServiceProxy implements AdminInventoryServiceProxy {

	private final MessageDispatcher messageDispatcher;

	private final RMQConsumer rmqConsumer;

	private final InventoryServiceCommandEncoder inventoryServiceCommandEncoder;

	public RMQAdminInventoryServiceProxy(
		@Nonnull MessageDispatcher messageDispatcher,
		@Nonnull RMQConsumer rmqConsumer,
		@Nonnull InventoryServiceCommandEncoder inventoryServiceCommandEncoder
	) {
		this.messageDispatcher = messageDispatcher;
		this.rmqConsumer = rmqConsumer;
		this.inventoryServiceCommandEncoder = inventoryServiceCommandEncoder;
	}

	@Override
	public void getItems(
		@Nonnull Account account, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws IOException, InterruptedException {
		String correlationId = UUID.randomUUID().toString();
		InventoryServiceCommandEncoder.EncodedCommand command =
			inventoryServiceCommandEncoder.encodeGettingItemsCommand();
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		rmqConsumer.register(correlationId, callResultConsumer);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.replyTo("spark-web-gateway")
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.inventory.get-items")
			.contentType(command.contentType())
			.headers(
				Map.of(
					"Version", command.version(),
					"Caller-Roles", encodedRoles,
					"Caller-Id", Long.toString(account.getId())
				)
			)
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-inventory-service", properties, command.body()
		);
	}

	@Override
	public void getItem(
		@Nonnull Account account, long itemId, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws IOException, InterruptedException {
		String correlationId = UUID.randomUUID().toString();
		InventoryServiceCommandEncoder.EncodedCommand command =
			inventoryServiceCommandEncoder.encodeGettingItemCommand(itemId);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		rmqConsumer.register(correlationId, callResultConsumer);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.replyTo("spark-web-gateway")
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.inventory.get-item")
			.contentType(command.contentType())
			.headers(
				Map.of(
					"Version", command.version(),
					"Caller-Roles", encodedRoles,
					"Caller-Id", Long.toString(account.getId())
				)
			)
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-inventory-service", properties, command.body()
		);
	}

	@Override
	public void addItem(
		@Nonnull Account account,
		@Nonnull String name,
		@Nonnull Money price,
		int amount,
		@Nullable String pictureName,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws IOException, InterruptedException {
		String correlationId = UUID.randomUUID().toString();
		InventoryServiceCommandEncoder.EncodedCommand command =
			inventoryServiceCommandEncoder.encodeAddingItemCommand(name, price, amount, pictureName);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		rmqConsumer.register(correlationId, callResultConsumer);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.replyTo("spark-web-gateway")
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.inventory.add-item")
			.contentType(command.contentType())
			.headers(
				Map.of(
					"Version", command.version(),
					"Caller-Roles", encodedRoles,
					"Caller-Id", Long.toString(account.getId())
				)
			)
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-inventory-service", properties, command.body()
		);
	}

	@Override
	public void deleteItem(
		@Nonnull Account account, long itemId, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws IOException, InterruptedException {
		String correlationId = UUID.randomUUID().toString();
		InventoryServiceCommandEncoder.EncodedCommand command =
			inventoryServiceCommandEncoder.encodeDeletingItemCommand(itemId);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		rmqConsumer.register(correlationId, callResultConsumer);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.replyTo("spark-web-gateway")
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.inventory.delete-item")
			.contentType(command.contentType())
			.headers(
				Map.of(
					"Version", command.version(),
					"Caller-Roles", encodedRoles,
					"Caller-Id", Long.toString(account.getId())
				)
			)
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-inventory-service", properties, command.body()
		);
	}

	@Override
	public void updateItemAmount(
		@Nonnull Account account,
		long itemId,
		int amount,
		@Nonnull String version,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws IOException, InterruptedException {
		String correlationId = UUID.randomUUID().toString();
		InventoryServiceCommandEncoder.EncodedCommand command =
			inventoryServiceCommandEncoder.encodeUpdatingItemAmountCommand(itemId, amount, version);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		rmqConsumer.register(correlationId, callResultConsumer);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.replyTo("spark-web-gateway")
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.inventory.update-item-amount")
			.contentType(command.contentType())
			.headers(
				Map.of(
					"Version", command.version(),
					"Caller-Roles", encodedRoles,
					"Caller-Id", Long.toString(account.getId())
				)
			)
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-inventory-service", properties, command.body()
		);
	}
}
