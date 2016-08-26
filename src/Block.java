import java.awt.*;
import java.util.*;
import java.util.List;

abstract class Block {

    final String name;
    private Integer arrivingTransitions;
    private Integer arrivedTransitions;
    List<Transition> transitions;
    private Set<Block> fathers;
    Point bestCoordinates;

    Block(String name) {
        this.name = name;
        this.arrivingTransitions = 0;
        this.arrivedTransitions = 0;
        this.transitions = new ArrayList<>();
        this.fathers= new HashSet<>();
        this.bestCoordinates = new Point();
    }

    boolean incArrivedTransition() {
        return Objects.equals(++this.arrivedTransitions, arrivingTransitions);
    }

    void incArrivingTransition(List<Block> blocks, Transition transition, Block father) {

        fathers.add(father);

        if (!transition.getName().contains("retour") && !transition.getName().contains("directionFaux"))
            ++this.arrivingTransitions;

        else if (transition.getName().contains("directionFaux")){
            HashSet<Block> sons = new HashSet<>();
            this.findSons(sons, blocks);

            if (!sons.contains(father))
               ++this.arrivingTransitions;
        }
    }

    private void findSons(HashSet<Block> sons, List<Block> blocks) {

        for (Transition transition: transitions) {
            Block directionBlock = Main.getBlockFromName(blocks, transition.getDirection());
            if (sons.add(directionBlock) && !transition.getName().contains("retour"))
                if (directionBlock != null) {
                    directionBlock.findSons(sons, blocks);
                }
        }
    }

    abstract void paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere);

    void drawAllTransitions(Graphics2D svgGenerator, HashSet<Block> blocksLeft) {
        blocksLeft.remove(this);
        for (Transition transition: transitions) {
            transition.paint(svgGenerator);
            List<Block> blockList = new ArrayList<>(blocksLeft);
            Block nextBlock = Main.getBlockFromName(blockList, transition.getDirection());
            if (blocksLeft.contains(nextBlock)) {
                nextBlock.drawAllTransitions(svgGenerator, blocksLeft);
            }
        }
    }

    void addTransition(Transition transition) {
        transitions.add(transition);
    }

    Set<Block> getFathers() {
        return fathers;
    }

    String getName() {
        return name;
    }

    List<Transition> getTransitions() {
        return transitions;
    }

    @Override
    public String toString() {
        return "Block{" +
                "name='" + name + '\'' +
                "transitions='" + transitions + '\'' +
                "number of arriving transitions='" + arrivingTransitions + "\'" +
                "}\n";
    }
}
