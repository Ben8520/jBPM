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
        if (!"end".equals(direction)) {
            svgGenerator.setStroke(new BasicStroke(4));
            int dy = (int)(0.8 * Math.abs(destination.y - origine.y));
            svgGenerator.drawLine(origine.x, origine.y, origine.x, origine.y + dy);
            svgGenerator.drawLine(origine.x, origine.y + dy, destination.x, origine.y + dy);
            svgGenerator.drawLine(destination.x, origine.y + dy, destination.x, destination.y);
            svgGenerator.setStroke(new BasicStroke(1));
        }
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

    void setOrigine(Point origine) {
        this.origine = origine;
    }

    void setDestination(Point destination) {
        this.destination = destination;
    }
}
