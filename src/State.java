import java.awt.*;

class State extends Block{

    private Point origine = new Point();

    State(String name) {
        super(name);
    }

    @Override
    void paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere) {
        Point point = chooseAndUpdateCoordinates(x, y);

        svgGenerator.setStroke(new BasicStroke(3));
        svgGenerator.draw(new Rectangle(point.x-70, point.y, 140, 40));
        this.printSimpleString(svgGenerator, this.name, 140, point.x-70, point.y + 24);
        svgGenerator.setStroke(new BasicStroke(1));
        this.origine.setLocation(point.x, point.y+40);
    }

    @Override
    Point getUniqueOrigine() {
        return origine;
    }

    private void printSimpleString(Graphics2D g2d, String s, int width, int XPos, int YPos){
        if (s.length() > 27) s = s.substring(0, 27);

        int stringLen = (int)
                g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();
        int start = width/2 - stringLen/2;
        g2d.drawString(s, start + XPos, YPos);
    }
}
