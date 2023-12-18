public class Burger extends Food {
    private String size;
    public Burger(String size) {
        super("Бургер");
        this.size = size;
    }
    public void consume() {
        System.out.println(this + " съеден");
    }
    public String getSize() {
        return size;
    }
    public void setSize(String size) {
        this.size = size;
    }

    public String toString() {
        return super.toString() + " размера '" + size.toUpperCase() + "'";
    }
}