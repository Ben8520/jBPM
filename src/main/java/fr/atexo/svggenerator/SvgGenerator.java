import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.DOMImplementation;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

class SvgGenerator {

    private final String jbpmFilePath;
    private final String svgOutputFilePath;
    private Map<String, Boolean> booleanValues;

    SvgGenerator(String jbpmFilePath, Map<String, Boolean> booleanValues) {
        this.jbpmFilePath = jbpmFilePath;
        this.svgOutputFilePath = jbpmFilePath.substring(0, jbpmFilePath.indexOf(".")) + ".svg";
        this.booleanValues = booleanValues;
    }

    void createSvgOutput() {
        try {
//            Open jBPM file
            File inputFile = new File(jbpmFilePath);

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
            svgGenerator.setFont(new Font("Arial", Font.CENTER_BASELINE, 28));

//            Add blocks to SVG file
            SvgGenerator painter = new SvgGenerator(jbpmFilePath, booleanValues);
            List<Point> points = painter.paint(svgGenerator, blocks);

//            Generate SVG output file
            Writer out = new OutputStreamWriter(openNewFile(svgOutputFilePath), "UTF-8");
            svgGenerator.stream(out, false);

            updateFileConfiguration(points, Paths.get(svgOutputFilePath));

        }catch(IOException | JDOMException e){
            e.printStackTrace();
        }
    }

    private static void updateFileConfiguration(List<Point> points, Path path) throws IOException {
        Integer width = points.get(1).x - points.get(0).x + 300;
        Integer height = points.get(1).y - points.get(0).y + 200;

        Integer x_min = points.get(0).x - 150;
        Integer y_min = points.get(0).y - 100;

        Integer translate_x = -(points.get(0).x) + 150;
        Integer translate_y = -(points.get(0).y) + 100;

        List<String> svgContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
        svgContent.set(3, svgContent.get(3) + " width=\"" + width +
                "\" height=\"" + height + "\"");
        svgContent.set(5,"/><g transform='translate(" + translate_x + " " + translate_y + ")'" );
        svgContent.set(7, "><rect fill=\"white\" x=\"" + x_min + "\" y=\"" + y_min + "\" " +
                "width=\"" + width + "\" height=\"" + height + "\"/" + svgContent.get(7).substring(4));
        Files.write(path, svgContent, StandardCharsets.UTF_8);
    }

