import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.sqrt;

class Fork extends Block {
    private Set<Point> origines = new HashSet<>();

    Fork(String name) {
        super(name);
    }

    @Override
    void paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere) {
        x = (bestCoordinates.x != 0 ? bestCoordinates.x : x);
        y = (bestCoordinates.y != 0 ? bestCoordinates.y : y);

        svgGenerator.setStroke(new BasicStroke(5));
        svgGenerator.drawLine(x, y, x, y+20);
        if (onlyOneHere) {
            Integer outgoingTransition = this.transitions.size();
            Integer next_x_offset = 1200 / 2 / outgoingTransition;
            svgGenerator.drawLine(next_x_offset, y + 20, 1200 - next_x_offset, y + 20);
            for (Integer x_it = next_x_offset; x_it <= 1200 - next_x_offset; x_it += (1200 - 2*next_x_offset)/(outgoingTransition-1)) {
                if (1200 - next_x_offset - x_it < 5) x_it = 1200 - next_x_offset;
                if (sqrt((x - x_it)^2) < 5) x_it = x;
                svgGenerator.drawLine(x_it, y + 20, x_it, y + 40);
                this.origines.add(new Point(x_it, y+40));
            }
        }
        else {
            svgGenerator.drawLine(x - x_offset / 4, y + 20, x + x_offset / 4, y + 20);
            for (Integer x_it = x - x_offset/4; x_it <= x + x_offset/4; x_it += 2*x_offset/4) {
                svgGenerator.drawLine(x_it, y + 20, x_it, y + 40);
                this.origines.add(new Point(x_it, y + 40));
            }
        }
        svgGenerator.setStroke(new BasicStroke(1));
    }
}
