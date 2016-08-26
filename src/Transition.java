import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

class Transition {

    private final String name;
    private final String direction;

    private Point origine = new Point();
    private Point destination = new Point();

    Transition(String name, String direction) {
        this.name = name;
        this.direction = direction;
    }

    void paint(Graphics2D svgGenerator, List<Rectangle> rectangles) {
        if (!"end".equals(direction)) {
            svgGenerator.setStroke(new BasicStroke(4));
            Line2D line_1 = new Line2D.Double(origine.x, origine.y+3, origine.x, origine.y+18);
            Line2D line_2 = new Line2D.Double(origine.x, origine.y+18, destination.x, destination.y-18);
            Line2D line_3 = new Line2D.Double(destination.x, destination.y-18, destination.x, destination.y-3);
            boolean print = true;
            for (Rectangle rectangle:rectangles) {
                if (line_1.intersects(rectangle) || line_2.intersects(rectangle) || line_3.intersects(rectangle))
                    print = false;
            }
            if (print) {
                svgGenerator.draw(line_1);
                svgGenerator.draw(line_2);
                svgGenerator.draw(line_3);
            }
            else
                handleLine(svgGenerator, rectangles);
            svgGenerator.setStroke(new BasicStroke(1));
        }
    }

    private void handleLine(Graphics2D svgGenerator, List<Rectangle> rectangles) {
//    TODO
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
