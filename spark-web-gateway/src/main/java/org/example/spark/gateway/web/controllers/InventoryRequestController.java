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

package org.example.spark.gateway.web.controllers;

import jakarta.servlet.http.HttpSession;
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Controller
public class InventoryRequestController {

	@Value("${org.example.spark.rest.timeout}")
	private long timeout;

	@Autowired
	private InventoryRequestProcessor inventoryRequestProcessor;

	@Autowired
	private AccountRequestProcessor accountRequestProcessor;

	@GetMapping("/inventory")
	public Callable<String> inventory(HttpSession httpSession, Model model) throws Exception {
		return () -> {
			Account account = accountRequestProcessor.getAccount(httpSession.getId());
			model.addAttribute("account", Optional.ofNullable(account));
			Future<Item[]> gettingInventoryProcess = inventoryRequestProcessor.getInventory(httpSession.getId());

			try {
				Item[] items = gettingInventoryProcess.get(timeout, TimeUnit.MILLISECONDS);
				model.addAttribute("items", items);
				return "inventory";
			} catch (TimeoutException e) {
				return "504";
			}
		};
	}
}
