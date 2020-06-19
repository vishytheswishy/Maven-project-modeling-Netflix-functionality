import java.util.HashMap;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String username;
    private HashMap<String, Integer> cart;
    private float totalCost;
    public User(String username) {
        this.username = username;
        this.cart = new HashMap<>();
        totalCost = 0;
    }


    public String getUsername() {
        return this.username;
    }

    public void updateCart(String movieID, int quantity) {
        if (cart.containsKey(movieID)) {
            this.cart.replace(movieID, quantity);
        }
    }

    public void removeFromCart(String movieID) {
        this.cart.remove(movieID);
    }

    public HashMap<String, Integer> getCart() {
        return this.cart;
    }


    public double calcTotalCost() {
        double cost = 15.99;
        for (int quantity: cart.values())
            cost += quantity + cost;
        return cost;
    }

    public float getQuantity(String movieID) {
        if (this.cart.containsKey(movieID))
            return this.cart.get(movieID);
        return -1;
    }

    public float getCost(String movieID) {
        if (this.cart.containsKey(movieID))
            return this.cart.get(movieID);
        return -1;
    }


    public boolean duplicateRequest(String movieID, int quantity) {
        return this.cart.containsKey(movieID) && this.cart.get(movieID) == quantity;
    }

}
