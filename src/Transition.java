import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

class Transition {

    private final String name;
    private final String from;
    private String direction;

    private Point origine = new Point();
    private Point destination = new Point();

    Transition(String name, String from, String direction) {
        this.name = name;
        this.from = from;
        this.direction = direction;
    }

    void paint(Graphics2D svgGenerator, List<Rectangle> rectangles, List<Block> blocks) {
        if (!"end".equals(direction)) {
            svgGenerator.setStroke(new BasicStroke(4));
            Line2D line_1 = new Line2D.Double(origine.x, origine.y+3, origine.x, origine.y+18);
            Line2D line_2 = new Line2D.Double(origine.x, origine.y+18, destination.x, destination.y-18);
            Line2D line_3 = new Line2D.Double(destination.x, destination.y-18, destination.x, destination.y-3);
            boolean print = true;
            for (Rectangle rectangle:rectangles) {
                if (line_1.intersects(rectangle) || line_2.intersects(rectangle) || line_3.intersects(rectangle))
                    print = false;
            }
            if (print) {
                svgGenerator.draw(line_1);
                svgGenerator.draw(line_2);
                svgGenerator.draw(line_3);
            }
            else {
                svgGenerator.draw(line_1);
                handleLine(svgGenerator, rectangles, line_2, blocks);
                svgGenerator.draw(line_3);
            }
            svgGenerator.setStroke(new BasicStroke(1));
        }
    }

    private void handleLine(Graphics2D svgGenerator, List<Rectangle> rectangles, Line2D line, List<Block> blocks) {

        Integer x_offset = 0;
        for (Rectangle rectangle: rectangles)
            if (line.intersects(rectangle))
                if (Math.abs(rectangle.x + (int)rectangle.getWidth() - (int)line.getX1()) > Math.abs(x_offset))
                    if (rectangle.x + (int)rectangle.getWidth() - (int)line.getX1() > 0)
                        x_offset = rectangle.x + (int)rectangle.getWidth() - (int)line.getX1();
                    else
                        x_offset = rectangle.x - (int)line.getX1();


        if (x_offset > 0)
            x_offset += 30;
        else
            x_offset -= 30;

        Block block = Main.getBlockFromName(blocks, this.from);
        if (block != null && block instanceof Decision) {
            origine = ((Decision)block).getLeftOrRightCorner(x_offset);
            svgGenerator.drawLine(origine.x, origine.y, origine.x + x_offset,origine.y);
            svgGenerator.drawLine(origine.x + x_offset, origine.y, origine.x + x_offset, destination.y-18);
        } else {
            svgGenerator.drawLine(origine.x, origine.y+18, origine.x + x_offset, origine.y+18);
            svgGenerator.drawLine(origine.x + x_offset, origine.y+18, destination.x + x_offset, destination.y-18);
        }
        svgGenerator.drawLine(origine.x + x_offset, destination.y-18, destination.x, destination.y-18);

    }

    String getName() {
        return name;
    }

    String getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return "Transition{" +
                "name='" + name + '\'' +
                ", to='" + direction + '\'' +
                '}';
    }

    void setOrigine(Point origine) {
        if (this.origine.x == 0)
            this.origine.x = origine.x;

        if (this.origine.y == 0)
            this.origine.y = origine.y;
    }

    void setDestination(Point destination) {
        if (this.destination.x == 0)
            this.destination.x = destination.x;

        if (this.destination.y == 0)
            this.destination.y = destination.y;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    Point getDestination() {
        return destination;
    }
}
