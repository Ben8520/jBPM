import java.awt.*;
import java.util.Arrays;
import java.util.List;

class Decision extends Block {

    private Point origine = new Point();
    private String parameter;

    Decision(String name) {
        super(name);
        this.parameter = null;
    }

    @Override
    List<Point> paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere, List<Rectangle> rectangles, List<Block> blocks) {
        Point point = chooseAndUpdateCoordinates(x, y);

        Integer outgoingTransition = transitions.size();
        if (outgoingTransition > 1) {
            Polygon polygon = new Polygon(new int[]{point.x, point.x + 20, point.x, point.x - 20}, new int[]{point.y + 20, point.y, point.y - 20, point.y}, 4);

            boolean intersect = false;
            for (Rectangle rectangle_it : rectangles) {
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
            rectangles.add(new Rectangle(point.x - 20, point.y - 20, 40, 40));
            svgGenerator.setColor(Color.red);
//            svgGenerator.drawString(this.getName(), point.x + 15, point.y - 15);
            svgGenerator.fillPolygon(polygon);
            svgGenerator.setStroke(new BasicStroke(1));
            svgGenerator.setColor(Color.black);
        } else if (outgoingTransition == 1) {
            svgGenerator.setStroke(new BasicStroke(4));
            svgGenerator.drawLine(point.x, point.y - 20, point.x, point.y + 20);
            svgGenerator.setStroke(new BasicStroke(1));
        }

        this.origine.setLocation(point.x, point.y + 20);

        return Arrays.asList(new Point(point.x - 20, point.y -20),
                new Point(point.x + 20, point.y + 20));
    }

    @Override
    Point getUniqueOrigine(Integer current_x) {
        return origine;
    }

    @Override
    void setTransitionsEndpoints(List<Block> blocks, List<Block> blocksLeft) {
        for (Block father: getFathers()) {
            Transition transition = father.getTransition(this);
            if (transition != null) {
                transition.setOrigine(father.getFatherOrigine());
                transition.setDestination(new Point(getBestCoordinates().x, getBestCoordinates().y - 20));
            }
        }

        for (Transition transition: transitions) {
            Block destination = SvgGenerator.getBlockFromName(blocks, transition.getDirection());
            if (!blocksLeft.contains(destination))
                transition.setOrigine(this.getBestCoordinates());
        }
    }

    Point getLeftOrRightCorner(Integer x_offset) {
        if (x_offset > 0)
            return new Point(getBestCoordinates().x + 22, getBestCoordinates().y);
        return new Point(getBestCoordinates().x - 22, getBestCoordinates().y);
    }

    String getParameter() {
        return parameter;
    }

    void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
