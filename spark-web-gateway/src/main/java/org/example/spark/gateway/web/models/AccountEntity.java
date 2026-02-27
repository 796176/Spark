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
import jakarta.persistence.*;

import java.util.Collection;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "accounts_replica")
public class AccountEntity {

	private long id;

	private String name;

	private String encodedPassword;

	private Collection<AssignedRole> assignedRoles;

	private AccountStatus accountStatus;

	public AccountEntity() { }

	public AccountEntity(
		long id,
		@Nonnull String name,
		@Nonnull String encodedPassword,
		@Nonnull Collection<AssignedRole> assignedRoles,
		@Nonnull AccountStatus status
	) {
		this();
		this.setId(id);
		this.setName(name);
		this.setEncodedPassword(encodedPassword);
		this.setAssignedRoles(assignedRoles);
		this.setAccountStatus(status);
	}


	@Id
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	public String getEncodedPassword() {
		return encodedPassword;
	}

	public void setEncodedPassword(@Nonnull String encodedPassword) {
		this.encodedPassword = encodedPassword;
	}


	@OneToMany(mappedBy = "accountEntity", cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
	public Collection<AssignedRole> getAssignedRoles() {
		return assignedRoles;
	}

	public void setAssignedRoles(@Nonnull Collection<AssignedRole> assignedRoles) {
		this.assignedRoles = assignedRoles;
	}

	@ManyToOne
	public AccountStatus getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(@Nonnull AccountStatus accountStatus) {
		this.accountStatus = accountStatus;
	}
}
