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

package org.example.spark.authorization;

import java.util.Objects;

public class BasicAuthorizer {

	public static boolean isAdmin(Role[] roles) {
		if (roles == null) return false;
		for (Role role: roles) {
			if (Objects.equals(role, Role.ADMIN)) return true;
		}
		return false;
	}

	public static boolean hasRole(Role[] roles, Role role) {
		if (role == null || roles == null) return false;
		for (Role r: roles) {
			if (Objects.equals(r, role)) return true;
		}
		return false;
	}
}
