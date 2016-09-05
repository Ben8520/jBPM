import java.awt.*;
import java.util.*;
import java.util.List;

class Fork extends Block {
    private Set<Point> origines = new LinkedHashSet<>();
    private Set<Point> alreadyGivenOrigines = new LinkedHashSet<>();

    Fork(String name) {
        super(name);
    }

    @Override
    List<Point> paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere, List<Rectangle> rectangles, List<Block> blocks) {
        Integer fork_offset = computeForkOffset(blocks);
        Point point = chooseAndUpdateCoordinates(x, y);
        Integer outgoingTransition = this.notFinalTransitions().size();

        if (outgoingTransition > 1)
            svgGenerator.setColor(Color.BLUE);
//        else if (outgoingTransition == 1)
//            svgGenerator.setColor(Color.MAGENTA);

        svgGenerator.setStroke(new BasicStroke(4));
        svgGenerator.drawLine(point.x, point.y-20, point.x, point.y);

        svgGenerator.drawLine(point.x - (outgoingTransition-1)*fork_offset, point.y, point.x + (outgoingTransition-1)*fork_offset, point.y);
//        if (outgoingTransition > 1)
//            svgGenerator.drawString(this.getName(), point.x + (outgoingTransition-1)*fork_offset - 100, point.y - 5);

        for (Integer x_it = point.x - (outgoingTransition-1)*fork_offset; x_it <= point.x + (outgoingTransition-1)*fork_offset; x_it += 2*fork_offset) {
            svgGenerator.drawLine(x_it, point.y, x_it, point.y+40);
            this.origines.add(new Point(x_it, point.y+40));
        }

        svgGenerator.setStroke(new BasicStroke(1));
        svgGenerator.setColor(Color.BLACK);

        return Arrays.asList(new Point(point.x - (outgoingTransition-1)*fork_offset, point.y - 20),
                new Point(point.x + (outgoingTransition-1)*fork_offset, point.y + 40));
    }

    private Integer computeForkOffset(List<Block> blocks) {
        Integer forkOffset = 350;

        for (Transition transition: transitions) {
            Block nextBlock = SvgGenerator.getBlockFromName(blocks, transition.getDirection());
            if (nextBlock instanceof Fork) {
                forkOffset += (nextBlock.notFinalTransitions().size() - 1) * 350;
            }
        }

        return (forkOffset > 350 ? forkOffset + 300 : 355);
    }

    @Override
    Point getUniqueOrigine(Integer current_x) {
        Integer min_dx = Integer.MAX_VALUE;
        Point origineToGive = new Point();
        for (Point origine: origines) {
            if (alreadyGivenOrigines.contains(origine)) continue;
            if (Math.abs(current_x - origine.x) < min_dx) {
                min_dx = Math.abs(current_x - origine.x);
                origineToGive = origine;
            }
        }

        if (origineToGive != new Point()) {
            alreadyGivenOrigines.add(origineToGive);
            return origineToGive;
        }

        return null;
    }

    @Override
    Point getFatherOrigine() {
        List<Point> origineList = new ArrayList<>(alreadyGivenOrigines);
        return origineList.get(origineList.size() - 1);
    }

    @Override
    void setTransitionsEndpoints(List<Block> blocks, List<Block> blocksLeft) {
        for (Block father: getFathers()) {
            Transition transition = father.getTransition(this);
            if (transition != null) {
                transition.setOrigine(father.getFatherOrigine());
                transition.setDestination(new Point(getBestCoordinates().x, getBestCoordinates().y - 19));
            }
        }

        for (Transition transition: transitions) {
            Block destination = SvgGenerator.getBlockFromName(blocks, transition.getDirection());
            if (!blocksLeft.contains(destination))
                transition.setOrigine(this.getBestCoordinates());
        }
    }

    void assignOrigineToChildren(List<Block> blocks) {
        Integer fork_offset = computeForkOffset(blocks);
        Point point = chooseAndUpdateCoordinates(0, getFathers().get(0).getBestCoordinates().y + 150);
        Integer outgoingTransition = this.notFinalTransitions().size();

        for (Integer x_it = point.x - (outgoingTransition - 1) * fork_offset; x_it <= point.x + (outgoingTransition - 1) * fork_offset; x_it += 2 * fork_offset) {
            this.origines.add(new Point(x_it, point.y + 40));
        }

        List<Block> children = new ArrayList<>();
        for (Transition transition : transitions) {
            Block nextBlock = SvgGenerator.getBlockFromName(blocks, transition.getDirection());
            if (!(nextBlock instanceof EndState) && nextBlock != null)
                children.add(nextBlock);
        }

        children = sonsFirst(children);

        for (Block child: children) {
            if (child instanceof EndState) continue;

            Set<Block> fathers = child.getUniqueFathers();
            if (!fathers.isEmpty()) {
                Integer best_x = 0;
                Integer realFathers = 0;
                for (Block father : fathers) {
                    if (!father.equals(this)) {
                        Transition transition = father.getUniqueTransition(child.getName());
                        if (transition != null)
                            if (child.transitionMayBeUsed(blocks, transition, father)) {
                                Integer father_x = (father.getUniqueOrigine(0).x == 0? father.getBestCoordinates().x : father.getUniqueOrigine(0).x);
                                transition.setOrigine(new Point(father_x, 0));
                                realFathers++;
                                best_x += father_x;
                            }
                    }
                }

                if (realFathers != 0)
                    best_x /= realFathers;

                Transition transition = this.getUniqueTransition(child.getName());
                if (transition != null)
                    if (child.transitionMayBeUsed(blocks, transition, this)) {
                        Integer father_x = this.getUniqueOrigine(best_x).x;
                        transition.setOrigine(new Point(father_x, this.getBestCoordinates().y + 40));
                        best_x *= realFathers;
                        best_x += father_x;
                        best_x /= realFathers + 1;
                    }
                child.setBestCoordinates(new Point(best_x, 0));
            }
        }
    }

    private List<Block> sonsFirst(List<Block> children) {

        Boolean dependency = false;
        for (Block child: children)
           for (Block block: children)
                if (child.getFathers().contains(block) && !child.equals(block))
                    dependency = true;

        if (!dependency) {
            Collections.sort(children, new Comparator<Block>() {
                @Override
                public int compare(Block o1, Block o2) {
                    return (o1.getUniqueFathers().size() > o2.getUniqueFathers().size() ? -1 : 1);
                }
            });
            return children;
        }


        List<Block> orderedChildren = new ArrayList<>();
        for (Block child: children)
            if (child.getFathers().size() == 1 && child.getFathers().get(0).equals(this))
                orderedChildren = addSmart(orderedChildren, child);


        assert orderedChildren.size() != 0;

        for (Block child: children) {
            if (orderedChildren.contains(child)) continue;
            Integer maxFatherIndex = 0;
            for (Block block: orderedChildren)
                if (child.getFathers().contains(block))
                    maxFatherIndex = orderedChildren.indexOf(block) + 1;
            orderedChildren = addSafe(orderedChildren, maxFatherIndex, child);

        }

        Collections.sort(children, new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                return (o1.getUniqueFathers().size() > o2.getUniqueFathers().size() ? -1 : 1);
            }
        });

        for (Block child: children)
            if (!orderedChildren.contains(child))
                orderedChildren.add(orderedChildren.size()-1, child);

        return orderedChildren;
    }

    private List<Block> addSafe(List<Block> orderedChildren, int i, Block child) {
        List<Block> retVal = new ArrayList<>(orderedChildren);
        retVal.add(i, child);
        return retVal;
    }

    private List<Block> addSmart(List<Block> orderedChildren, Block child) {
        List<Block> retVal = new ArrayList<>(orderedChildren);
        for (Block block: orderedChildren)
            if (block.getUniqueFathers().size() < child.getUniqueFathers().size())
                retVal.add(orderedChildren.indexOf(block), child);
        if (!retVal.contains(child)) retVal.add((retVal.size() == 0? 0 : retVal.size()-1), child);

        return retVal;
    }
}
