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
import org.example.spark.gateway.web.converters.AccountServiceCommandEncoder;
import org.example.spark.gateway.web.converters.RoleEncoder;
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.RemoteCallResult;
import org.example.spark.gateway.web.proxies.AdminAccountServiceProxy;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RMQAdminAccountServiceProxy implements AdminAccountServiceProxy {

	private final MessageDispatcher messageDispatcher;

	private final RMQConsumer rmqConsumer;

	private final AccountServiceCommandEncoder accountServiceCommandEncoder;

	public RMQAdminAccountServiceProxy(
		@Nonnull MessageDispatcher messageDispatcher,
		@Nonnull RMQConsumer rmqConsumer,
		@Nonnull AccountServiceCommandEncoder accountServiceCommandEncoder
	) {
		this.messageDispatcher = messageDispatcher;
		this.rmqConsumer = rmqConsumer;
		this.accountServiceCommandEncoder = accountServiceCommandEncoder;
	}

	@Override
	public void getAccounts(
		@Nonnull Account account, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws IOException, InterruptedException {
		String correlationId = UUID.randomUUID().toString();
		AccountServiceCommandEncoder.EncodedCommand command =
			accountServiceCommandEncoder.encodeGettingAccountsCommand();
		rmqConsumer.register(correlationId, callResultConsumer);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.account.get-accounts")
			.replyTo("spark-web-gateway")
			.contentType(command.contentType())
			.headers(Map.of(
				"Version", command.version(),
				"Caller-Roles", encodedRoles,
				"Caller-Id", Long.toString(account.getId())
			))
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-account-service", properties, command.body()
		);
	}

	@Override
	public void getAccount(
		@Nonnull Account account, long accountId, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws IOException, InterruptedException {
		String correlationId = UUID.randomUUID().toString();
		AccountServiceCommandEncoder.EncodedCommand command =
			accountServiceCommandEncoder.encodeGettingAccountCommand(accountId);
		rmqConsumer.register(correlationId, callResultConsumer);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.account.get-account")
			.replyTo("spark-web-gateway")
			.contentType(command.contentType())
			.headers(Map.of(
				"Version", command.version(),
				"Caller-Roles", encodedRoles,
				"Caller-Id", Long.toString(account.getId())
			))
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-account-service", properties, command.body()
		);
	}

	@Override
	public void createAccount(
		@Nonnull Account account,
		@Nonnull String name,
		@Nonnull String encodedPassword,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws IOException, InterruptedException {
		String correlationId = UUID.randomUUID().toString();
		AccountServiceCommandEncoder.EncodedCommand command =
			accountServiceCommandEncoder.encodeCreatingAccountCommand(name, encodedPassword);
		rmqConsumer.register(correlationId, callResultConsumer);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.account.create-account")
			.replyTo("spark-web-gateway")
			.contentType(command.contentType())
			.headers(Map.of(
				"Version", command.version(),
				"Caller-Roles", encodedRoles,
				"Caller-Id", Long.toString(account.getId())
			))
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-account-service", properties, command.body()
		);
	}

	@Override
	public void createAdministratorAccount(
		@Nonnull Account account,
		@Nonnull String name,
		@Nonnull String encodedPassword,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws IOException, InterruptedException {
		String correlationId = UUID.randomUUID().toString();
		AccountServiceCommandEncoder.EncodedCommand command =
			accountServiceCommandEncoder.encodeCreatingAdministratorAccountCommand(name, encodedPassword);
		rmqConsumer.register(correlationId, callResultConsumer);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.account.create-admin-account")
			.replyTo("spark-web-gateway")
			.contentType(command.contentType())
			.headers(Map.of(
				"Version", command.version(),
				"Caller-Roles", encodedRoles,
				"Caller-Id", Long.toString(account.getId())
			))
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-account-service", properties, command.body()
		);
	}

	@Override
	public void suspendAccount(
		@Nonnull Account account, long accountId, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws IOException, InterruptedException {
		String correlationId = UUID.randomUUID().toString();
		AccountServiceCommandEncoder.EncodedCommand command =
			accountServiceCommandEncoder.encodeSuspendingAccountCommand(accountId);
		rmqConsumer.register(correlationId, callResultConsumer);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.account.suspend-account")
			.replyTo("spark-web-gateway")
			.contentType(command.contentType())
			.headers(Map.of(
				"Version", command.version(),
				"Caller-Roles", encodedRoles,
				"Caller-Id", Long.toString(account.getId())
			))
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-account-service", properties, command.body()
		);
	}

	@Override
	public void restoreAccount(
		@Nonnull Account account, long accountId, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws InterruptedException, IOException {
		String correlationId = UUID.randomUUID().toString();
		AccountServiceCommandEncoder.EncodedCommand command =
			accountServiceCommandEncoder.encodeRestoringAccountCommand(accountId);
		rmqConsumer.register(correlationId, callResultConsumer);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.account.restore-account")
			.replyTo("spark-web-gateway")
			.contentType(command.contentType())
			.headers(Map.of(
				"Version", command.version(),
				"Caller-Roles", encodedRoles,
				"Caller-Id", Long.toString(account.getId())
			))
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-account-service", properties, command.body()
		);
	}

	@Override
	public void updateRoles(
		@Nonnull Account account,
		long accountId,
		@Nonnull Role[] roles,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws IOException, InterruptedException {
		String correlationId = UUID.randomUUID().toString();
		AccountServiceCommandEncoder.EncodedCommand command =
			accountServiceCommandEncoder.encodeUpdatingRolesCommand(accountId, roles);
		rmqConsumer.register(correlationId, callResultConsumer);
		String encodedRoles = RoleEncoder.encode(account.getRoles());
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.messageId(UUID.randomUUID().toString())
			.type("org.example.spark.account.change-account-roles")
			.replyTo("spark-web-gateway")
			.contentType(command.contentType())
			.headers(Map.of(
				"Version", command.version(),
				"Caller-Roles", encodedRoles,
				"Caller-Id", Long.toString(account.getId())
			))
			.build();
		messageDispatcher.blockingSend(
			"commands", "spark-account-service", properties, command.body()
		);
	}
}
