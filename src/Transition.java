
public class Transition {

    private final String name;
    private final String direction;

    public Transition(String name, String direction) {
        this.name = name;
        this.direction = direction;
    }

    public String getName() {
        return name;
    }

    public String getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return "Transition{" +
                "name='" + name + '\'' +
                ", to='" + direction + '\'' +
                '}';
    }
}
