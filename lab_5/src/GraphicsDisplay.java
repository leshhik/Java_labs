import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;

public class GraphicsDisplay extends JPanel
{
    private ArrayList<Double[]> graphicsData;
    private ArrayList<Double[]> originalData;
    private int selectedMarker;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double viewport[][];
    private ArrayList<double[][]> undoHistory;
    private double scaleX;
    private double scaleY;
    private BasicStroke axisStroke;
    private BasicStroke gridStroke;
    private BasicStroke markerStroke;
    private BasicStroke selectionStroke;
    private Font axisFont;
    private Font labelsFont;
    private static DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance();
    private boolean scaleMode;
    private boolean changeMode;
    private double originalPoint[];
    private Rectangle2D.Double selectionRect;

    public class MouseHandler extends MouseAdapter
    {

        public void mouseClicked(MouseEvent ev) //клавиша нажата и  затем отпущена
        {
            if(ev.getButton() == 3)
            {
                if(undoHistory.size() > 0)
                {
                    viewport = (double[][])undoHistory.get(undoHistory.size() - 1);
                    undoHistory.remove(undoHistory.size() - 1);
                }
                else
                {
                    zoomToRegion(minX, maxY, maxX, minY);
                }
                repaint();
            }
        }

        public void mousePressed(MouseEvent ev) //клавиша зажата
        {
            if(ev.getButton() != 1)
                return;
            selectedMarker = findSelectedPoint(ev.getX(), ev.getY());
            originalPoint = translatePointToXY(ev.getX(), ev.getY());
            if(selectedMarker >= 0)
            {
                changeMode = true;
                setCursor(Cursor.getPredefinedCursor(8));
            }
            else
            {
                scaleMode = true;
                setCursor(Cursor.getPredefinedCursor(5));
                selectionRect.setFrame(ev.getX(), ev.getY(), 1.0D, 1.0D);
            }
        }

        public void mouseReleased(MouseEvent ev) //клавиша отжата
        {
            if(ev.getButton() != 1)
                return;
            setCursor(Cursor.getPredefinedCursor(0));
            if(changeMode)
            {
                changeMode = false;
            }
            else
            {
                scaleMode = false;
                double finalPoint[] = translatePointToXY(ev.getX(), ev.getY());
                undoHistory.add(viewport);
                viewport = new double[2][2];
                zoomToRegion(originalPoint[0], originalPoint[1], finalPoint[0], finalPoint[1]);
                repaint();
            }
        }

        final GraphicsDisplay this$0;
        public MouseHandler()
        {
            this$0 = GraphicsDisplay.this;
        }
    }

    public class MouseMotionHandler implements MouseMotionListener
    {

        public void mouseMoved(MouseEvent ev) //движение мыши, пока клавиша не зажата
        {
            selectedMarker = findSelectedPoint(ev.getX(), ev.getY());
            if(selectedMarker >= 0)
                setCursor(Cursor.getPredefinedCursor(8));
            else
                setCursor(Cursor.getPredefinedCursor(0));
            repaint();
        }

        public void mouseDragged(MouseEvent ev) //движение мыши, пока клавиша зажата
        {
            if(changeMode)
            {
                double currentPoint[] = translatePointToXY(ev.getX(), ev.getY());
                double newY = ((Double[])graphicsData.get(selectedMarker))[1].doubleValue() + (currentPoint[1] - ((Double[])graphicsData.get(selectedMarker))[1].doubleValue());
                if(newY > viewport[0][1])
                    newY = viewport[0][1];
                if(newY < viewport[1][1])
                    newY = viewport[1][1];
                ((Double[])graphicsData.get(selectedMarker))[1] = Double.valueOf(newY);
                repaint();
            }
            else
            {
                double width = (double)ev.getX() - selectionRect.getX();
                if(width < 5D)
                    width = 5D;
                double height = (double)ev.getY() - selectionRect.getY();
                if(height < 5D)
                    height = 5D;
                selectionRect.setFrame(selectionRect.getX(), selectionRect.getY(), width, height);
                repaint();
            }
        }

        final GraphicsDisplay this$0;
        public MouseMotionHandler()
        {
            this$0 = GraphicsDisplay.this;
        }
    }

