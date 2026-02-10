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

package org.example.spark.inventory.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import org.example.spark.inventory.sagas.Saga;

import java.util.UUID;

@Entity
@Table(name = "sagas")
public class SagaEntity {

	private long id;

	private String idempotenceToken;

	private int state;

	private ItemEntity item;

	private String className;

	public SagaEntity() { }

	public SagaEntity(int state, @Nonnull ItemEntity item, @Nonnull Class<? extends Saga> clazz) {
		this();
		this.setIdempotenceToken(UUID.randomUUID().toString());
		this.setState(state);
		this.setItem(item);
		this.setClassName(clazz.getName());
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getIdempotenceToken() {
		return idempotenceToken;
	}

	public void setIdempotenceToken(@Nonnull String idempotenceToken) {
		this.idempotenceToken = idempotenceToken;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	@OneToOne
	public ItemEntity getItem() {
		return item;
	}

	public void setItem(@Nonnull ItemEntity item) {
		this.item = item;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(@Nonnull String className) {
		this.className = className;
	}

	public void generateNewIdempotenceToken() {
		setIdempotenceToken(UUID.randomUUID().toString());
	}
}
