import java.awt.*;
import java.util.List;

class Decision extends Block {

    private Point origine = new Point();

    Decision(String name) {
        super(name);
    }

    @Override
    void paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere, List<Rectangle> rectangles) {
        Point point = chooseAndUpdateCoordinates(x, y);

        svgGenerator.setStroke(new BasicStroke(5));
        svgGenerator.drawPolygon(new int[]{point.x, point.x + 20, point.x, point.x - 20}, new int[]{point.y + 20, point.y, point.y - 20, point.y}, 4);
        svgGenerator.setColor(Color.red);
        svgGenerator.fillPolygon(new int[]{point.x, point.x + 20, point.x, point.x - 20}, new int[]{point.y + 20, point.y, point.y - 20, point.y}, 4);
        svgGenerator.setStroke(new BasicStroke(1));
        svgGenerator.setColor(Color.black);
        this.origine.setLocation(point.x, point.y+20);
    }

    @Override
    Point getUniqueOrigine() {
        return origine;
    }

    @Override
    void setTransitionsEndpoints() {
        for (Block father: getFathers()) {
            Transition transition = father.getTransition(this);
            if (transition != null) {
                transition.setOrigine(father.getUniqueOrigine());
                transition.setDestination(new Point(getBestCoordinates().x, getBestCoordinates().y - 20));
            }
        }
    }
}