    public GraphicsDisplay()
    {
        selectedMarker = -1;
        viewport = new double[2][2];
        undoHistory = new ArrayList<double[][]>(5);
        scaleMode = false;
        changeMode = false;
        originalPoint = new double[2];
        selectionRect = new Rectangle2D.Double();
        setBackground(Color.WHITE);
        axisStroke = new BasicStroke(2.0F, 0, 0, 10F, null, 0.0F);
        gridStroke = new BasicStroke(1.0F, 0, 0, 10F, null, 0.0F);
        markerStroke = new BasicStroke(1.0F, 0, 0, 10F, null, 0.0F);
        selectionStroke = new BasicStroke(1.0F, 0, 0, 10F, new float[] {10F, 10F}, 0.0F);
        axisFont = new Font("Serif", 1, 36);
        labelsFont = new Font("Serif", 0, 10);
        formatter.setMaximumFractionDigits(5);
        addMouseListener(new MouseHandler());
        addMouseMotionListener(new MouseMotionHandler());
    }

    @SuppressWarnings("removal")
    public void displayGraphics(ArrayList<Double[]> graphicsData)
    {
        this.graphicsData = graphicsData;
        originalData = new ArrayList<Double[]>(graphicsData.size());
        Double newPoint[];
        for(Iterator<Double[]> iterator = graphicsData.iterator(); iterator.hasNext(); originalData.add(newPoint))
        {
            Double point[] = (Double[])iterator.next();
            newPoint = new Double[2];
            newPoint[0] = new Double(point[0].doubleValue());
            newPoint[1] = new Double(point[1].doubleValue());
        }
        minX = ((Double[])graphicsData.get(0))[0].doubleValue();
        maxX = ((Double[])graphicsData.get(graphicsData.size() - 1))[0].doubleValue();
        minY = ((Double[])graphicsData.get(0))[1].doubleValue();
        maxY = minY;
        for(int i = 1; i < graphicsData.size(); i++)
        {
            if(((Double[])graphicsData.get(i))[1].doubleValue() < minY)
                minY = ((Double[])graphicsData.get(i))[1].doubleValue();
            if(((Double[])graphicsData.get(i))[1].doubleValue() > maxY)
                maxY = ((Double[])graphicsData.get(i))[1].doubleValue();
        }
        zoomToRegion(minX, maxY, maxX, minY);
    }

    public void zoomToRegion(double x1, double y1, double x2, double y2)
    {
        viewport[0][0] = x1;
        viewport[0][1] = y1;
        viewport[1][0] = x2;
        viewport[1][1] = y2;
        repaint();
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        scaleX = getSize().getWidth() / (viewport[1][0] - viewport[0][0]);
        scaleY = getSize().getHeight() / (viewport[0][1] - viewport[1][1]);
        if(graphicsData == null || graphicsData.size() == 0)
        {
            return;
        }
        else
        {
            Graphics2D canvas = (Graphics2D)g;
            paintGrid(canvas);
            paintAxis(canvas);
            paintGraphics(canvas);
            paintMarkers(canvas);
            paintLabels(canvas);
            paintSelection(canvas);
            return;
        }
    }

    private void paintSelection(Graphics2D canvas)
    {
        if(!scaleMode)
            return;
        else
        {
            canvas.setStroke(selectionStroke);
            canvas.setColor(Color.BLACK);
            canvas.draw(selectionRect);
            return;
        }
    }

    private void paintGraphics(Graphics2D canvas)
    {
        canvas.setStroke(markerStroke);
        canvas.setColor(Color.RED);
        Double currentX = null;
        Double currentY = null;
        for(Iterator<Double[]> iterator = graphicsData.iterator(); iterator.hasNext();)
        {
            Double point[] = (Double[])iterator.next();
            if(point[0].doubleValue() >= viewport[0][0] && point[1].doubleValue() <= viewport[0][1] && point[0].doubleValue() <= viewport[1][0] && point[1].doubleValue() >= viewport[1][1])
            {
                if(currentX != null && currentY != null)
                    canvas.draw(new Line2D.Double(translateXYtoPoint(currentX.doubleValue(), currentY.doubleValue()), translateXYtoPoint(point[0].doubleValue(), point[1].doubleValue())));
                currentX = point[0];
                currentY = point[1];
            }
        }

    }

