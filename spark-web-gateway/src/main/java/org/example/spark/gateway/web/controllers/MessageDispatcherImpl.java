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

package org.example.spark.gateway.web.controllers;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class MessageDispatcherImpl implements MessageDispatcher, Runnable, AutoCloseable {

	private record Message(String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) { }

	private final Queue<Message> queue = new ConcurrentLinkedQueue<>();

	private final AtomicReference<CountDownLatch> confirmLatch = new AtomicReference<>(new CountDownLatch(1));

	private final Channel channel;

	private final ScheduledExecutorService scheduledExecutorService;

	public MessageDispatcherImpl(@Nonnull Channel channel) {
		this.channel = channel;
		this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleWithFixedDelay(this, 500, 50, TimeUnit.MILLISECONDS);
	}

	@Override
	public void blockingSend(
		@Nonnull String exchange,
		@Nonnull String routingKey,
		@Nonnull AMQP.BasicProperties properties,
		@Nonnull byte[] body
	) throws InterruptedException {
		CountDownLatch localConfirmLatch;
		do {
			queue.add(new Message(exchange, routingKey, properties, body));
			localConfirmLatch = confirmLatch.get();
		} while (waitIfNull(localConfirmLatch));
		if (!localConfirmLatch.await(200, TimeUnit.MILLISECONDS)) {
			throw new InterruptedException();
		}
	}

	private boolean waitIfNull(Object o) throws InterruptedException {
		if (o == null) {
			Thread.sleep(5);
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		try {
			CountDownLatch currentLatch = confirmLatch.get();
			confirmLatch.set(null);
			Message[] messages;
			do {
				messages = queue.toArray(new Message[0]);
				for (Message m : messages) {
					channel.basicPublish(m.exchange, m.routingKey, m.properties, m.body);
				}
			} while (!channel.waitForConfirms());
			queue.removeAll(Arrays.stream(messages).toList());
			confirmLatch.set(new CountDownLatch(1));
			currentLatch.countDown();
		} catch (Exception ignored) { }
	}

	@Override
	public void close() throws Exception {
		scheduledExecutorService.shutdown();
		scheduledExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
	}
}
