public class CartItem {
    private String id;
    private int count;

    public CartItem(String id) {
        this.id = id;
        this.count = 1;
    }

    public String getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
