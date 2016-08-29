import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.DOMImplementation;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Main {
    private static final Map<String, Boolean> booleanValues;
    static {
        Map<String, Boolean> map = new HashMap<>();
        map.put("skipPublicite", false);
        map.put("skipDCE", false);
        map.put("skipRegistreDepot", false);
        map.put("skipQuestionReponse", false);
        map.put("skipRegistreRetrait", false);
        map.put("skipCandidature", false);
        map.put("skipRecommendation", false);
        map.put("skipCalendrierReel", false);
        map.put("skipSuiviEchange", false);
        booleanValues = Collections.unmodifiableMap(map);
    }

    public static void main(String[] args) {
        try {
//            Open jBPM file
            File inputFile = new File("src/mapa_ouverte.xml");

//            Parse jBPM file and create corresponding objects
            SAXBuilder saxBuilder = new SAXBuilder();
            org.jdom2.Document document = saxBuilder.build(inputFile);
            Element processElement = document.getRootElement();
            List<Element> blockList = processElement.getChildren();
            List<Block> blocks = createObjectsFromXML(blockList);

//            Create new SVG output file
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
            String svgNS = "http://www.w3.org/2000/svg";
            org.w3c.dom.Document svgDocument =  domImpl.createDocument(svgNS, "svg", null);
            SVGGraphics2D svgGenerator = new SVGGraphics2D(svgDocument);
            svgGenerator.setFont(new Font("Arial", Font.CENTER_BASELINE, 8));

//            Add blocks to SVG file
            Main painter = new Main();
            painter.paint(svgGenerator, blocks);

//            Generate SVG output file
            Writer out = new OutputStreamWriter(openNewFile("output.svg"), "UTF-8");
            svgGenerator.stream(out, false);

        }catch(IOException | JDOMException e){
            e.printStackTrace();
        }
    }


    private void paint(Graphics2D svgGenerator, List<Block> blocks) {
        List<Block> activeBlocks = new ArrayList<>();
        StartState startState = (StartState) Main.getBlockFromName(blocks, "start-state");
        activeBlocks.add(startState);
        List<Block> blocksLeft = new ArrayList<>(blocks);
        List<Rectangle> rectangles = new ArrayList<>();

        Integer x_svg;
        Integer y_svg = 0;

        Integer x_offset;
        Integer y_offset = 150;

        while (!blocksLeft.isEmpty() && !activeBlocks.isEmpty()) {
//            Paint active blocks and remove them from block list and
//            explore new transitions from active blocks
            Set<Block> nextActiveBlocks = new HashSet<>();
            x_offset = 1200 / activeBlocks.size();
            x_svg = x_offset / 2;

            Main.computeBestCoordinates(blocks, activeBlocks);
            for (Block block: activeBlocks) {
                block.paint(svgGenerator, x_svg, y_svg, x_offset, activeBlocks.size() == 1, rectangles);
                block.setTransitionsEndpoints(blocks, blocksLeft);
                blocksLeft.remove(block);

                List<Transition> transitions = block.getTransitions();
                for (Transition transition: transitions) {
                    Block nextBlock = Main.getBlockFromName(blocksLeft, transition.getDirection());

                    if (nextBlock != null)
                        if (nextBlock.incArrivedTransition())
                            nextActiveBlocks.add(nextBlock);
                }

                x_svg += x_offset;
            }

            activeBlocks = new ArrayList<>(nextActiveBlocks);

            y_svg += y_offset;
        }

//        Once every block has been printed, add transitions into the mix
        if (startState != null) {
            startState.drawAllTransitions(svgGenerator, blocks, new HashSet<>(blocks), rectangles);
        }
    }

    private static void computeBestCoordinates(List<Block> blocks, List<Block> activeBlocks) {
        for (Block block: activeBlocks) {
            Set<Block> fathers = block.getUniqueFathers();
            if (!fathers.isEmpty()) {
                Integer best_x = 0;
                Integer realFathers = 0;
                for (Block father : fathers) {
                    Transition transition = father.getUniqueTransition(block.getName());
                    if (transition != null)
                        if (block.transitionMayBeUsed(blocks, transition, father)) {
                            Integer father_x = father.getUniqueOrigine().x;
                            transition.setOrigine(new Point(father_x, 0));
                            best_x += father_x;
                            realFathers++;
                        }
                }
                if (realFathers != 0)
                    best_x /= realFathers;
                block.setBestCoordinates(new Point(best_x, 0));
            }
        }
    }

    private static FileOutputStream openNewFile(String name) throws IOException {
        FileOutputStream fop = null;
        File file;

        try {
            file = new File(name);
            file.createNewFile();
            fop = new FileOutputStream(file, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  fop;
    }

    private static List<Block> createObjectsFromXML(List<Element> blockList) {

        List<Block> blocks = new ArrayList<>();
        Map<String, String> skipMap = new HashMap<>();

        for (Element element : blockList) {

            String transitionDirection = "";

//            Create blocks
            if ("start-state".equals(element.getName())) {
                StartState startState = new StartState();
                blocks.add(startState);
            } else if ("end-state".equals(element.getName())) {
                EndState endState = new EndState();
                blocks.add(endState);
            } else if ("state".equals(element.getName())) {
                State state = new State(element.getAttributeValue("name"));
                blocks.add(state);
            } else if ("fork".equals(element.getName())) {
//                Skip first fork named "forkHideScreens"
                // if (!"forkHideScreens".equals(element.getAttributeValue("name"))) {
                Fork fork = new Fork(element.getAttributeValue("name"));
                blocks.add(fork);
                //}
            } else if ("decision".equals(element.getName())) {
                Decision decision = new Decision(element.getAttributeValue("name"));
                blocks.add(decision);

                List<Element> handlers = element.getChildren();
                Attribute attribute = handlers.get(0).getAttributes().get(0);
                String handlerName = attribute.getValue();
                if (handlerName.contains("SkipStepDecisionHandler")) {
                    String boolVar = ((Element) handlers.get(0).getContent().get(1)).getContent().get(0).getValue();
                    if (booleanValues.get(boolVar)) {
//                            Follow "directionVrai" transition
                        Iterator<Element> iter = handlers.iterator();
                        while (iter.hasNext()) {
                            Element element_it = iter.next();
                            if ("directionVrai".equals(element_it.getAttributes().get(0).getValue())) {
                                transitionDirection = element_it.getAttributes().get(1).getValue();
                                skipMap.put(element.getAttributes().get(0).getValue(), transitionDirection);
                            } else iter.remove();
                        }
                    } else {
//                        Follow "directionFaux" transition
                        Iterator<Element> iter = handlers.iterator();
                        while (iter.hasNext()) {
                            Element element_it = iter.next();
                            if ("directionFaux".equals(element_it.getAttributes().get(0).getValue())) {
                                transitionDirection = element_it.getAttributes().get(1).getValue();
                                skipMap.put(element.getAttributes().get(0).getValue(), transitionDirection);
                            } else iter.remove();
                        }
                    }
                }
            }

//            Add transitions to newly created block
            Block newBlock = blocks.get(blocks.size() - 1);
            List<Element> children = element.getChildren();

            for (Element child : children)
                if ("transition".equals(child.getName())) {
                    Transition transition = new Transition(child.getAttributeValue("name"), newBlock.getName(), child.getAttributeValue("to"));
                    newBlock.addTransition(transition);
                }

        }

//        Update arriving transition number
        Iterator<Block> iter = blocks.iterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            if (skipMap.get(block.getName()) != null) {
                iter.remove();
                continue;
            }

            List<Transition> transitions = block.getTransitions();

            Iterator<Transition> iterator = transitions.iterator();
            while (iterator.hasNext()) {
                Transition transition = iterator.next();
                while (skipMap.get(transition.getDirection()) != null) {
                    transition.setDirection(skipMap.get(transition.getDirection()));
                }

                Block directionBlock = getBlockFromName(blocks, transition.getDirection());
                if (directionBlock != null) {
                    directionBlock.incArrivingTransition(blocks, transition, block);
                } else System.out.println("ERROR in block named " + block.getName() +
                        " due to transition named " + transition.getName() + " to " + transition.getDirection());
            }
        }

        return blocks;
    }


    static Block getBlockFromName(List<Block> blocks, String name) {
        for (Block block: blocks) {
            if (name.equals(block.getName()))
                return  block;
        }
        return null;
    }
}
