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

package org.example.spark.inventory.main;

import jakarta.annotation.Nonnull;
import org.example.spark.inventory.sagas.SagaType;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;

public class SchemaMigration {

	public static void migrate(@Nonnull DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		createSchemaVersioningTable(jdbcTemplate);
		if (tableBelowVersion(jdbcTemplate, "sagas", 2)) {
			migrateToV2(jdbcTemplate);
			insertTableVersion(jdbcTemplate, "sagas", 2);
		}
	}

	private static void createSchemaVersioningTable(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS table_schema(table_name text, table_version bigint);");
	}

	private static boolean tableBelowVersion(JdbcTemplate jdbcTemplate, String tableName, long version) {
		return !jdbcTemplate.query(
			"SELECT table_version FROM table_schema WHERE table_name = ? AND table_version = ?;",
			ps -> {
				ps.setString(1, tableName);
				ps.setLong(2, version);
			},
			ResultSet::next
		);
	}

	private static void migrateToV2(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.execute("ALTER TABLE sagas ALTER COLUMN state TYPE bigint;");
		jdbcTemplate.execute("ALTER TABLE sagas ADD COLUMN IF NOT EXISTS saga_type text;");
		jdbcTemplate.update(
			"UPDATE sagas SET saga_type=?;",
			ps -> ps.setString(1, SagaType.ITEM_DELETED.getId())
		);
		jdbcTemplate.execute("ALTER TABLE sagas ALTER COLUMN saga_type SET NOT NULL;");
		jdbcTemplate.execute("ALTER TABLE sagas ADD CONSTRAINT item_id_unique_constraint UNIQUE (item_id);");
		jdbcTemplate.execute("ALTER TABLE sagas DROP COLUMN IF EXISTS class_name;");
	}

	private static void insertTableVersion(JdbcTemplate jdbcTemplate, String tableName, long version) {
		jdbcTemplate.update(
			"INSERT INTO table_schema(table_name, table_version) VALUES (?, ?);",
			ps -> {
				ps.setString(1, tableName);
				ps.setLong(2, version);
			}
		);
	}
}
