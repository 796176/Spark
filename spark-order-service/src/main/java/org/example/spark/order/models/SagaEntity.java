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

package org.example.spark.order.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "sagas")
public class SagaEntity {

	private long id;

	private OrderEntity order;

	private String idempotenceToken;

	private long state;

	private String type;

	public SagaEntity() { }

	public SagaEntity(@Nonnull OrderEntity order, long stateId, @Nonnull String type) {
		this();
		this.setOrder(order);
		this.setIdempotenceToken(UUID.randomUUID().toString());
		this.setState(stateId);
		this.setType(type);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@OneToOne
	public OrderEntity getOrder() {
		return order;
	}

	public void setOrder(@Nonnull OrderEntity order) {
		this.order = order;
	}

	public String getIdempotenceToken() {
		return idempotenceToken;
	}

	public void setIdempotenceToken(@Nonnull String idempotenceToken) {
		this.idempotenceToken = idempotenceToken;
	}

	public long getState() {
		return state;
	}

	public void setState(long state) {
		this.state = state;
	}

	public String getType() {
		return type;
	}

	public void setType(@Nonnull String type) {
		this.type = type;
	}

	public void generateNewIdempotenceToken() {
		setIdempotenceToken(UUID.randomUUID().toString());
	}
}
