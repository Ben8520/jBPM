import java.awt.*;
import java.util.*;
import java.util.List;

class Fork extends Block {
    private Set<Point> origines = new LinkedHashSet<>();

    Fork(String name) {
        super(name);
    }

    @Override
    List<Point> paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere, List<Rectangle> rectangles) {
        Integer fork_offset = 350;
        Point point = chooseAndUpdateCoordinates(x, y);
        Integer outgoingTransition = this.notFinalTransitions().size();

        if (outgoingTransition > 1)
            svgGenerator.setColor(Color.BLUE);
        svgGenerator.setStroke(new BasicStroke(4));
        svgGenerator.drawLine(point.x, point.y-20, point.x, point.y);


        svgGenerator.drawLine(point.x - (outgoingTransition-1)*fork_offset, point.y, point.x + (outgoingTransition-1)*fork_offset, point.y);
        if (outgoingTransition > 1)
            svgGenerator.drawString(this.getName(), point.x + (outgoingTransition-1)*fork_offset - 100, point.y - 5);

        for (Integer x_it = point.x - (outgoingTransition-1)*fork_offset; x_it <= point.x + (outgoingTransition-1)*fork_offset; x_it += 2*fork_offset) {
            svgGenerator.drawLine(x_it, point.y, x_it, point.y+40);
            this.origines.add(new Point(x_it, point.y+40));
        }

        svgGenerator.setStroke(new BasicStroke(1));
        svgGenerator.setColor(Color.BLACK);

        return Arrays.asList(new Point(point.x - (outgoingTransition-1)*fork_offset, point.y - 20),
                new Point(point.x + (outgoingTransition-1)*fork_offset, point.y + 40));
    }

    @Override
    Point getUniqueOrigine() {
        List<Point> origineList = new ArrayList<>(origines);
        Point retVal = origineList.get(0);
        origines.remove(retVal);
        origines.add(retVal);
        return retVal;
    }

    @Override
    Point getFatherOrigine() {
        List<Point> origineList = new ArrayList<>(origines);
        return origineList.get(origineList.size() - 1);
    }

    Point getOrigine(int i) {
        List<Point> list = new ArrayList<>(origines);
        return list.get(i);
    }

    @Override
    void setTransitionsEndpoints(List<Block> blocks, List<Block> blocksLeft) {
        for (Block father: getFathers()) {
            Transition transition = father.getTransition(this);
            if (transition != null) {
                transition.setOrigine(father.getUniqueOrigine());
                transition.setDestination(new Point(getBestCoordinates().x, getBestCoordinates().y - 19));
            }
        }

        for (Transition transition: transitions) {
            Block destination = Main.getBlockFromName(blocks, transition.getDirection());
            if (!blocksLeft.contains(destination))
                transition.setOrigine(this.getBestCoordinates());
        }
    }
}
