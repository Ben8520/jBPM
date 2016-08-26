import java.awt.*;

class Decision extends Block {

    private Point origine = new Point();

    Decision(String name) {
        super(name);
    }

    @Override
    void paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere) {
        x = (bestCoordinates.x != 0 ? bestCoordinates.x : x);
        y = (bestCoordinates.y != 0 ? bestCoordinates.y : y);

        svgGenerator.setStroke(new BasicStroke(3));
        svgGenerator.drawPolygon(new int[]{x, x + 20, x, x - 20}, new int[]{y + 20, y, y - 20, y}, 4);
        svgGenerator.setColor(Color.red);
        svgGenerator.fillPolygon(new int[]{x, x + 20, x, x - 20}, new int[]{y + 20, y, y - 20, y}, 4);
        svgGenerator.setStroke(new BasicStroke(1));
        svgGenerator.setColor(Color.black);
        this.origine.setLocation(x, y);
    }
}
