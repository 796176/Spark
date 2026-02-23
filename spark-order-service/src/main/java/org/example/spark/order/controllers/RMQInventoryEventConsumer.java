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

import com.rabbitmq.client.*;
import jakarta.annotation.Nonnull;

import java.io.IOException;

public class RMQInventoryEventConsumer implements Consumer {

	private final InventoryEventListener inventoryEventListener;

	private final Channel acknowledgementChannel;

	public RMQInventoryEventConsumer(
		@Nonnull InventoryEventListener inventoryEventListener, @Nonnull Channel acknowledgementChannel
	) {
		this.inventoryEventListener = inventoryEventListener;
		this.acknowledgementChannel = acknowledgementChannel;
	}

	@Override
	public void handleConsumeOk(String s) { }

	@Override
	public void handleCancelOk(String s) { }

	@Override
	public void handleCancel(String s) { }

	@Override
	public void handleShutdownSignal(String s, ShutdownSignalException e) { }

	@Override
	public void handleRecoverOk(String s) { }

	@Override
	public void handleDelivery(
		String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes
	) throws IOException {
		try {
			inventoryEventListener.processEvent(
				basicProperties.getType(),
				basicProperties.getContentType(),
				basicProperties.getHeaders().get("Version").toString(),
				bytes,
				basicProperties.getMessageId()
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		acknowledgementChannel.basicAck(envelope.getDeliveryTag(), false);
	}
}
