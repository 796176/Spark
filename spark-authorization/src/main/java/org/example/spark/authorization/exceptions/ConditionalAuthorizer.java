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

package org.example.spark.authorization.exceptions;

import org.example.spark.authorization.Role;

public class ConditionalAuthorizer {

	public static abstract sealed class Condition {

		protected abstract boolean evaluate(long accountId, Role[] accountRoles);

		public ConditionalAuthorizer build() {
			return new ConditionalAuthorizer(this);
		}

		private final static class HasAnyRoles extends Condition {

			@Override
			protected boolean evaluate(long accountId, Role[] accountRoles) {
				return accountRoles.length != 0;
			}
		}

		private final static class HasSpecifiedRoles extends Condition {

			private final Role[] roles;

			private HasSpecifiedRoles(Role[] roles) {
				this.roles = roles;
			}

			@Override
			protected boolean evaluate(long accountId, Role[] accountRoles) {
				outer_for: for (Role role: roles) {
					for (Role accountRole: accountRoles) {
						if (accountRole == role) continue outer_for;
					}
					return false;
				}
				return true;
			}
		}

		private final static class MatchesId extends Condition {

			private final long id;

			private MatchesId(long id) {
				this.id = id;
			}

			@Override
			protected boolean evaluate(long accountId, Role[] accountRoles) {
				return id == accountId;
			}
		}

		private final static class Conjunction extends Condition {

			private final Condition[] conditions;

			private Conjunction(Condition[] conditions) {
				this.conditions = conditions;
			}

			@Override
			protected boolean evaluate(long accountId, Role[] accountRoles) {
				boolean base = conditions.length > 0;
				for (Condition condition: conditions) {
					base &= condition.evaluate(accountId, accountRoles);
				}
				return base;
			}
		}

		private final static class Addition extends Condition {

			private final Condition[] conditions;

			public Addition(Condition[] conditions) {
				this.conditions = conditions;
			}

			@Override
			protected boolean evaluate(long accountId, Role[] accountRoles) {
				boolean base = conditions.length == 0;
				for (Condition condition: conditions) {
					base |= condition.evaluate(accountId, accountRoles);
				}
				return base;
			}
		}

		private final static class Negation extends Condition {

			private final Condition condition;

			public Negation(Condition condition) {
				this.condition = condition;
			}

			@Override
			protected boolean evaluate(long accountId, Role[] accountRoles) {
				return !condition.evaluate(accountId, accountRoles);
			}
		}

		private final static class Constant extends Condition {

			private final boolean constant;

			private Constant(boolean constant) {
				this.constant = constant;
			}

			@Override
			protected boolean evaluate(long accountId, Role[] accountRoles) {
				return constant;
			}
		}
	}

	public static class Builder {

		private Builder() { }

		public Condition hasRoles() {
			return new Condition.HasAnyRoles();
		}

		public Condition hasRoles(Role... roles) {
			return new Condition.HasSpecifiedRoles(roles);
		}

		public Condition isAdministrator() {
			return new Condition.HasSpecifiedRoles(new Role[]{Role.ADMIN});
		}

		public Condition isUser() {
			return new Condition.HasSpecifiedRoles(new Role[]{Role.USER});
		}

		public Condition isService() {
			return new Condition.HasSpecifiedRoles(new Role[]{Role.SERVICE});
		}

		public Condition matchesId(long id) {
			return new Condition.MatchesId(id);
		}

		public Condition allowAny() {
			return new Condition.Constant(true);
		}

		public Condition allowNone() {
			return new Condition.Constant(false);
		}

		public Condition all(Condition... conditions) {
			return new Condition.Conjunction(conditions);
		}

		public Condition any(Condition... conditions) {
			return new Condition.Addition(conditions);
		}

		public Condition not(Condition condition) {
			return new Condition.Negation(condition);
		}
	}

	private final Condition condition;

	private ConditionalAuthorizer(Condition condition) {
		this.condition = condition;
	}

	public static Builder builder() {
		return new Builder();
	}

	public void authorize(long accountId, Role[] accountRoles) throws AuthorizationException {
		if (!condition.evaluate(accountId, accountRoles)) throw new AuthorizationException("Not authorized");
	}
}
