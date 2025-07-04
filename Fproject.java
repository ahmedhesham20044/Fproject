package fproject;

import java.text.DecimalFormat;
import java.util.*;

abstract class Product {
	private final String name;
	private final double price;
	private int stock;
	private final boolean expired;
	private final boolean digital;
	private final double weight;

	public Product(String name, double price, int stock, boolean expired, boolean digital, double weight) {
		this.name = name;
		this.price = price;
		this.stock = stock;
		this.expired = expired;
		this.digital = digital;
		this.weight = weight;
	}

	public String getName() {
		return name;
	}

	public double getPrice() {
		return price;
	}

	public int getStock() {
		return stock;
	}

	public boolean isExpired() {
		return expired;
	}

	public boolean isDigital() {
		return digital;
	}

	public double getWeight() {
		return weight;
	}

	public void reduceStock(int amount) {
		if (amount > stock)
			throw new IllegalArgumentException("Not enough stock for " + name);
		stock -= amount;
	}

	public boolean requiresShipping() {
		return !digital;
	}
}

// Concrete product types
class BasicProduct extends Product {
	public BasicProduct(String name, double price, int stock, boolean expired, boolean digital, double weight) {
		super(name, price, stock, expired, digital, weight);
	}
}

// Customer & Cart
class Customer {
	private final String name;
	private double balance;

	public Customer(String name, double balance) {
		this.name = name;
		this.balance = balance;
	}

	public String getName() {
		return name;
	}

	public double getBalance() {
		return balance;
	}

	public void deduct(double amount) {
		if (balance < amount)
			throw new IllegalArgumentException("Insufficient balance");
		balance -= amount;
	}
}

class CartItem {
	private final Product product;
	private final int quantity;

	public CartItem(Product product, int quantity) {
		this.product = product;
		this.quantity = quantity;
	}

	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}
}

class Cart {
	private final List<CartItem> items = new ArrayList<>();

	public void addItem(Product product, int quantity) {
		if (product.getStock() < quantity) {
			throw new IllegalArgumentException("Not enough stock for " + product.getName());
		}
		items.add(new CartItem(product, quantity));
	}

	public List<CartItem> getItems() {
		return items;
	}

	public void clear() {
		items.clear();
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}
}

// Checkout logic
class CheckoutService {
	private static final double SHIPPING_FEE_PER_KG = 30.0;
	private final DecimalFormat df = new DecimalFormat("#.##");

	public void checkout(Customer customer, Cart cart) {
		if (cart.isEmpty())
			throw new IllegalStateException("Cart is empty");

		// Validate
		for (CartItem item : cart.getItems()) {
			Product product = item.getProduct();
			if (product.isExpired())
				throw new IllegalStateException(product.getName() + " is expired");
			if (product.getStock() < item.getQuantity())
				throw new IllegalStateException(product.getName() + " is out of stock");
		}

		// Calculate totals
		double subtotal = 0, shipping = 0;
		for (CartItem item : cart.getItems()) {
			subtotal += item.getProduct().getPrice() * item.getQuantity();
		}
		shipping = calculateShipping(cart);
		double total = subtotal + shipping;

		if (customer.getBalance() < total)
			throw new IllegalStateException("Insufficient balance");

		// Deduct
		customer.deduct(total);
		for (CartItem item : cart.getItems()) {
			item.getProduct().reduceStock(item.getQuantity());
		}

		// Print receipt
		printReceipt(cart, subtotal, shipping, total, customer.getBalance());

		cart.clear();
	}

	private double calculateShipping(Cart cart) {
		double totalWeight = 0;
		for (CartItem item : cart.getItems()) {
			Product product = item.getProduct();
			if (product.requiresShipping()) {
				totalWeight += product.getWeight() * item.getQuantity();
			}
		}
		return totalWeight * SHIPPING_FEE_PER_KG;
	}

	private void printReceipt(Cart cart, double subtotal, double shipping, double total, double remaining) {
		System.out.println("=== Receipt ===");
		for (CartItem item : cart.getItems()) {
			System.out.println(item.getQuantity() + "X" + item.getProduct().getName() + "\t"
					+ item.getProduct().getPrice() * item.getQuantity());
		}
		System.out.println("-----------------------");
		System.out.println("Subtotal: " + subtotal);
		System.out.println("Shipping: " + shipping);
		System.out.println("Total: " + total);
		System.out.printf("Remaining Balance:" + remaining);
	}
}


public class Fproject {
	public static void main(String[] args) {
		Product milk = new BasicProduct("Milk", 100, 10, false, false, 0.4);
		Product biscuits = new BasicProduct("Biscuits", 150, 5, false, false, 0.7);
		Product tv = new BasicProduct("TV", 20000, 3, false, false, 15.5);
		Product mobile = new BasicProduct("Mobile", 15000, 8, false, false, 0);
		Product scratchCard = new BasicProduct("Scratch Card", 50, 100, false, true, 0);

		Customer ahmed = new Customer("Ahmed", 50000);
		CheckoutService checkout = new CheckoutService();

		// Test Case 1
		try {
			System.out.println("== Test Case 1 ==");
			Cart cart1 = new Cart();
			cart1.addItem(milk, 2);
			cart1.addItem(biscuits, 1);
			checkout.checkout(ahmed, cart1);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}

		// Test Case 2
		try {
			System.out.println("\n== Test Case 2 ==");
			Cart cart2 = new Cart();
			cart2.addItem(scratchCard, 3);
			cart2.addItem(mobile, 1);
			checkout.checkout(ahmed, cart2);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}

		// Test Case 3
		try {
			System.out.println("\n== Test Case 3 ==");
			Product expiredMilk = new BasicProduct("Expired Milk", 100, 5, true, false, 0.4);
			Cart cart3 = new Cart();
			cart3.addItem(expiredMilk, 1);
			checkout.checkout(ahmed, cart3);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}

		// Test Case 4
		try {
			System.out.println("\n== Test Case 4 ==");
			Cart cart4 = new Cart();
			cart4.addItem(tv, 3); // ~60000, should fail
			checkout.checkout(ahmed, cart4);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}

		// Test Case 5
		try {
			System.out.println("\n== Test Case 5 ==");
			Cart cart5 = new Cart();
			checkout.checkout(ahmed, cart5); // Empty cart
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}
