import java.awt.*;

class EndState extends Block{

    private Point origine = new Point();

    EndState() {
        super("end");
    }

    @Override
    void paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere) {
        x = (bestCoordinates.x != 0 ? bestCoordinates.x : x);
        y = (bestCoordinates.y != 0 ? bestCoordinates.y : y);

        svgGenerator.setStroke(new BasicStroke(4));
        svgGenerator.drawOval(x-30, y-5, 50, 50);
        svgGenerator.fillOval(x-25, y, 40, 40);
        svgGenerator.setStroke(new BasicStroke(1));
        this.origine.setLocation(x, y);
    }
}
