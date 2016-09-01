import java.awt.*;
import java.util.*;
import java.util.List;

abstract class Block {

    final String name;
    private Integer arrivingTransitions;
    private Integer arrivedTransitions;
    Set<Transition> transitions;
    private List<Block> fathers;
    private Point bestCoordinates;
    private Boolean skipped;

    Block(String name) {
        this.name = name;
        this.arrivingTransitions = 0;
        this.arrivedTransitions = 0;
        this.transitions = new HashSet<>();
        this.fathers= new ArrayList<>();
        this.bestCoordinates = new Point();
        this.skipped = false;
    }

    boolean incArrivedTransition() {
        return Objects.equals(++this.arrivedTransitions, arrivingTransitions);
    }

    void incArrivingTransition(List<Block> blocks, Transition transition, Block father) {

        fathers.add(father);

        if (transitionMayBeUsed(blocks, transition, father))
            ++arrivingTransitions;
    }

    boolean transitionMayBeUsed(List<Block> blocks, Transition transition, Block father) {
        if (transition.getName() == null)
            return  true;

        if (transition.getDirection().equals(father.name))
            return false;

        if(!transition.getName().contains("retour") && !transition.getName().contains("directionFaux"))
            return  true;

        if (transition.getName().contains("directionFaux")){
            HashSet<Block> sons = new HashSet<>();
            this.findSons(sons, blocks);

            if (!sons.contains(father))
                return true;
        }

        return false;
    }

    private void findSons(HashSet<Block> sons, List<Block> blocks) {

        for (Transition transition: transitions) {
            Block directionBlock = SvgGenerator.getBlockFromName(blocks, transition.getDirection());
            if (sons.add(directionBlock)) {
                if (transition.getName() != null) {
                    if (!transition.getName().contains("retour") && !transition.getName().contains("directionFaux"))
                        if (directionBlock != null)
                            directionBlock.findSons(sons, blocks);
                } else {
                    if (directionBlock != null)
                        directionBlock.findSons(sons, blocks);
                }
            }
        }
    }

    abstract List<Point> paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere, List<Rectangle> rectangles, List<Block> blocks);

    List<Transition> notFinalTransitions() {
        List<Transition> list = new ArrayList<>();
        for (Transition transition: transitions)
            if (!"end".equals(transition.getDirection()))
                list.add(transition);

        return list;
    }

    void drawAllTransitions(Graphics2D svgGenerator, List<Block> blocks, HashSet<Block> blocksLeft, List<Rectangle> rectangles) {
        blocksLeft.remove(this);
        for (Transition transition: transitions) {
            transition.paint(svgGenerator, rectangles, blocks);
            List<Block> blockList = new ArrayList<>(blocksLeft);
            Block nextBlock = SvgGenerator.getBlockFromName(blockList, transition.getDirection());
            if (blocksLeft.contains(nextBlock)) {
                if (nextBlock != null) {
                    nextBlock.drawAllTransitions(svgGenerator, blocks, blocksLeft, rectangles);
                }
            }
        }
    }

    void addTransition(Transition transition) {
        transitions.add(transition);
    }

    Set<Transition> reloadTransition() {
        this.transitions = new HashSet<>(transitions);
        return transitions;
    }

    Set<Block> getUniqueFathers() {
        return new HashSet<>(fathers);
    }

    List<Block> getFathers() {
        return fathers;
    }

    String getName() {
        return name;
    }

    Set<Transition> getTransitions() {
        return transitions;
    }

    Transition getUniqueTransition(String direction) {
        for (Transition transition: transitions) {
            if (transition.getDirection().equals(direction))
                return transition;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Block{" +
                "name='" + name + '\'' +
                "transitions='" + transitions + '\'' +
                "number of arriving transitions='" + arrivingTransitions + "\'" +
                "}\n";
    }

    abstract Point getUniqueOrigine(Integer current_x);

    Point chooseAndUpdateCoordinates(Integer x, Integer y) {
        x = (bestCoordinates.x != 0 ? bestCoordinates.x : x);
        bestCoordinates.setLocation(x, y);
        return bestCoordinates;
    }

    void setTransitionsEndpoints(List<Block> blocks, List<Block> blocksLeft) {
        for (Block father: fathers) {
            Transition transition = father.getTransition(this);
            if (transition != null) {
                transition.setOrigine(father.getFatherOrigine());
                transition.setDestination(new Point(bestCoordinates.x, bestCoordinates.y));
            }
        }

        for (Transition transition: transitions) {
            Block destination = SvgGenerator.getBlockFromName(blocks, transition.getDirection());
            if (!blocksLeft.contains(destination))
                transition.setOrigine(this.bestCoordinates);
        }
    }

    Point getFatherOrigine() {
        return this.getUniqueOrigine(0);
    }

    Transition getTransition(Block block) {
        for (Transition transition: transitions) {
            if (transition.getDirection().equals(block.getName()))
                return transition;
        }
        return null;
    }

    void setBestCoordinates(Point bestCoordinates) {
        this.bestCoordinates.x = (this.bestCoordinates.x == 0 ? bestCoordinates.x : this.bestCoordinates.x);
        this.bestCoordinates.y = (this.bestCoordinates.y == 0 ? bestCoordinates.y : this.bestCoordinates.y);
    }

    void overideBestCoordinates(Point bestCoordinates) {
        this.bestCoordinates = bestCoordinates;
    }

    Point getBestCoordinates() {
        return bestCoordinates;
    }

    Integer getArrivingTransitions() {
        return arrivingTransitions;
    }

    Boolean getSkipped() {
        return skipped;
    }

    void setSkipped(Boolean skipped) {
        this.skipped = skipped;
    }
}
