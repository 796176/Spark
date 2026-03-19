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
import org.example.spark.authorization.Role;
import org.example.spark.gateway.web.converters.AccountServiceCommandEncoder;
import org.example.spark.gateway.web.converters.RoleEncoder;
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.RemoteCallResult;
import org.example.spark.gateway.web.proxies.UserAccountServiceProxy;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RMQUserAccountServiceProxy implements UserAccountServiceProxy {

	private final Channel channel;

	private final RMQConsumer rmqConsumer;

	private final AccountServiceCommandEncoder accountServiceCommandEncoder;

	public RMQUserAccountServiceProxy(
		@Nonnull Channel channel,
		@Nonnull RMQConsumer rmqConsumer,
		@Nonnull AccountServiceCommandEncoder accountServiceCommandEncoder
	) {
		this.channel = channel;
		this.rmqConsumer = rmqConsumer;
		this.accountServiceCommandEncoder = accountServiceCommandEncoder;
	}

	@Override
	public synchronized void createAccount(
		@Nonnull String name,
		@Nonnull String password,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer,
		@Nonnull Role[] roles,
		long callerId
	) throws Exception {
		String correlationId = UUID.randomUUID().toString();
		rmqConsumer.register(correlationId, callResultConsumer);
		String encodedRoles = RoleEncoder.encode(roles);
		AccountServiceCommandEncoder.EncodedCommand encodedCommand =
			accountServiceCommandEncoder.encodeCreatingAccountCommand(name, password);
		AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.replyTo("spark-web-gateway")
			.headers(
				Map.of(
					"Caller-Id", Long.toString(callerId),
					"Version", encodedCommand.version(),
					"Caller-Roles", encodedRoles
				)
			)
			.type("org.example.spark.account.create-account")
			.messageId(UUID.randomUUID().toString())
			.contentType(encodedCommand.contentType())
			.build();
		channel.basicPublish("commands", "spark-account-service", basicProperties, encodedCommand.body());
		channel.waitForConfirms();
	}

	@Override
	public synchronized void deleteAccount(
		@Nonnull Account account, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception {
		String correlationId = UUID.randomUUID().toString();
		rmqConsumer.register(correlationId, callResultConsumer);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		AccountServiceCommandEncoder.EncodedCommand encodedCommand =
			accountServiceCommandEncoder.encodeDeletingAccountCommand(account.getId());
		AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.replyTo("spark-web-gateway")
			.headers(
				Map.of(
					"Caller-Id", Long.toString(account.getId()),
					"Version", encodedCommand.version(),
					"Caller-Roles", encodedRoles
				)
			)
			.type("org.example.spark.account.delete-account")
			.messageId(UUID.randomUUID().toString())
			.contentType(encodedCommand.contentType())
			.build();
		channel.basicPublish("commands", "spark-account-service", basicProperties, encodedCommand.body());
		channel.waitForConfirms();
	}
}