    private List<Point> paint(Graphics2D svgGenerator, List<Block> blocks) {
        Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        Point max = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

        List<Block> activeBlocks = new ArrayList<>();
        StartState startState = (StartState) getBlockFromName(blocks, "start-state");
        activeBlocks.add(startState);
        List<Block> blocksLeft = new ArrayList<>(blocks);
        List<Rectangle> rectangles = new ArrayList<>();

        Integer x_svg;
        Integer y_svg = 0;

        Integer x_offset;
        Integer y_offset = 250;

        while (!blocksLeft.isEmpty() && !activeBlocks.isEmpty()) {
//            Paint active blocks and remove them from block list and
//            explore new transitions from active blocks
            Set<Block> nextActiveBlocks = new HashSet<>();
            x_offset = 1200 / activeBlocks.size();
            x_svg = x_offset / 2;

            if (activeBlocks.size() > 1)
                Collections.sort(activeBlocks, new Comparator<Block>() {
                    @Override
                    public int compare(Block b1, Block b2) {
                        if (b1.notFinalTransitions().size() < b2.notFinalTransitions().size())
                            return -1;
                        else if (b1.notFinalTransitions().size() == b2.notFinalTransitions().size()) {
                            if (b1 instanceof Fork) return 1;
                            if (b2 instanceof Fork) return -1;
                        }
                        return 1;
                    }
                });
            for (Block block: activeBlocks) {
                computeBestCoordinates(blocks, block);
                List<Point> maxs = block.paint(svgGenerator, x_svg, y_svg, x_offset, activeBlocks.size() == 1, rectangles, blocks);
                min = smallerOne(maxs.get(0), min);
                max = greaterOne(maxs.get(1), max);
                block.setTransitionsEndpoints(blocks, blocksLeft);
                blocksLeft.remove(block);

                Set<Transition> transitions = block.getTransitions();
                for (Transition transition: transitions) {
                    Block nextBlock = SvgGenerator.getBlockFromName(blocksLeft, transition.getDirection());

                    if (nextBlock != null) {
                        if (nextBlock.incArrivedTransition())
                            nextActiveBlocks.add(nextBlock);
                    } else if (SvgGenerator.getBlockFromName(blocks, transition.getDirection()) == null && !transition.getDirection().equals("end"))
                        System.err.println("Block named " + transition.getDirection() + " not found..");

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

        return Arrays.asList(min, max);
    }

    private List<Block> createObjectsFromXML(List<Element> blockList) {

        List<Block> blocks = new ArrayList<>();
        Map<String, String> skipMap = new HashMap<>();

        for (Element element : blockList) {

            String transitionDirection;

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
                Fork fork = new Fork(element.getAttributeValue("name"));
                blocks.add(fork);
            } else if ("decision".equals(element.getName())) {
                Decision decision = new Decision(element.getAttributeValue("name"));
                blocks.add(decision);

                List<Element> handlers = element.getChildren();
                Attribute attribute = handlers.get(0).getAttributes().get(0);
                String handlerName = attribute.getValue();
                if (handlerName.contains("SkipStepDecisionHandler")) {
                    String boolVar = ((Element) handlers.get(0).getContent().get(1)).getContent().get(0).getValue();
                    decision.setParameter(boolVar);
                    if (this.booleanValues.get(boolVar)) {
//                            Follow "directionVrai" transition
                        Iterator<Element> iter = handlers.iterator();
                        while (iter.hasNext()) {
                            Element element_it = iter.next();
                            if ("directionVrai".equals(element_it.getAttributes().get(0).getValue())) {
                                transitionDirection = element_it.getAttributes().get(1).getValue();
                                skipMap.put(element.getAttributes().get(0).getValue(), transitionDirection);
                            }
                        }
                    } else {
//                            Follow "directionFaux" transition
                        Iterator<Element> iter = handlers.iterator();
                        while (iter.hasNext()) {
                            Element element_it = iter.next();
                            if ("directionFaux".equals(element_it.getAttributes().get(0).getValue())) {
                                transitionDirection = element_it.getAttributes().get(1).getValue();
                                skipMap.put(element.getAttributeValue("name"), transitionDirection);
                            }
                        }
                    }
                } else if (handlerName.contains("ContinuerDecisionHandler")) {
                    for (Element element_it: handlers) {
                        if (element_it.getAttributes().get(0).getValue().contains("continuer")) {
                            transitionDirection = element_it.getAttributes().get(1).getValue();
                            skipMap.put(element.getAttributes().get(0).getValue(), transitionDirection);
                        }
                    }
                } else if (element.getAttributeValue("name").contains("join")) {
                    for (Element element_it: handlers) {
                        if (element_it.getAttributes().get(0).getValue().contains("continuer")) {
                            transitionDirection = element_it.getAttributes().get(1).getValue();
                            skipMap.put(element.getAttributes().get(0).getValue(), transitionDirection);
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

            Set<Transition> transitions = block.getTransitions();

            Iterator<Transition> iterator = transitions.iterator();
            while (iterator.hasNext()) {
                Transition transition = iterator.next();
                while (skipMap.get(transition.getDirection()) != null) {
                    setBlockSkipped(blocks, SvgGenerator.getBlockFromName(blocks, transition.getDirection()), skipMap.get(transition.getDirection()));
                    transition.setDirection(skipMap.get(transition.getDirection()));
                }
            }
        }

        iter = blocks.iterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            if (block.getSkipped()) {
                iter.remove();
                continue;
            }

            Set<Transition> transitions = block.reloadTransition();

            Iterator<Transition> iterator = transitions.iterator();
            while (iterator.hasNext()) {
                Transition transition = iterator.next();
                Block directionBlock = getBlockFromName(blocks, transition.getDirection());
                if (directionBlock != null) {
                    directionBlock.incArrivingTransition(blocks, transition, block);
                } else System.err.println("ERROR in block named " + block.getName() +
                        " due to transition named " + transition.getName() + " to " + transition.getDirection());
            }


        }

        blocks = removeUnvisitedBlocks(blocks);

        return blocks;
    }

    private void setBlockSkipped(List<Block> blocks, Block block, String finalDestination) {
        if (block == null)
            return;

        Set<Block> activeBlocks = new HashSet<>();
        Set<Block> doneBlocks = new HashSet<>();
//        If booleanValue is false, just skip decision
        if (block instanceof Decision) {
            Boolean boolVal = this.booleanValues.get(((Decision) block).getParameter());
            if (boolVal != null && !boolVal) {
                block.setSkipped(Boolean.TRUE);
                return;
            }

            if (block.transitions.size() > 2) {
                block.setSkipped(Boolean.TRUE);
                return;
            }
        }

        for (Transition transition: block.getTransitions())
            if (!transition.getDirection().equals(finalDestination))
                activeBlocks.add(getBlockFromName(blocks, transition.getDirection()));

        while ((activeBlocks.size() != 1 || !activeBlocks.contains(SvgGenerator.getBlockFromName(blocks, finalDestination))) && !activeBlocks.isEmpty()) {
            Set<Block> nextBlocks = new HashSet<>();
            for (Block block_it: activeBlocks) {
                if (!block_it.equals(SvgGenerator.getBlockFromName(blocks, finalDestination))) block_it.setSkipped(Boolean.TRUE);

                doneBlocks.add(block_it);

                if (block_it.getTransitions().isEmpty() && !doneBlocks.contains(SvgGenerator.getBlockFromName(blocks, "end"))) nextBlocks.add(SvgGenerator.getBlockFromName(blocks, "end"));
                else
                    for (Transition transition_it: block_it.getTransitions())
                        if (block_it.equals(SvgGenerator.getBlockFromName(blocks, finalDestination))) nextBlocks.add(block_it);
                        else if (!doneBlocks.contains(SvgGenerator.getBlockFromName(blocks, transition_it.getDirection())))
                            nextBlocks.add(SvgGenerator.getBlockFromName(blocks, transition_it.getDirection()));
            }
            activeBlocks = nextBlocks;
        }
    }

    private static List<Block> removeUnvisitedBlocks(List<Block> blocks) {
        List<Block> cleanBlocks = new ArrayList<>();

        for (Block block: blocks)
            if (block.getArrivingTransitions() != 0 || block instanceof StartState)
                cleanBlocks.add(block);

        return cleanBlocks;
    }

    private void computeBestCoordinates(List<Block> blocks, Block block) {
        if (block instanceof EndState) return;

        Set<Block> fathers = block.getUniqueFathers();
        if (!fathers.isEmpty()) {
            Integer best_x = 0;
            Integer realFathers = 0;
            for (Block father : fathers) {
                Transition transition = father.getUniqueTransition(block.getName());
                if (transition != null)
                    if (block.transitionMayBeUsed(blocks, transition, father)) {
                        Integer father_x = father.getUniqueOrigine(0).x;
                        transition.setOrigine(new Point(father_x, 0));
                        best_x += father_x;
                        realFathers++;
                    }
            }

            if (realFathers != 0)
                best_x /= realFathers;
            block.setBestCoordinates(new Point(best_x, 0));
        }
        if (block instanceof Fork && block.transitions.size() > 1) {
            ((Fork)block).assignOrigineToChildren(blocks);
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

    static Block getBlockFromName(List<Block> blocks, String name) {
        for (Block block: blocks) {
            if (name.equals(block.getName()))
                return  block;
        }
        return null;
    }

    private Point smallerOne(Point point, Point min) {
        return new Point(point.x < min.x ? point.x : min.x, point.y < min.y ? point.y : min.y);
    }

    private Point greaterOne(Point point, Point max) {
        return new Point(point.x > max.x ? point.x : max.x, point.y > max.y ? point.y : max.y);
    }

}
