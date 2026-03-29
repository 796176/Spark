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
import org.example.spark.order.converters.AuthorizingAccountMessageProcessor;
import org.example.spark.order.sagas.AccountServiceProxy;
import org.example.spark.order.sagas.SagaState;

import java.io.IOException;
import java.util.Map;

public class RMQAccountService implements AccountServiceProxy {

	private final Channel channel;

	private final String replyChannel;

	private final AuthorizingAccountMessageProcessor authorizingAccountMessageProcessor;

	public RMQAccountService(
		@Nonnull Channel channel,
		@Nonnull String replyChannel,
		@Nonnull AuthorizingAccountMessageProcessor authorizingAccountMessageProcessor
	) {
		this.channel = channel;
		this.replyChannel = replyChannel;
		this.authorizingAccountMessageProcessor = authorizingAccountMessageProcessor;
	}

	@Override
	public boolean authorizeAccount(
		@Nonnull SagaState state, long accountId, @Nonnull String correlationId
	) throws IOException, InterruptedException {
		AuthorizingAccountMessageProcessor.MessageDetails messageDetails =
			authorizingAccountMessageProcessor.createRequest(accountId);
		AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
			.deliveryMode(2)
			.correlationId(correlationId)
			.messageId(state.getIdempotenceToken())
			.replyTo(replyChannel)
			.headers(
				Map.of(
					"Version", messageDetails.getVersion(),
					"Caller-Id", "-1",
					"Caller-Roles", ""
				)
			)
			.contentType(messageDetails.getContentType())
			.type("org.example.spark.account.get-account-permissions")
			.build();
		channel.basicPublish("commands", "spark-account-service", properties, messageDetails.getBody());
		channel.waitForConfirms();
		return false;
	}
}
