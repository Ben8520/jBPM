import java.awt.*;
import java.util.Arrays;
import java.util.List;

class StartState extends Block{

    private Point origine = new Point();

    StartState() {
        super("start-state");
    }

    @Override
    List<Point> paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere, List<Rectangle> rectangles, List<Block> blocks) {
       Point point = chooseAndUpdateCoordinates(x, y);

        svgGenerator.fillOval(point.x-25, point.y, 50, 50);
        this.origine.setLocation(point.x, point.y+50);

        return Arrays.asList(origine, origine);
    }

    @Override
    Point getUniqueOrigine(Integer current_x) {
        return origine;
    }


}
