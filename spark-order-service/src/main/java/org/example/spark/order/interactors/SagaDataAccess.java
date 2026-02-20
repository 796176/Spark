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

package org.example.spark.order.interactors;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.example.spark.order.aggregates.OrderAggregate;
import org.example.spark.order.sagas.Saga;

public interface SagaDataAccess {

	Saga newSaga(@Nonnull OrderAggregate order, @Nonnull String sagaType, @Nullable String idempotenceToken) throws Exception;

	Saga[] getSagas(@Nonnull OrderDataAccess orderDataAccess) throws Exception;

	Saga getSagaByOrder(@Nonnull OrderAggregate order);

	String updateSagaState(long sagaId, long sagaStateId);

	void deleteSaga(long sagaId);
}
