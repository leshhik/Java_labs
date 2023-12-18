public class MainApplication {
    public static int countConsumedFood(Food[] breakfast) {

        int count = 0;

        for(Food item : breakfast) {

            if(item != null) {

                item.consume();
                count++;

            } else {
                break;
            }

        }

        return count;

    }
    public static void main(String[] args) throws Exception {
        Food[] breakfast = new Food[20];
        int itemsSoFar = 0;
        for (String arg : args) {
            String[] parts = arg.split("/");
            if (parts[0].equals("Cheese")) {
                breakfast[itemsSoFar] = new Cheese();
            } else if (parts[0].equals("Apple")) {
                breakfast[itemsSoFar] = new Apple(parts[1]);
            }
            if (parts[0].equals("Burger")) {
                breakfast[itemsSoFar] = new Burger(parts[1]);
            }
            itemsSoFar++;
        }
        int total = countConsumedFood(breakfast);
        System.out.println("Общее кол-во фруктов съеденыъ на завтрак: " + total);
    }
}
