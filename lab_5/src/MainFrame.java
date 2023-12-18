import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

public class MainFrame extends JFrame
{
    private static final int WIDTH = 700;
    private static final int HEIGHT = 500;
    private JFileChooser fileChooser;
    private JMenuItem resetGraphicsMenuItem;
    private GraphicsDisplay display;

    public MainFrame()
    {
        super("Обработка событий от мыши");
        fileChooser = null;
        display = new GraphicsDisplay();
        setSize(700, 500);
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH) / 2, (kit.getScreenSize().height - HEIGHT) / 2);
        setExtendedState(6);
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);
        Action openGraphicsAction = new AbstractAction("Открыть файл с графиком") {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent event)
            {
                if(fileChooser == null)
                {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                fileChooser.showOpenDialog(MainFrame.this);
                openGraphics(fileChooser.getSelectedFile());
            }
        };
        fileMenu.add(openGraphicsAction);
        Action resetGraphicsAction = new AbstractAction("Отменить все изменения") {

            public void actionPerformed(ActionEvent event)
            {
                display.reset();
            }
        };
        resetGraphicsMenuItem = fileMenu.add(resetGraphicsAction);
        resetGraphicsMenuItem.setEnabled(false);
        getContentPane().add(display, "Center");
    }

    protected void openGraphics(File selectedFile)
    {
        try
        {
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
            ArrayList<Double[]> graphicsData = new ArrayList<Double[]>(50);
            Double x;
            Double y;
            for(; in.available() > 0; graphicsData.add(new Double[] { x, y}))
            {
                x = Double.valueOf(in.readDouble());
                y = Double.valueOf(in.readDouble());
            }

            if(graphicsData.size() > 0)
            {
                resetGraphicsMenuItem.setEnabled(true);
                display.displayGraphics(graphicsData);
            }
        }
        catch(FileNotFoundException ex)
        {
            JOptionPane.showMessageDialog(this, "Указанный файл не найден", "Ошибка загрузки данных", 2);
            return;
        }
        catch(IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Ошибка чтения координат точек из файла", "Ошибка загрузки данных", 2);
            return;
        }
    }

    public static void main(String args[])
    {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(3);
        frame.setVisible(true);
    }
}
