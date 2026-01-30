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

package org.example.spark.account.main;

import jakarta.persistence.EntityManagerFactory;
import org.example.spark.account.events.AccountEventConverter;
import org.example.spark.account.events.JsonAccountEventConverter;
import org.example.spark.account.controllers.PasswordEncoder;
import org.example.spark.account.controllers.SpringPasswordEncoder;
import org.example.spark.account.controllers.AccountService;
import org.example.spark.account.controllers.AccountServiceImpl;
import org.example.spark.account.intaractors.AccountDataAccess;
import org.example.spark.account.models.Password;
import org.example.spark.account.persistence.JPAAccountDataAccess;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;

import java.nio.charset.StandardCharsets;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true, rollbackOn = RollbackOn.ALL_EXCEPTIONS)
@EntityScan({"org.example.spark.account.models"})
public class AccountServiceConfiguration {

	@Bean
	AccountEventConverter<String> accountEventConverter() {
		return new JsonAccountEventConverter();
	}

	@Bean
	TransactionManager transactionManager() {
		return new JpaTransactionManager();
	}

	@Bean
	AccountDataAccess accountDataAccess(EntityManagerFactory entityManagerFactory, AccountEventConverter<String> accountEventConverter) {
		return new JPAAccountDataAccess(entityManagerFactory, accountEventConverter);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new SpringPasswordEncoder(new BCryptPasswordEncoder());
	}

	@Bean
	AccountService accountService(AccountDataAccess accountDataAccess, PasswordEncoder passwordEncoder) {
		return new AccountServiceImpl(accountDataAccess, passwordEncoder);
	}

	@Bean(initMethod = "run")
	MainThread mainThread(AccountService accountService) {
		return new MainThread(accountService);
	}

	static class MainThread extends Thread {

		private AccountService accountService;

		public MainThread(AccountService accountService) {
			this.accountService = accountService;
		}
		@Override
		public void run() {
			accountService.createAdminAccount("betty", new Password() {
				@Override
				public byte[] getPassword() {
					return "pass".getBytes(StandardCharsets.UTF_8);
				}

				@Override
				public void destroy() {

				}
			});
		}
	}


	static void main(String[] args) {
		SpringApplication.run(AccountServiceConfiguration.class, args);
	}
}
