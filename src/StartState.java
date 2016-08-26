import java.awt.*;

class StartState extends Block{

    private Point origine = new Point();

    StartState() {
        super("start-state");
    }

    @Override
    void paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere) {
       Point point = chooseAndUpdateCoordinates(x, y);

        svgGenerator.fillOval(point.x-25, point.y, 50, 50);
        this.origine.setLocation(point.x, point.y+50);
    }

    @Override
    Point getUniqueOrigine() {
        return origine;
    }


}
