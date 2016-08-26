import java.awt.*;

class Transition {

    private final String name;
    private final String direction;

    private Point origine = new Point();
    private Point destination = new Point();

    Transition(String name, String direction) {
        this.name = name;
        this.direction = direction;
    }

    void paint(Graphics2D svgGenerator) {
        svgGenerator.setStroke(new BasicStroke(3));
        svgGenerator.drawLine(origine.x, origine.y, destination.x, destination.y);
    }

    String getName() {
        return name;
    }

    String getDirection() {
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
