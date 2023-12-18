//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//



import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class formula extends JFrame {
    private Double result = 0.0;
    private Double sum = 0.0;
    private int formula = 0;
    private JButton button1 = new JButton("Вычислить");
    private JButton button2 = new JButton("Очистить");
    private JButton button3 = new JButton("M+");
    private JButton button4 = new JButton("MC");
    private JRadioButton radio1 = new JRadioButton("Формула 1");
    private JRadioButton radio2 = new JRadioButton("Формула 2");
    private JTextField textFieldX = new JTextField("0", 5);
    private JTextField textFieldY = new JTextField("0", 5);
    private JTextField textFieldZ = new JTextField("0", 5);
    private JTextField textFieldResult = new JTextField("0", 10);
    private JLabel labelX = new JLabel("X:");
    private JLabel labelY = new JLabel("Y:");
    private JLabel labelZ = new JLabel("Z:");
    private JLabel labelResult = new JLabel("Результат:");

    public double calculate1(double x, double y, double z) {
        if(y < 0) {
            throw new IllegalArgumentException("Y must be greater than 0");
        }
        return (Math.sin(y) + y * y + Math.exp(Math.cos(y))) * Math.pow(Math.log(z) + Math.sin(Math.PI) * Math.pow(x, 2.0), 0.25);
    }

    public double calculate2(double x, double y, double z) {
        if(y < 0) {
            throw new IllegalArgumentException("Y must be greater than 0");
        }
        return (Math.tan(Math.pow(x, 2.0)) + Math.pow(y, 0.5)) / (z * Math.log(x + y));
    }

    public formula() {
        super("Вычисление формулы");
        this.setBounds(760, 500, 500, 400);
        this.setDefaultCloseOperation(3);
        Container container = this.getContentPane();
        container.setLayout(new BoxLayout(container, 1));
        ButtonGroup group = new ButtonGroup();
        group.add(this.radio1);
        group.add(this.radio2);
        JPanel panel1 = new JPanel();
        panel1.add(this.radio1);
        this.radio1.setSelected(true);
        panel1.add(this.radio2);
        this.radio1.addActionListener(new ButtonEventListener());
        this.radio2.addActionListener(new ButtonEventListener());
        container.add(panel1);
        JPanel panel2 = new JPanel();
        panel2.add(this.labelX);
        panel2.add(this.textFieldX);
        panel2.add(this.labelY);
        panel2.add(this.textFieldY);
        panel2.add(this.labelZ);
        panel2.add(this.textFieldZ);
        container.add(panel2);
        JPanel panel3 = new JPanel();
        panel3.add(this.labelResult);
        panel3.add(this.textFieldResult);
        container.add(panel3);
        JPanel panel4 = new JPanel();
        panel4.add(this.button1);
        panel4.add(this.button2);
        container.add(panel4);
        JPanel panel5 = new JPanel();
        panel5.add(this.button3);
        panel5.add(this.button4);
        container.add(panel5);
        this.button1.addActionListener(new ButtonEventListener());
        this.button2.addActionListener(new ButtonEventListener());
        this.button3.addActionListener(new ButtonEventListener());
        this.button4.addActionListener(new ButtonEventListener());
    }

    public class ButtonEventListener implements ActionListener {
        public ButtonEventListener() {
        }
        public void actionPerformed(ActionEvent e) {
            try {
                if (e.getSource() == formula.this.radio1) {
                    formula.this.formula = 1;
                } else if (e.getSource() == formula.this.radio2) {
                    formula.this.formula = 0;
                } else if (e.getSource() == formula.this.button1) {
                    Double x = Double.parseDouble(formula.this.textFieldX.getText());
                    Double y = Double.parseDouble(formula.this.textFieldY.getText());
                    Double z = Double.parseDouble(formula.this.textFieldZ.getText());
                    if (formula.this.formula == 1) {
                        formula.this.result = formula.this.calculate1(x, y, z);
                    } else if (formula.this.formula == 0) {
                        formula.this.result = formula.this.calculate2(x, y, z);
                    }

                    formula.this.textFieldResult.setText(formula.this.result.toString());
                } else if (e.getSource() == formula.this.button2) {
                    formula.this.textFieldX.setText("0");
                    formula.this.textFieldY.setText("0");
                    formula.this.textFieldZ.setText("0");
                    formula.this.textFieldResult.setText("0");
                } else if (e.getSource() == formula.this.button3) {
                    formula.this.result = Double.parseDouble(formula.this.textFieldResult.getText());
                    formula.this.sum = formula.this.sum + formula.this.result;
                    formula.this.textFieldResult.setText(formula.this.sum.toString());
                } else if (e.getSource() == formula.this.button4) {
                    formula.this.textFieldResult.setText("0");
                    formula.this.result = Double.parseDouble(formula.this.textFieldResult.getText());
                    formula.this.sum = 0.0;
                    formula.this.textFieldResult.setText(formula.this.sum.toString());
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(formula.this,
                        ex.getMessage(), "Error. ", JOptionPane.ERROR_MESSAGE);
            }

        }
    }
}