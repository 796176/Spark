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

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.spark.gateway.web.interactors.UploadedFileDataAccess;
import org.example.spark.gateway.web.models.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.Callable;

@Controller
@RequestMapping
public class GeneralRequestController {

	@Autowired
	private AccountRequestProcessor accountRequestProcessor;

	@Autowired
	private GeneralRequestProcessor generalRequestProcessor;

	@GetMapping("/")
	public String rootPage() {
		return "redirect:/index";
	}

	@GetMapping("/index")
	public Callable<String> welcomePage(HttpSession httpSession, Model model) {
		return () -> {
			Account account = accountRequestProcessor.getAccount(httpSession.getId());
			model.addAttribute("account", Optional.ofNullable(account));
			return "index";
		};
	}

	@GetMapping("/400")
	public String error400() {
		return "400";
	}

	@GetMapping("/403")
	public String error403() {
		return "403";
	}

	@GetMapping("/500")
	public String error500() {
		return "500";
	}

	@GetMapping("/504")
	public String error504() {
		return "504";
	}

	@ResponseBody
	@GetMapping("/upload/{fileName}")
	public Callable<Void> getUpload(HttpServletResponse httpServletResponse, @PathVariable("fileName") String fileName) {
		return () -> {
			try {
				UploadedFileDataAccess.Blob blob = generalRequestProcessor.getUploadedFile(fileName);
				httpServletResponse.setContentType(blob.contentType());
				httpServletResponse.getOutputStream().write(blob.content());
				httpServletResponse.flushBuffer();
			} catch (Exception e) {
				httpServletResponse.sendRedirect("/400");
			}
			return null;
		};
	}

}
