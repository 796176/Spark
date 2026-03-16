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

package org.example.spark.gateway.web.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "active_sessions")
public class SessionEntity {

	private String id;

	private AccountEntity accountEntity;

	public SessionEntity() { }

	public SessionEntity(@Nonnull String id, @Nonnull AccountEntity accountEntity) {
		this();
		this.setId(id);
		this.setAccountEntity(accountEntity);
	}

	@Id
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@ManyToOne
	public AccountEntity getAccountEntity() {
		return accountEntity;
	}

	public void setAccountEntity(@Nonnull AccountEntity accountEntity) {
		this.accountEntity = accountEntity;
	}
}
