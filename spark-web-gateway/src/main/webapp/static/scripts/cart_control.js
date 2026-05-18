function addToCartButtonPressed(e) {
	e.preventDefault();
	const pressedButton = e.currentTarget;
	const cartControl = pressedButton.parentElement.querySelector(".cart_control");
	pressedButton.classList.add("hidden");
	cartControl.classList.remove("hidden");
	const selectElement = cartControl.querySelector("select");
	selectElement.selectedIndex = 0;
}

function cartControlSelected(e) {
	e.preventDefault();
	const selectedValue = e.target.value;
	if (selectedValue === "0") {
		const cartControl = e.target.parentElement;
		const addToCartButton = cartControl.parentElement.querySelector(".add_to_cart_button");
		cartControl.classList.add("hidden");
		addToCartButton.classList.remove("hidden");
	}
}

const allCartControlSelectors = document.querySelectorAll("select");
for (const cartControlSelector of allCartControlSelectors) {
	cartControlSelector.addEventListener("change", cartControlSelected);
}

const allAddToCartButtons = document.querySelectorAll(".add_to_cart_button");
for (const addToCartButton of allAddToCartButtons) {
	addToCartButton.addEventListener("click", addToCartButtonPressed);
}
	
