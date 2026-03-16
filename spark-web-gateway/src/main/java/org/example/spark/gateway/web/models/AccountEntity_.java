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

import jakarta.persistence.metamodel.*;

@StaticMetamodel(AccountEntity.class)
public class AccountEntity_ {
	public static volatile EntityType<AccountEntity> class_;

	public static volatile String ID = "id";
	public static volatile String NAME = "name";
	public static volatile String ENCODED_PASSWORD = "encodedPassword";
	public static volatile String ASSIGNED_ROLES = "assignedRoles";
	public static volatile String ACCOUNT_STATUS = "accountStatus";

	public static volatile SingularAttribute<AccountEntity, Long> id;
	public static volatile SingularAttribute<AccountEntity, String> name;
	public static volatile SingularAttribute<AccountEntity, String> encodedPassword;
	public static volatile CollectionAttribute<AccountEntity, AssignedRole> assignedRoles;
	public static volatile SingularAttribute<AccountEntity, AccountStatus> accountStatus;
}
