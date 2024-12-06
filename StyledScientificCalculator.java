import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

public class StyledScientificCalculator extends JFrame implements ActionListener {
    private JTextField display;
    private JPanel buttonPanel;
    private boolean isScientific = false;

    public StyledScientificCalculator() {
        // Frame setup
        setTitle("Styled Calculator");
        setSize(350, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        // Set the background to match the blue theme
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setBackground(new Color(0, 85, 170)); // Vibrant blue color
        backgroundPanel.setBounds(0, 0, 350, 600);
        backgroundPanel.setLayout(null); // No layout to allow custom placement
        add(backgroundPanel);

        // Display field
        display = new JTextField();
        display.setFont(new Font("Digital-7 Mono", Font.BOLD, 24));
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setBounds(10, 10, 330, 60);
        display.setEditable(false);
        display.setBackground(Color.WHITE);
        display.setForeground(Color.BLACK);
        display.setBorder(BorderFactory.createLineBorder(new Color(0, 50, 100), 2));
        backgroundPanel.add(display);

        // Button panel
        buttonPanel = new JPanel();
        buttonPanel.setBounds(10, 80, 330, 400);
        buttonPanel.setLayout(new GridLayout(6, 4, 5, 5));
        buttonPanel.setBackground(new Color(0, 85, 170));
        backgroundPanel.add(buttonPanel);

        // Add basic buttons
        addBasicButtons();

        // Add toggle mode button with a popup menu
        JButton switchMode = new JButton("Mode");
        switchMode.setFont(new Font("Arial", Font.BOLD, 18));
        switchMode.setBackground(new Color(135, 206, 250));
        switchMode.setBounds(10, 500, 150, 50);
        switchMode.addActionListener(e -> showModeOptions());
        backgroundPanel.add(switchMode);

        setResizable(false);
        setVisible(true);
    }

    private void addBasicButtons() {
        String[] basicButtons = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "=", "+",
                "(", ")", "C", "Del"
        };

        for (String text : basicButtons) {
            JButton button = createStyledButton(text, Color.CYAN);
            button.addActionListener(this);
            buttonPanel.add(button);
        }
    }

    private void addScientificButtons() {
        String[] scientificButtons = {
                "sin", "cos", "tan", "log",
                "sqrt", "^", "x²"
        };

        for (String text : scientificButtons) {
            JButton button = createStyledButton(text, Color.ORANGE);
            button.addActionListener(this);
            buttonPanel.add(button);
        }
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createBevelBorder(0)); // 3D effect
        return button;
    }

    private void showModeOptions() {
        JPopupMenu modeMenu = new JPopupMenu();

        JMenuItem basicMode = new JMenuItem("Basic Mode");
        basicMode.addActionListener(e -> {
            isScientific = false;
            buttonPanel.removeAll();
            addBasicButtons();
            buttonPanel.revalidate();
            buttonPanel.repaint();
        });

        JMenuItem scientificMode = new JMenuItem("Scientific Mode");
        scientificMode.addActionListener(e -> {
            isScientific = true;
            buttonPanel.removeAll();
            addBasicButtons();
            addScientificButtons();
            buttonPanel.revalidate();
            buttonPanel.repaint();
        });

        modeMenu.add(basicMode);
        modeMenu.add(scientificMode);

        modeMenu.show(this, 10, 500);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        try {
            if (command.equals("=")) {
                String result = evaluate(display.getText());
                display.setText(result);
            } else if (command.equals("C")) {
                display.setText("");
            } else if (command.equals("Del")) {
                String text = display.getText();
                if (!text.isEmpty()) {
                    display.setText(text.substring(0, text.length() - 1));
                }
            } else if (command.equals("x²")) {
                String text = display.getText();
                if (!text.isEmpty()) {
                    double value = Double.parseDouble(text);
                    display.setText(String.valueOf(value * value));
                }
            } else {
                display.setText(display.getText() + command);
            }
        } catch (Exception ex) {
            display.setText("Error");
        }
    }

    private String evaluate(String expression) {
        try {
            ExpressionEvaluator evaluator = new ExpressionEvaluator();
            double result = evaluator.evaluate(expression);
            return String.valueOf(result);
        } catch (Exception ex) {
            return "Error";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StyledScientificCalculator::new);
    }
}

// ExpressionEvaluator: Handles mathematical evaluations
class ExpressionEvaluator {
    public double evaluate(String expression) throws Exception {
        Stack<Double> values = new Stack<>();
        Stack<String> functions = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            // Numbers and decimals
            if (Character.isDigit(ch) || ch == '.') {
                StringBuilder buffer = new StringBuilder();
                while (i < expression.length() &&
                        (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    buffer.append(expression.charAt(i++));
                }
                values.push(Double.parseDouble(buffer.toString()));
                i--;
            }
            // Functions like sqrt, sin, etc.
            else if (Character.isLetter(ch)) {
                StringBuilder func = new StringBuilder();
                while (i < expression.length() && Character.isLetter(expression.charAt(i))) {
                    func.append(expression.charAt(i++));
                }
                functions.push(func.toString());
                i--;
            }
            // Parentheses
            else if (ch == '(') {
                operators.push(ch);
            } else if (ch == ')') {
                while (operators.peek() != '(') {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop();

                // If there's a function before the parentheses
                if (!functions.isEmpty()) {
                    String func = functions.pop();
                    values.push(applyFunction(func, values.pop()));
                }
            }
            // Operators
            else if (isOperator(ch)) {
                while (!operators.isEmpty() && precedence(ch) <= precedence(operators.peek())) {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(ch);
            }
        }

        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '^';
    }

    private int precedence(char operator) {
        if (operator == '+' || operator == '-') return 1;
        if (operator == '*' || operator == '/') return 2;
        if (operator == '^') return 3;
        return -1;
    }

    private double applyOperator(char operator, double b, double a) {
        return switch (operator) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> a / b;
            case '^' -> Math.pow(a, b);
            default -> 0;
        };
    }

    private double applyFunction(String function, double value) {
        double result;
        switch (function) {
            case "sin" -> result = Math.sin(Math.toRadians(value));
            case "cos" -> result = Math.cos(Math.toRadians(value));
            case "tan" -> result = Math.tan(Math.toRadians(value));
            case "log" -> result = Math.log10(value);
            case "sqrt" -> result = Math.sqrt(value);
            default -> throw new IllegalArgumentException("Invalid function: " + function);
        }
        return roundToDecimalPlaces(result, 4); // Round to 4 decimal places
    }

    // Utility method to round to a specific number of decimal places
    private double roundToDecimalPlaces(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("Decimal places must be non-negative.");
        long factor = (long) Math.pow(10, places);
        return Math.round(value * factor) / (double) factor;
    }
}
