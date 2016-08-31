import java.awt.*;
import java.util.Arrays;
import java.util.List;

class EndState extends Block{

    private Point origine = new Point();

    EndState() {
        super("end");
    }

    @Override
    List<Point> paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere, List<Rectangle> rectangles) {
        Point point = chooseAndUpdateCoordinates(x, y);
        this.origine.setLocation(point.x, point.y);
        return Arrays.asList(origine, origine);
    }

    @Override
    Point getUniqueOrigine(Integer current_x) {
        return origine;
    }

}
