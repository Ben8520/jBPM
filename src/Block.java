import org.jdom2.Element;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Block {

    private final String type;
    private final String name;
    private Integer arrivingTransitions;
    private Integer arrivedTransitions;
    private List<Element> events;
    private List<Transition> transitions;

    public Block(String type, String name) {
        this.type = type;
        this.name = name;
        this.arrivingTransitions = 0;
        this.arrivedTransitions = 0;
        this.events = new ArrayList<>();
        this.transitions = new ArrayList<>();
    }

    public boolean incArrivedTransition() {
        return ++this.arrivedTransitions == arrivingTransitions;
    }

    public void incArrivingTransition(List<Block> blocks, Transition transition, Block father) {

        if (!transition.getName().contains("retour") && !transition.getName().contains("directionFaux"))
            ++this.arrivingTransitions;

        else if (transition.getName().contains("directionFaux")){
            HashSet sons = new HashSet<Block>();
            this.findSons(sons, blocks);

            if (!sons.contains(father))
               ++this.arrivingTransitions;
        }
    }

    private void findSons(HashSet<Block> sons, List<Block> blocks) {

        for (Transition transition: transitions) {
            Block directionBlock = Main.getBlockFromName(blocks, transition.getDirection());
            if (sons.add(directionBlock) && !transition.getName().contains("retour"))
                directionBlock.findSons(sons, blocks);
        }
    }

    public void paint(Graphics2D svgGenerator, Integer x, Integer y) {
        switch (this.type) {
            case "state":
                svgGenerator.draw(new Rectangle(x-70, y, 140, 40));
                this.printSimpleString(svgGenerator, this.name, 140, x-70, y + 24);
                break;
            case "start-state":
                svgGenerator.fillOval(x-25, y, 50, 50);
                break;
            case "end-state":
                svgGenerator.setStroke(new BasicStroke(4));
                svgGenerator.drawOval(x-30, y-5, 50, 50);
                svgGenerator.fillOval(x-25, y, 40, 40);
                svgGenerator.setStroke(new BasicStroke(1));
                break;
            case "decision":
                svgGenerator.setStroke(new BasicStroke(3));
                svgGenerator.drawPolygon(new int[]{x, x + 20, x, x - 20}, new int[]{y + 20, y, y - 20, y}, 4);
                svgGenerator.setColor(Color.red);
                svgGenerator.fillPolygon(new int[]{x, x + 20, x, x - 20}, new int[]{y + 20, y, y - 20, y}, 4);
                svgGenerator.setStroke(new BasicStroke(1));
                svgGenerator.setColor(Color.black);
                break;
            default:

        }
    }

    private void printSimpleString(Graphics2D g2d, String s, int width, int XPos, int YPos){
        if (s.length() > 27) s = s.substring(0, 27);

        int stringLen = (int)
                g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();
        int start = width/2 - stringLen/2;
        g2d.drawString(s, start + XPos, YPos);
 }

    public void addTransition(Transition transition) {
        transitions.add(transition);
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<Transition> getTransitions() {
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
