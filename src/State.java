import java.awt.*;

class State extends Block{

    private Point origine = new Point();

    State(String name) {
        super(name);
    }

    @Override
    void paint(Graphics2D svgGenerator, Integer x, Integer y, Integer x_offset, boolean onlyOneHere) {
        x = (bestCoordinates.x != 0 ? bestCoordinates.x : x);
        y = (bestCoordinates.y != 0 ? bestCoordinates.y : y);

        svgGenerator.draw(new Rectangle(x-70, y, 140, 40));
        this.printSimpleString(svgGenerator, this.name, 140, x-70, y + 24);
        this.origine.setLocation(x, y+40);
    }

    private void printSimpleString(Graphics2D g2d, String s, int width, int XPos, int YPos){
        if (s.length() > 27) s = s.substring(0, 27);

        int stringLen = (int)
                g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();
        int start = width/2 - stringLen/2;
        g2d.drawString(s, start + XPos, YPos);
    }
}