    private void paintMarkers(Graphics2D canvas)
    {
        canvas.setStroke(markerStroke);
        canvas.setColor(Color.RED);
        canvas.setPaint(Color.RED);
        Ellipse2D.Double lastMarker = null;
        int i = -1;
        for(Iterator<Double[]> iterator = graphicsData.iterator(); iterator.hasNext();)
        {
            Double point[] = (Double[])iterator.next();
            i++;
            if(point[0].doubleValue() >= viewport[0][0] && point[1].doubleValue() <= viewport[0][1] && point[0].doubleValue() <= viewport[1][0] && point[1].doubleValue() >= viewport[1][1])
            {
                int radius;
                if(i == selectedMarker)
                    radius = 8;
                else
                    radius = 4;
                Ellipse2D.Double marker = new Ellipse2D.Double();
                Point2D center = translateXYtoPoint(point[0].doubleValue(), point[1].doubleValue());
                Point2D corner = new Point2D.Double(center.getX() + (double)radius, center.getY() + (double)radius);
                marker.setFrameFromCenter(center, corner);
                if(i == selectedMarker)
                {
                    lastMarker = marker;
                }
                else
                {
                    canvas.draw(marker);
                    canvas.fill(marker);
                }
            }
        }
        if(lastMarker != null)
        {
            canvas.setColor(Color.BLUE);
            canvas.setPaint(Color.BLUE);
            canvas.draw(lastMarker);
            canvas.fill(lastMarker);
        }
    }

    private void paintLabels(Graphics2D canvas)
    {
        canvas.setColor(Color.BLACK);
        canvas.setFont(labelsFont);
        FontRenderContext context = canvas.getFontRenderContext();
        double labelYPos;
        if(viewport[1][1] < 0.0D && viewport[0][1] > 0.0D)
            labelYPos = 0.0D;
        else
            labelYPos = viewport[1][1];
        double labelXPos;
        if(viewport[0][0] < 0.0D && viewport[1][0] > 0.0D)
            labelXPos = 0.0D;
        else
            labelXPos = viewport[0][0];
        double pos = viewport[0][0];
        double step = (viewport[1][0] - viewport[0][0]) / 10D;
        for(; pos < viewport[1][0]; pos += step)
        {
            Point2D.Double point = translateXYtoPoint(pos, labelYPos);
            String label = formatter.format(pos);
            Rectangle2D bounds = labelsFont.getStringBounds(label, context);
            canvas.drawString(label, (float)(point.getX() + 5D), (float)(point.getY() - bounds.getHeight()));
        }
        pos = viewport[1][1];
        step = (viewport[0][1] - viewport[1][1]) / 10D;
        for(; pos < viewport[0][1]; pos += step)
        {
            Point2D.Double point = translateXYtoPoint(labelXPos, pos);
            String label = formatter.format(pos);
            Rectangle2D bounds = labelsFont.getStringBounds(label, context);
            canvas.drawString(label, (float)(point.getX() + 5D), (float)(point.getY() - bounds.getHeight()));
        }
        if(selectedMarker >= 0)
        {
            Point2D.Double point = translateXYtoPoint(((Double[])graphicsData.get(selectedMarker))[0].doubleValue(), ((Double[])graphicsData.get(selectedMarker))[1].doubleValue());
            String label = (new StringBuilder("X=")).append(formatter.format(((Double[])graphicsData.get(selectedMarker))[0])).append(", Y=").append(formatter.format(((Double[])graphicsData.get(selectedMarker))[1])).toString();
            Rectangle2D bounds = labelsFont.getStringBounds(label, context);
            canvas.setColor(Color.BLUE);
            canvas.drawString(label, (float)(point.getX() + 5D), (float)(point.getY() - bounds.getHeight()));
        }
    }

