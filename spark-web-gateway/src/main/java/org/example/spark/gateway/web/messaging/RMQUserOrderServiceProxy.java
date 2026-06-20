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
import org.example.spark.gateway.web.controllers.MessageDispatcher;
import org.example.spark.gateway.web.converters.OrderServiceCommandEncoder;
import org.example.spark.gateway.web.converters.RoleEncoder;
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.LineItem;
import org.example.spark.gateway.web.models.RemoteCallResult;
import org.example.spark.gateway.web.proxies.UserOrderServiceProxy;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RMQUserOrderServiceProxy implements UserOrderServiceProxy {

	private final MessageDispatcher messageDispatcher;

	private final RMQConsumer rmqConsumer;

	private final OrderServiceCommandEncoder orderServiceCommandEncoder;

	public RMQUserOrderServiceProxy(
		@Nonnull MessageDispatcher messageDispatcher,
		@Nonnull RMQConsumer rmqConsumer,
		@Nonnull OrderServiceCommandEncoder orderServiceCommandEncoder
	) {
		this.messageDispatcher = messageDispatcher;
		this.rmqConsumer = rmqConsumer;
		this.orderServiceCommandEncoder = orderServiceCommandEncoder;
	}

	@Override
	public void getOrders(
		@Nonnull Account account, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception {
		String correlationId = UUID.randomUUID().toString();
		rmqConsumer.register(correlationId, callResultConsumer);
		String roles = RoleEncoder.encode(account.getRoles());
		OrderServiceCommandEncoder.EncodedCommand encodedCommand =
			orderServiceCommandEncoder.getOrders(account.getId());
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.type("org.example.spark.order.get-orders-by-account")
			.replyTo("spark-web-gateway")
			.contentType(encodedCommand.contentType())
			.messageId(UUID.randomUUID().toString())
			.headers(
				Map.of(
					"Caller-Id", Long.toString(account.getId()),
					"Caller-Roles", roles,
					"Version", encodedCommand.version()
				)
			)
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-order-service", properties, encodedCommand.body()
		);
	}

	@Override
	public void getOrder(
		@Nonnull Account account, long orderId, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception {
		String correlationId = UUID.randomUUID().toString();
		rmqConsumer.register(correlationId, callResultConsumer);
		String roles = RoleEncoder.encode(account.getRoles());
		OrderServiceCommandEncoder.EncodedCommand encodedCommand =
			orderServiceCommandEncoder.getOrder(orderId);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.type("org.example.spark.order.get-order")
			.replyTo("spark-web-gateway")
			.contentType(encodedCommand.contentType())
			.messageId(UUID.randomUUID().toString())
			.headers(
				Map.of(
					"Caller-Id", Long.toString(account.getId()),
					"Caller-Roles", roles,
					"Version", encodedCommand.version()
				)
			)
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-order-service", properties, encodedCommand.body()
		);
	}

	@Override
	public void cancelOrder(
		@Nonnull Account account,
		long orderId,
		@Nonnull String version,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception {
		String correlationId = UUID.randomUUID().toString();
		rmqConsumer.register(correlationId, callResultConsumer);
		String roles = RoleEncoder.encode(account.getRoles());
		OrderServiceCommandEncoder.EncodedCommand encodedCommand =
			orderServiceCommandEncoder.cancelOrder(orderId, version);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.type("org.example.spark.order.cancel-order")
			.replyTo("spark-web-gateway")
			.contentType(encodedCommand.contentType())
			.messageId(UUID.randomUUID().toString())
			.headers(
				Map.of(
					"Caller-Id", Long.toString(account.getId()),
					"Caller-Roles", roles,
					"Version", encodedCommand.version()
				)
			)
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-order-service", properties, encodedCommand.body()
		);
	}

	@Override
	public void restoreOrder(
		@Nonnull Account account,
		long orderId,
		@Nonnull String version,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception {
		String correlationId = UUID.randomUUID().toString();
		rmqConsumer.register(correlationId, callResultConsumer);
		String roles = RoleEncoder.encode(account.getRoles());
		OrderServiceCommandEncoder.EncodedCommand encodedCommand =
			orderServiceCommandEncoder.restoreOrder(orderId, version);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.type("org.example.spark.order.restore-order")
			.replyTo("spark-web-gateway")
			.contentType(encodedCommand.contentType())
			.messageId(UUID.randomUUID().toString())
			.headers(
				Map.of(
					"Caller-Id", Long.toString(account.getId()),
					"Caller-Roles", roles,
					"Version", encodedCommand.version()
				)
			)
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-order-service", properties, encodedCommand.body()
		);
	}

	@Override
	public void placeOrder(
		@Nonnull Account account,
		long orderTimestamp,
		@Nonnull LineItem[] lineItems,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception {
		String correlationId = UUID.randomUUID().toString();
		rmqConsumer.register(correlationId, callResultConsumer);
		String roles = RoleEncoder.encode(account.getRoles());
		OrderServiceCommandEncoder.EncodedCommand encodedCommand =
			orderServiceCommandEncoder.placeOrder(account.getId(), orderTimestamp, lineItems);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.type("org.example.spark.order.place-order")
			.replyTo("spark-web-gateway")
			.contentType(encodedCommand.contentType())
			.messageId(UUID.randomUUID().toString())
			.headers(
				Map.of(
					"Caller-Id", Long.toString(account.getId()),
					"Caller-Roles", roles,
					"Version", encodedCommand.version()
				)
			)
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-order-service", properties, encodedCommand.body()
		);
	}
}
