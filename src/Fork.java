import java.awt.*;
import java.util.*;
import java.util.List;

import static java.lang.Math.sqrt;

class Fork extends Block {
    private Set<Point> origines = new LinkedHashSet<>();

    Fork(String name) {
        super(name);
    }

    @Override
    void paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere) {
        Point point = chooseAndUpdateCoordinates(x, y);

        svgGenerator.setColor(Color.BLUE);
        svgGenerator.setStroke(new BasicStroke(4));
        svgGenerator.drawLine(point.x, point.y, point.x, point.y+20);
        if (onlyOneHere) {
            Integer outgoingTransition = this.transitions.size();
            Integer next_x_offset = 1200 / 2 / outgoingTransition;
            svgGenerator.drawLine(next_x_offset, point.y + 20, 1200 - next_x_offset, point.y + 20);
            for (Integer x_it = next_x_offset; x_it <= 1220 - next_x_offset; x_it += (1200 - 2*next_x_offset)/(outgoingTransition-1)) {
                if (1200 - next_x_offset - x_it < 5) x_it = 1200 - next_x_offset;
                if (sqrt((point.x - x_it)^2) < 5) x_it = point.x;
                svgGenerator.drawLine(x_it, point.y + 20, x_it, point.y + 40);
                this.origines.add(new Point(x_it, point.y+40));
            }
        }
        else {
            svgGenerator.drawLine(point.x - x_offset / 4, point.y + 20, point.x + x_offset / 4, point.y + 20);
            for (Integer x_it = point.x - x_offset/4; x_it <= point.x + x_offset/4; x_it += 2*x_offset/4) {
                svgGenerator.drawLine(x_it, point.y + 20, x_it, point.y + 40);
                this.origines.add(new Point(x_it, point.y + 40));
            }
        }
        svgGenerator.setStroke(new BasicStroke(1));
        svgGenerator.setColor(Color.BLACK);
    }

    @Override
    Point getUniqueOrigine() {
        List<Point> origineList = new ArrayList<>(origines);
        Point retVal = origineList.get(0);
        origines.remove(retVal);
        origines.add(retVal);
        return retVal;
    }

    Point getOrigine(int i) {
        List<Point> list = new ArrayList<>(origines);
        return list.get(i);
    }
}
