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

        Polygon polygon = new Polygon(new int[]{point.x, point.x + 20, point.x, point.x - 20}, new int[]{point.y + 20, point.y, point.y - 20, point.y}, 4);

        boolean intersect = false;
        for (Rectangle rectangle_it: rectangles) {
            if (polygon.intersects(rectangle_it)) {
                intersect = true;
                break;
            }
        }

        if (intersect) {
            this.setBestCoordinates(new Point(this.getBestCoordinates().x - x_offset, 0));
            point = chooseAndUpdateCoordinates(x - x_offset, y);
            polygon = new Polygon(new int[]{point.x, point.x + 20, point.x, point.x - 20}, new int[]{point.y + 20, point.y, point.y - 20, point.y}, 4);
        }

        svgGenerator.setStroke(new BasicStroke(5));
        svgGenerator.drawPolygon(polygon);
        svgGenerator.setColor(Color.red);
        svgGenerator.fillPolygon(polygon);
        svgGenerator.setStroke(new BasicStroke(1));
        svgGenerator.setColor(Color.black);
        this.origine.setLocation(point.x, point.y+20);
    }

    @Override
    Point getUniqueOrigine() {
        return origine;
    }

    @Override
    void setTransitionsEndpoints(List<Block> blocks, List<Block> blocksLeft) {
        for (Block father: getFathers()) {
            Transition transition = father.getTransition(this);
            if (transition != null) {
                transition.setOrigine(father.getUniqueOrigine());
                transition.setDestination(new Point(getBestCoordinates().x, getBestCoordinates().y - 20));
            }
        }

        for (Transition transition: transitions) {
            Block destination = Main.getBlockFromName(blocks, transition.getDirection());
            if (!blocksLeft.contains(destination))
                transition.setOrigine(this.getBestCoordinates());
        }
    }

    Point getLeftOrRightCorner(Integer x_offset) {
        if (x_offset > 0)
            return new Point(getBestCoordinates().x + 22, getBestCoordinates().y);
        return new Point(getBestCoordinates().x - 22, getBestCoordinates().y);
    }
}
