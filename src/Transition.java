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
        if (!"end".equals(direction) && (origine.x != 0 && origine.y != 0) && (destination.x != 0 && destination.y != 0)) {
            svgGenerator.setStroke(new BasicStroke(4));
            Line2D line_1 = new Line2D.Double(origine.x, origine.y+3, origine.x, origine.y+18);
            Line2D line_2 = new Line2D.Double(origine.x, origine.y+18, destination.x, destination.y-38);
            Line2D line_3 = new Line2D.Double(destination.x, destination.y-38, destination.x, destination.y-3);

            if (!lineIntersectSth(rectangles, line_1) && !lineIntersectSth(rectangles, line_2) && !lineIntersectSth(rectangles, line_3)) {
                svgGenerator.draw(line_1);
                svgGenerator.draw(line_2);
                svgGenerator.draw(line_3);
            }
            else {
                handleLine(svgGenerator, rectangles, line_2, blocks);
            }
            svgGenerator.setStroke(new BasicStroke(1));
        }
    }

    private Boolean lineIntersectSth(List<Rectangle> rectangles, Line2D line) {
        for (Rectangle rectangle: rectangles)
            if (line.intersects(rectangle))
                if (!(line.getX1() != line.getX2() && rectangle.width == 20))
                    return true;

        return false;
    }

    private void handleLine(Graphics2D svgGenerator, List<Rectangle> rectangles, Line2D line, List<Block> blocks) {

        Integer x_offset = 0;
        do {
            Integer loop_offset = 0;
            for (Rectangle rectangle : rectangles)
                if (line.intersects(rectangle))
                    if (Math.abs(rectangle.x + (int) rectangle.getWidth() - (int) line.getX1()) > Math.abs(loop_offset))
                        if (rectangle.x + (int) rectangle.getWidth() - (int) line.getX1() > 0)
                            loop_offset = rectangle.x + (int) rectangle.getWidth() - (int) line.getX1();
                        else
                            loop_offset = rectangle.x - (int) line.getX1();


            if (loop_offset > 0)
                loop_offset += 30;
            else
                loop_offset -= 30;
            x_offset += loop_offset;

            line = new Line2D.Double(origine.x + x_offset + (x_offset < 0 ? (-20) : 20), origine.y, origine.x + x_offset + (x_offset < 0 ? (-20) : 20), destination.y);
        } while (lineIntersectSth(rectangles, line));

        Block block = Main.getBlockFromName(blocks, this.from);
        if (block != null && block instanceof Decision) {
            origine = ((Decision)block).getLeftOrRightCorner(x_offset);
            svgGenerator.drawLine(origine.x, origine.y, origine.x + x_offset,origine.y);
            svgGenerator.drawLine(origine.x + x_offset, origine.y, origine.x + x_offset, destination.y-38);
            rectangles.add(new Rectangle(origine.x + x_offset - 10, (origine.y < destination.y ? origine.y : destination.y), 20, Math.abs(destination.y - origine.y)- 38));
            svgGenerator.drawLine(origine.x + x_offset, destination.y-38, destination.x, destination.y-38);
        } else {
            svgGenerator.drawLine(origine.x, origine.y+3, origine.x, origine.y+18);
            svgGenerator.drawLine(origine.x, origine.y + 18, origine.x + x_offset, origine.y+18);
            svgGenerator.drawLine(origine.x + x_offset, origine.y+18, destination.x + x_offset, destination.y-38);
            rectangles.add(new Rectangle(destination.x + x_offset - 10, (origine.y < destination.y ? origine.y : destination.y), 20, Math.abs(destination.y - origine.y)- 38));
            svgGenerator.drawLine(destination.x + x_offset, destination.y-38, destination.x, destination.y-38);
        }
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
                ", from='" + from + '\'' +
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transition that = (Transition) o;

        if (!from.equals(that.from)) return false;
        return direction.equals(that.direction);

    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }
}
