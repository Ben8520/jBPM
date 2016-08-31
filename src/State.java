import java.awt.*;
import java.util.Arrays;
import java.util.List;

class State extends Block{

    private Point origine = new Point();

    State(String name) {
        super(name);
    }

    @Override
    List<Point> paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere, List<Rectangle> rectangles, List<Block> blocks) {
        Point point = chooseAndUpdateCoordinates(x, y);

        Rectangle rectangle = new Rectangle(point.x-70, point.y, 140, 40);
        boolean intersect = false;
        for (Rectangle rectangle_it: rectangles) {
            if (rectangle.intersects(rectangle_it)) {
                intersect = true;
                break;
            }
        }

        if (intersect) {
            this.overideBestCoordinates(new Point(this.getBestCoordinates().x + x_offset, 0));
            point = chooseAndUpdateCoordinates(x + x_offset, y);
            rectangle = new Rectangle(point.x-70, point.y, 140, 40);
        }

        rectangles.add(rectangle);

        svgGenerator.setStroke(new BasicStroke(3));
        svgGenerator.draw(rectangle);
        this.printSimpleString(svgGenerator, this.name, 140, point.x-70, point.y + 24);
        svgGenerator.setStroke(new BasicStroke(1));
        this.origine.setLocation(point.x, point.y+40);

        return Arrays.asList(new Point(point.x-70, point.y),
                new Point(point.x + 70, point.y + 40));
    }

    @Override
    Point getUniqueOrigine(Integer current_x) {
        return origine;
    }

    private void printSimpleString(Graphics2D g2d, String s, int width, int XPos, int YPos){
        if (s.length() > 27) s = s.substring(0, 27);

        int stringLen = (int)
                g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();
        int start = width/2 - stringLen/2;
        g2d.drawString(s, start + XPos, YPos);
    }
}
