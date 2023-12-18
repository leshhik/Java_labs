

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private JFileChooser fileChooser = null;
    private JCheckBoxMenuItem showAxisMenuItem;
    private JCheckBoxMenuItem showMarkersMenuItem;
    private GraphicsDisplay display = new GraphicsDisplay();
    private boolean fileLoaded = false;

    public MainFrame() {

        super("Построение графиков функций на основе заранее подготовленных файлов");
        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH)/2,
                (kit.getScreenSize().height - HEIGHT)/2);
        setExtendedState(MAXIMIZED_BOTH);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);

        Action openGraphicsAction = new AbstractAction("Открыть файл с графиком") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser==null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    openGraphics(fileChooser.getSelectedFile());
            }
        };

        fileMenu.add(openGraphicsAction);

        JMenu graphicsMenu = new JMenu("График");
        menuBar.add(graphicsMenu);

        Action showAxisAction = new AbstractAction("Показывать оси координат") {
            public void actionPerformed(ActionEvent event) {
                display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };

        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        graphicsMenu.add(showAxisMenuItem);
        showAxisMenuItem.setSelected(true);

        Action showMarkersAction = new AbstractAction("Показывать маркеры точек") {
            public void actionPerformed(ActionEvent event) {
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };

        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
        showMarkersMenuItem.setSelected(true);

        graphicsMenu.addMenuListener(new GraphicsMenuListener());
        getContentPane().add(display, BorderLayout.CENTER);
    }

    protected void openGraphics(File selectedFile) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
            //Всего байт в потоке - in.available() байт;
            //Размер числа Double - Double.SIZE бит, или Double.SIZE/8 байт;
            //Так как числа записываются парами, то число пар меньше в 2 раза
            Double[][] graphicsData = new Double[in.available()/(Double.SIZE/8)/2][];
            int i = 0;
            while (in.available()>0) {
                Double x = in.readDouble();
                Double y = in.readDouble();
                graphicsData[i++] = new Double[] {x, y};
            }
            if (graphicsData!=null && graphicsData.length>0) {
                fileLoaded = true;
                display.showGraphics(graphicsData);
            }
            in.close();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Указанный файл не найден", "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтения координат точек из файла", "Ошибка загрузки данных",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
    }

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private class GraphicsMenuListener implements MenuListener {
        public void menuSelected(MenuEvent e) {
            showAxisMenuItem.setEnabled(fileLoaded);
            showMarkersMenuItem.setEnabled(fileLoaded);
        }

        public void menuDeselected(MenuEvent e) {}

        public void menuCanceled(MenuEvent e) {}

    }
    @SuppressWarnings("serial")
    public class GraphicsDisplay extends JPanel {

        private Double[][] graphicsData;

        private boolean showAxis = true;
        private boolean showMarkers = true;

        private double minX;
        private double maxX;
        private double minY;
        private double maxY;
        private double scale;
        private BasicStroke graphicsStroke;
        private BasicStroke axisStroke;
        private BasicStroke markerStroke;
        private Font axisFont;

        public GraphicsDisplay() {
            setBackground(Color.WHITE);
            graphicsStroke = new BasicStroke(4.0f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 10.0f, new float[] {1,1,2,1,1,1,4,1,2,1,1}, 0.0f);
            axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
            markerStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
            axisFont = new Font("Serif", Font.BOLD, 30);
        }

        public void showGraphics(Double[][] graphicsData) {
            this.graphicsData = graphicsData;
            //paintComponent();
            repaint();
        }

        public void setShowAxis(boolean showAxis) {
            this.showAxis = showAxis;
            repaint();
        }

        public void setShowMarkers(boolean showMarkers) {
            this.showMarkers = showMarkers;
            repaint();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (graphicsData == null || graphicsData.length == 0) return;
            minX = graphicsData[0][0];
            maxX = graphicsData[graphicsData.length - 1][0];
            minY = graphicsData[0][1];
            maxY = minY;
            for (int i = 1; i < graphicsData.length; i++) {
                if (graphicsData[i][1] < minY) {
                    minY = graphicsData[i][1];
                }
                if (graphicsData[i][1] > maxY) {
                    maxY = graphicsData[i][1];
                }
            }

            double scaleX = getSize().getWidth() / (maxX - minX);
            double scaleY = getSize().getHeight() / (maxY - minY);
            scale = Math.min(scaleX, scaleY);
            if (scale == scaleX) {
                double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
                maxY += yIncrement;
                minY -= yIncrement;
            }
            if (scale == scaleY) {
                double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
                maxX += xIncrement;
                minX -= xIncrement;
            }
            Graphics2D canvas = (Graphics2D) g;
            Stroke oldStroke = canvas.getStroke();
            Color oldColor = canvas.getColor();
            Paint oldPaint = canvas.getPaint();
            Font oldFont = canvas.getFont();

            if (showAxis) paintAxis(canvas);
            paintGraphics(canvas);
            if (showMarkers) paintMarkers(canvas);
            canvas.setFont(oldFont);
            canvas.setPaint(oldPaint);
            canvas.setColor(oldColor);
            canvas.setStroke(oldStroke);
        }

        protected void paintGraphics(Graphics2D canvas) {
            canvas.setStroke(graphicsStroke);
            canvas.setColor(Color.BLACK);

            GeneralPath graphics = new GeneralPath();
            for (int i = 0; i < graphicsData.length; i++) {
                Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                if (i > 0) {
                    graphics.lineTo(point.getX(), point.getY());
                } else {
                    graphics.moveTo(point.getX(), point.getY());
                }
            }
            canvas.draw(graphics);
        }

        protected void paintMarkers(Graphics2D canvas) {
            canvas.setStroke(markerStroke);

            for (Double[] point : graphicsData) {
                GeneralPath path = new GeneralPath();
                Point2D.Double center = xyToPoint(point[0], point[1]);
                canvas.setColor(Color.RED);
                if (isOdd(point[1]) )
                {
                    canvas.setColor(Color.GREEN);
                }

                path.append(new Line2D.Double(center.getX()-30, center.getY(), center.getX()-10, center.getY()-10), false);
                path.append(new Line2D.Double(center.getX()-10, center.getY()-10, center.getX(), center.getY()-30), false);
                path.append(new Line2D.Double(center.getX(), center.getY()-30, center.getX()+10, center.getY()-10), false);
                path.append(new Line2D.Double(center.getX()+10, center.getY()-10, center.getX()+30, center.getY()), false);
                path.append(new Line2D.Double(center.getX()+30, center.getY(), center.getX()+10, center.getY()+10), false);
                path.append(new Line2D.Double(center.getX()+10, center.getY()+10, center.getX(), center.getY()+30), false);
                path.append(new Line2D.Double(center.getX(), center.getY()+30, center.getX()-10, center.getY()+10), false);
                path.append(new Line2D.Double(center.getX()-10, center.getY()+10, center.getX()-30, center.getY()), false);





                canvas.draw(path);
            }
        }

        //Целая часть значения функции в точке - чѐтная
        boolean isOdd(double value)
        {
            int temp = (int)value;
            if (temp % 2 == 0)
                return true;
            else
                return false;
        }

        protected void paintAxis(Graphics2D canvas) {
            canvas.setStroke(axisStroke);
            canvas.setColor(Color.BLACK);
            canvas.setPaint(Color.BLACK);
            canvas.setFont(axisFont);
            FontRenderContext context = canvas.getFontRenderContext();
            if (minX <= 0.0 && maxX >= 0.0) {
                canvas.draw(new Line2D.Double(xyToPoint(0, maxY),
                        xyToPoint(0, minY)));
                GeneralPath arrow = new GeneralPath();
                Point2D.Double lineEnd = xyToPoint(0, maxY);
                arrow.moveTo(lineEnd.getX(), lineEnd.getY());
                arrow.lineTo(arrow.getCurrentPoint().getX() + 5,
                        arrow.getCurrentPoint().getY() + 20);
                arrow.lineTo(arrow.getCurrentPoint().getX() - 10,
                        arrow.getCurrentPoint().getY());
                arrow.closePath();
                canvas.draw(arrow);
                canvas.fill(arrow);
                Rectangle2D bounds = axisFont.getStringBounds("y", context);
                Point2D.Double labelPos = xyToPoint(0, maxY);
                canvas.drawString("y", (float) labelPos.getX() + 10,
                        (float) (labelPos.getY() - bounds.getY()));
            }
            if (minY <= 0.0 && maxY >= 0.0) {

                canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
                        xyToPoint(maxX, 0)));
                GeneralPath arrow = new GeneralPath();
                Point2D.Double lineEnd = xyToPoint(maxX, 0);
                arrow.moveTo(lineEnd.getX(), lineEnd.getY());
                arrow.lineTo(arrow.getCurrentPoint().getX() - 20,
                        arrow.getCurrentPoint().getY() - 5);
                arrow.lineTo(arrow.getCurrentPoint().getX(),
                        arrow.getCurrentPoint().getY() + 10);
                arrow.closePath();
                canvas.draw(arrow);
                canvas.fill(arrow);
                Rectangle2D bounds = axisFont.getStringBounds("x", context);
                Point2D.Double labelPos = xyToPoint(maxX, 0);
                canvas.drawString("x", (float) (labelPos.getX() -
                        bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
            }
        }

        protected Point2D.Double xyToPoint(double x, double y) {
            double deltaX = x - minX;
            double deltaY = maxY - y;
            return new Point2D.Double(deltaX * scale, deltaY * scale);
        }
    }

}