    private void paintGrid(Graphics2D canvas)
    {
        canvas.setStroke(gridStroke);
        canvas.setColor(Color.GRAY);
        double pos = viewport[0][0];
        double step = (viewport[1][0] - viewport[0][0]) / 10D;
        for(; pos < viewport[1][0]; pos += step)
            canvas.draw(new Line2D.Double(translateXYtoPoint(pos, viewport[0][1]), translateXYtoPoint(pos, viewport[1][1])));
        canvas.draw(new Line2D.Double(translateXYtoPoint(viewport[1][0], viewport[0][1]), translateXYtoPoint(viewport[1][0], viewport[1][1])));
        pos = viewport[1][1];
        step = (viewport[0][1] - viewport[1][1]) / 10D;
        for(; pos < viewport[0][1]; pos += step)
            canvas.draw(new Line2D.Double(translateXYtoPoint(viewport[0][0], pos), translateXYtoPoint(viewport[1][0], pos)));

        canvas.draw(new Line2D.Double(translateXYtoPoint(viewport[0][0], viewport[0][1]), translateXYtoPoint(viewport[1][0], viewport[0][1])));
    }

    private void paintAxis(Graphics2D canvas)
    {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setFont(axisFont);
        FontRenderContext context = canvas.getFontRenderContext();
        if(viewport[0][0] <= 0.0D && viewport[1][0] >= 0.0D)
        {
            canvas.draw(new Line2D.Double(translateXYtoPoint(0.0D, viewport[0][1]), translateXYtoPoint(0.0D, viewport[1][1])));
            canvas.draw(new Line2D.Double(translateXYtoPoint(-(viewport[1][0] - viewport[0][0]) * 0.0025000000000000001D, viewport[0][1] - (viewport[0][1] - viewport[1][1]) * 0.014999999999999999D), translateXYtoPoint(0.0D, viewport[0][1])));
            canvas.draw(new Line2D.Double(translateXYtoPoint((viewport[1][0] - viewport[0][0]) * 0.0025000000000000001D, viewport[0][1] - (viewport[0][1] - viewport[1][1]) * 0.014999999999999999D), translateXYtoPoint(0.0D, viewport[0][1])));
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = translateXYtoPoint(0.0D, viewport[0][1]);
            canvas.drawString("y", (float)labelPos.x + 10F, (float)(labelPos.y + bounds.getHeight() / 2D));
        }
        if(viewport[1][1] <= 0.0D && viewport[0][1] >= 0.0D)
        {
            canvas.draw(new Line2D.Double(translateXYtoPoint(viewport[0][0], 0.0D), translateXYtoPoint(viewport[1][0], 0.0D)));
            canvas.draw(new Line2D.Double(translateXYtoPoint(viewport[1][0] - (viewport[1][0] - viewport[0][0]) * 0.01D, (viewport[0][1] - viewport[1][1]) * 0.0050000000000000001D), translateXYtoPoint(viewport[1][0], 0.0D)));
            canvas.draw(new Line2D.Double(translateXYtoPoint(viewport[1][0] - (viewport[1][0] - viewport[0][0]) * 0.01D, -(viewport[0][1] - viewport[1][1]) * 0.0050000000000000001D), translateXYtoPoint(viewport[1][0], 0.0D)));
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = translateXYtoPoint(viewport[1][0], 0.0D);
            canvas.drawString("x", (float)(labelPos.x - bounds.getWidth() - 10D), (float)(labelPos.y - bounds.getHeight() / 2D));
        }
    }

    protected Point2D.Double translateXYtoPoint(double x, double y)
    {
        double deltaX = x - viewport[0][0];
        double deltaY = viewport[0][1] - y;
        return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
    }

    protected double[] translatePointToXY(int x, int y)
    {
        return (new double[] {
                viewport[0][0] + (double)x / scaleX, viewport[0][1] - (double)y / scaleY
        });
    }

    protected int findSelectedPoint(int x, int y)
    {
        if(graphicsData == null)
            return -1;
        int pos = 0;
        for(Iterator<Double[]> iterator = graphicsData.iterator(); iterator.hasNext();)
        {
            Double point[] = (Double[])iterator.next();
            Point2D.Double screenPoint = translateXYtoPoint(point[0].doubleValue(), point[1].doubleValue());
            double distance = (screenPoint.getX() - (double)x) * (screenPoint.getX() - (double)x) + (screenPoint.getY() - (double)y) * (screenPoint.getY() - (double)y);
            if(distance < 100D)
                return pos;
            pos++;
        }
        return -1;
    }

    public void reset()
    {
        displayGraphics(originalData);
    }
}