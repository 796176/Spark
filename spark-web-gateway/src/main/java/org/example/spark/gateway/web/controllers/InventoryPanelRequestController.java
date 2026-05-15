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
import org.example.spark.authorization.exceptions.AuthorizationException;
import org.example.spark.gateway.web.exceptions.AuthenticationException;
import org.example.spark.gateway.web.models.*;
import org.example.spark.gateway.web.validators.FormValidators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.concurrent.*;

@Controller
@RequestMapping("/panel")
public class InventoryPanelRequestController {

	@Value("${org.example.spark.rest.timeout}")
	private long timeout;

	@Autowired
	private InventoryPanelRequestProcessor inventoryPanelRequestProcessor;

	@Autowired
	private AccountRequestProcessor accountRequestProcessor;

	@GetMapping("/inventory")
	public Callable<String> getItems(HttpSession httpSession, Model model) {
		return () -> {
			try {
				Account account = accountRequestProcessor.getAccount(httpSession.getId());
				Future<Item[]> gettingInventoryProcess = inventoryPanelRequestProcessor.getItems(httpSession.getId());
				Item[] items = gettingInventoryProcess.get(timeout, TimeUnit.MILLISECONDS);
				model.addAttribute("account", Optional.ofNullable(account));
				model.addAttribute("items", items);
				return "inventory_panel";
			} catch (AuthenticationException e) {
				return "redirect:/login";
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return "403";
					} else return "redirect:/login";
				}
				else return "500";
			} catch (TimeoutException e) {
				return "504";
			}
		};
	}

	@GetMapping("/item/{itemId}")
	public Callable<String> getItem(HttpSession httpSession, Model model, @PathVariable(name = "itemId") long itemId) {
		return () -> {
			try {
				Account account = accountRequestProcessor.getAccount(httpSession.getId());
				Future<Item> gettingItemProcess = inventoryPanelRequestProcessor.getItem(httpSession.getId(), itemId);
				Item item = gettingItemProcess.get(timeout, TimeUnit.MILLISECONDS);
				model.addAttribute("account", Optional.ofNullable(account));
				model.addAttribute("managedItem", item);
				model.addAttribute("errorMessage", Optional.empty());
				return "item_management_form";
			} catch (AuthenticationException e) {
				return "redirect:/login";
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return "403";
					} else return "redirect:/login";
				}
				else return "500";
			} catch (TimeoutException e) {
				return "504";
			}
		};
	}

	@PostMapping("/item/{itemId}/save")
	@ResponseBody
	public Callable<FormSubmissionResponse> saveItem(
		HttpSession httpSession, @PathVariable(name = "itemId") long itemId, @RequestBody ItemManagementForm form
	) {
		return () -> {
			try {
				if (FormValidators.validateItemManagementForm(form) != null) return new ErrorFormSubmissionResponse("Bad Request");
				Future<?> savingItemProcess = inventoryPanelRequestProcessor.saveItem(
					httpSession.getId(),
					itemId,
					form.getVersion(),
					form.getPreviousItemAmount(),
					form.getCurrentItemAmount()
				);
				savingItemProcess.get(timeout, TimeUnit.MILLISECONDS);
				return new RedirectFormSubmissionResponse("/panel/inventory");
			} catch (AuthenticationException e) {
				return new RedirectFormSubmissionResponse("/login");
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return new ErrorFormSubmissionResponse("Not Authorized");
					} else return new RedirectFormSubmissionResponse("/login");
				}
				else return new ErrorFormSubmissionResponse("Server Error. Try Again Later");
			} catch (TimeoutException e) {
				return new ErrorFormSubmissionResponse("Timeout Error. Try Again Later");
			}
		};
	}

	@DeleteMapping("/item/{itemId}/delete")
	@ResponseBody
	public Callable<FormSubmissionResponse> deleteItem(HttpSession httpSession, @PathVariable(name = "itemId") long itemId) {
		return () -> {
			try {
				Future<?> deletingItemProcess = inventoryPanelRequestProcessor.deleteItem(httpSession.getId(), itemId);
				deletingItemProcess.get(timeout, TimeUnit.MILLISECONDS);
				return new RedirectFormSubmissionResponse("/panel/inventory");
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return new ErrorFormSubmissionResponse("Not Authorized");
					} else return new RedirectFormSubmissionResponse("/login");
				}
				else return new ErrorFormSubmissionResponse("Server Error. Try Again Later");
			} catch (TimeoutException e) {
				return new ErrorFormSubmissionResponse("Timeout Error. Try Again Later");
			}
		};
	}

	@GetMapping("/newitem")
	public Callable<String> newItem(HttpSession httpSession, Model model) {
		return () -> {
			Account account = accountRequestProcessor.getAccount(httpSession.getId());
			model.addAttribute("account", Optional.ofNullable(account));
			model.addAttribute("errorMessage", Optional.empty());
			return "new_item_form";
		};
	}

	@PostMapping("/newitem")
	@ResponseBody
	public Callable<FormSubmissionResponse> newItem(
		HttpSession httpSession,
		@RequestPart("form") CreatingItemForm form,
		@RequestPart(value = "item_picture") MultipartFile itemPicture
	) {
		return () -> {
			try {
				UploadedFile picture = null;
				boolean filenameIsNotNullOrBlank =
					itemPicture.getOriginalFilename() != null && !itemPicture.getOriginalFilename().isBlank();
				if (filenameIsNotNullOrBlank) {
					picture = new UploadedFile(itemPicture.getOriginalFilename(), itemPicture.getBytes());
				}

				Future<?> creatingItemProcess = inventoryPanelRequestProcessor
					.addItem(httpSession.getId(), form.getItemName(), form.getPrice(), form.getAmount(), picture);
				creatingItemProcess.get(timeout, TimeUnit.MILLISECONDS);
				return new RedirectFormSubmissionResponse("/panel/inventory");
			} catch (ExecutionException e) {
				if (e.getCause() instanceof AuthorizationException) {
					if (accountRequestProcessor.isLoggedIn(httpSession.getId())) {
						return new ErrorFormSubmissionResponse("Not Authorized");
					} else return new RedirectFormSubmissionResponse("/login");
				}
				else return new ErrorFormSubmissionResponse("Server Error. Try Again Later");
			} catch (TimeoutException e) {
				return new ErrorFormSubmissionResponse("Timeout Error. Try Again Later");
			}
		};
	}
}
