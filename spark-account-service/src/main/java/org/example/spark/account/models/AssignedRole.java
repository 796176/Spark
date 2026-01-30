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

package org.example.spark.account.models;

import jakarta.persistence.*;

@Entity
@Table(name = "assigned_roles")
@IdClass(AssignedRole.AssignedRolePK.class)
public class AssignedRole {

	public record AssignedRolePK(AccountEntity accountEntity, RoleEntity roleEntity) {}

	private AccountEntity accountEntity;

	private RoleEntity roleEntity;

	private boolean active;

	public AssignedRole() {}

	public AssignedRole(AccountEntity accountEntity, RoleEntity roleEntity, boolean isActive) {
		this();
		this.setAccountEntity(accountEntity);
		this.setRoleEntity(roleEntity);
		this.setActive(isActive);
	}

	@Id
	@ManyToOne
	public AccountEntity getAccountEntity() {
		return accountEntity;
	}

	public void setAccountEntity(AccountEntity accountEntity) {
		this.accountEntity = accountEntity;
	}

	@Id
	@ManyToOne
	public RoleEntity getRoleEntity() {
		return roleEntity;
	}

	public void setRoleEntity(RoleEntity roleEntity) {
		this.roleEntity = roleEntity;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean isActive) {
		active = isActive;
	}
}
