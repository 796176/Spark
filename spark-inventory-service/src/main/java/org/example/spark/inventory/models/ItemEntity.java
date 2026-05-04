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

package org.example.spark.inventory.models;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

@Entity
@Table(name = "items")
public class ItemEntity {

	private long id;

	private String name;

	private String pictureName;

	private EmbeddablePrice embeddablePrice;

	private int amount;

	private ItemStatus itemStatus;

	private long version;

	public ItemEntity() { }

	public ItemEntity(
		@Nonnull String name,
		@Nonnull Money price,
		int amount,
		@Nullable String pictureName,
		@Nonnull ItemStatus status
	) {
		this();
		this.setName(name);
		this.setPictureName(pictureName);
		this.setEmbeddablePrice(new EmbeddablePrice(price.getCurrencyAmount(), price.getCentAmount()));
		this.setAmount(amount);
		this.setItemStatus(status);
		this.setVersion(0L);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	@Nullable
	public String getPictureName() {
		return pictureName;
	}

	public void setPictureName(@Nullable String pictureName) {
		this.pictureName = pictureName;
	}

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "currencyAmount", column = @Column(name = "price_currency_amount")),
		@AttributeOverride(name = "centAmount", column = @Column(name = "price_cent_amount"))
	})
	public EmbeddablePrice getEmbeddablePrice() {
		return embeddablePrice;
	}

	public void setEmbeddablePrice(@Nonnull EmbeddablePrice embeddablePrice) {
		this.embeddablePrice = embeddablePrice;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	@ManyToOne
	public ItemStatus getItemStatus() {
		return itemStatus;
	}

	public void setItemStatus(@Nonnull ItemStatus itemStatus) {
		this.itemStatus = itemStatus;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
