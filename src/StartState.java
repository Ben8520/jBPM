import java.awt.*;

class StartState extends Block{

    private Point origine = new Point();

    StartState() {
        super("start-state");
    }

    @Override
    void paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere) {
        x = (bestCoordinates.x != 0 ? bestCoordinates.x : x);
        y = (bestCoordinates.y != 0 ? bestCoordinates.y : y);

        svgGenerator.fillOval(x-25, y, 50, 50);
        this.origine.setLocation(x, y+50);
    }


}
