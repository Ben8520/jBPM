import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.DOMImplementation;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

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
        activeBlocks.add(Main.getBlockFromName(blocks, "start-state"));

        Integer x_svg = 0;
        Integer y_svg = 0;

        Integer x_offset = 0;
        Integer y_offset = 150;

        while (!blocks.isEmpty()) {
//            Paint active blocks and remove them from block list and
//            explore new transitions from active blocks
            Set<Block> nextActiveBlocks = new HashSet<>();
            x_offset = 1200 / activeBlocks.size();
            x_svg = x_offset / 2;
            for (Block block: activeBlocks) {
                block.paint(svgGenerator, x_svg, y_svg);
                blocks.remove(block);

                List<Transition> transitions = block.getTransitions();
                for (Transition transition: transitions) {
                    Block nextBlock = Main.getBlockFromName(blocks, transition.getDirection());

                    if (nextBlock != null)
                        if (nextBlock.incArrivedTransition())
                            nextActiveBlocks.add(nextBlock);
                }

                x_svg += x_offset;
            }

            activeBlocks = new ArrayList<>(nextActiveBlocks);

            y_svg += y_offset;
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

        for (Element element: blockList) {

//            Create blocks
            if ("start-state".equals(element.getName())) {
                StartState startState = new StartState();
                blocks.add(startState);
            } else if ("end-state".equals(element.getName())) {
                EndState endState = new EndState();
                blocks.add(endState);
            } else {
                Block block = new Block(element.getName(), element.getAttributeValue("name"));
                blocks.add(block);
            }

//            Add transitions to newly created block
            Block newBlock = blocks.get(blocks.size() - 1);
            List<Element> children = element.getChildren();

            for (Element child: children)
                if ("transition".equals(child.getName())) {
                    Transition transition = new Transition(child.getAttributeValue("name"), child.getAttributeValue("to"));
                    newBlock.addTransition(transition);
                }
        }

//        Update arriving transition number
        for (Block block: blocks) {
            List<Transition> transitions = block.getTransitions();

            for (Transition transition: transitions) {
                Block directionBlock = getBlockFromName(blocks, transition.getDirection());
                if (directionBlock != null) {
                    directionBlock.incArrivingTransition(blocks, transition, block);
                } else System.out.println("ERROR in block named " + block.getName() +
                        " due to transition named " + transition.getName());
            }
        }

        return blocks;
    }

    public static Block getBlockFromName(List<Block> blocks, String name) {
        for (Block block: blocks) {
            if (name.equals(block.getName()))
                return  block;
        }
        return null;
    }
}
