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
import com.rabbitmq.client.Channel;
import jakarta.annotation.Nonnull;
import org.example.spark.gateway.web.converters.OrderServiceCommandEncoder;
import org.example.spark.gateway.web.converters.RoleEncoder;
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.RemoteCallResult;
import org.example.spark.gateway.web.proxies.AdminOrderServiceProxy;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RMQAdminOrderServiceProxy implements AdminOrderServiceProxy {

	private final Channel channel;

	private final RMQConsumer rmqConsumer;

	private final OrderServiceCommandEncoder orderServiceCommandEncoder;

	public RMQAdminOrderServiceProxy(
		@Nonnull Channel channel,
		@Nonnull RMQConsumer rmqConsumer,
		@Nonnull OrderServiceCommandEncoder orderServiceCommandEncoder
	) {
		this.channel = channel;
		this.rmqConsumer = rmqConsumer;
		this.orderServiceCommandEncoder = orderServiceCommandEncoder;
	}

	@Override
	public synchronized void getOrders(
		@Nonnull Account account, long accountId, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception {
		String correlationId = UUID.randomUUID().toString();
		rmqConsumer.register(correlationId, callResultConsumer);
		String roles = RoleEncoder.encode(account.getRoles());
		OrderServiceCommandEncoder.EncodedCommand encodedCommand =
			orderServiceCommandEncoder.getOrders(accountId);
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
		channel.basicPublish("commands", "spark-order-service", properties, encodedCommand.body());
		channel.waitForConfirms();
	}

	@Override
	public synchronized void getOrder(
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
		channel.basicPublish("commands", "spark-order-service", properties, encodedCommand.body());
		channel.waitForConfirms();
	}

	@Override
	public void rejectOrder(
		@Nonnull Account account,
		long orderId,
		@Nonnull String version,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws InterruptedException, IOException {
		String correlationId = UUID.randomUUID().toString();
		OrderServiceCommandEncoder.EncodedCommand command =
			orderServiceCommandEncoder.encodeRejectingOrderCommand(orderId, version);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		rmqConsumer.register(correlationId, callResultConsumer);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.order.reject-order")
			.contentType(command.contentType())
			.replyTo("spark-web-gateway")
			.headers(Map.of(
				"Version", command.version(),
				"Caller-Roles", encodedRoles,
				"Caller-Id", Long.toString(account.getId())
			))
			.build();
		channel.basicPublish("commands", "spark-order-service", properties, command.body());
		channel.waitForConfirms();
	}

	@Override
	public void acceptOrder(
		@Nonnull Account account,
		long orderId,
		@Nonnull String version,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws InterruptedException, IOException {
		String correlationId = UUID.randomUUID().toString();
		OrderServiceCommandEncoder.EncodedCommand command =
			orderServiceCommandEncoder.encodeAcceptingOrderCommand(orderId, version);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		rmqConsumer.register(correlationId, callResultConsumer);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.order.accept-order")
			.contentType(command.contentType())
			.replyTo("spark-web-gateway")
			.headers(Map.of(
				"Version", command.version(),
				"Caller-Roles", encodedRoles,
				"Caller-Id", Long.toString(account.getId())
			))
			.build();
		channel.basicPublish("commands", "spark-order-service", properties, command.body());
		channel.waitForConfirms();
	}
}
