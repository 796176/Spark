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

package org.example.spark.gateway.web.validators;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.example.spark.gateway.web.models.OrderManagementForm;
import org.springframework.stereotype.Component;

@Component
public class OrderManagementFormValidator implements Validator {

	@Override
	public boolean supports(@Nonnull Class<?> clazz) {
		return clazz.equals(OrderManagementForm.class);
	}

	@Nullable
	@Override
	public ValidationResult validate(@Nonnull Object o) {
		OrderManagementForm orderManagementForm = (OrderManagementForm) o;
		String validationResult = FormValidators.validateOrderManagementForm(orderManagementForm);
		if (validationResult != null) return new ValidationResult(validationResult);
		else return null;
	}
}
