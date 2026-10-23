package canteen.ui.components;

import canteen.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

public class SearchField extends JPanel {

    private final JTextField textField;
    private final JLabel searchIcon;
    private final JLabel clearIcon;
    private final String placeholder;
    private boolean hasFocus = false;

    public SearchField(String placeholder) {
        this.placeholder = placeholder;
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(300, 40));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        searchIcon.setBorder(new EmptyBorder(0, 14, 0, 8));
        searchIcon.setForeground(ThemeManager.getTextMuted());

        textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setBorder(null);
        textField.setOpaque(false);
        textField.setForeground(ThemeManager.getTextPrimary());
        textField.setCaretColor(ThemeManager.getAccent());
        textField.setSelectedTextColor(Color.WHITE);
        textField.setSelectionColor(ThemeManager.getAccent());

        clearIcon = new JLabel("✕");
        clearIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clearIcon.setBorder(new EmptyBorder(0, 8, 0, 14));
        clearIcon.setForeground(ThemeManager.getTextMuted());
        clearIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearIcon.setVisible(false);
        clearIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                textField.setText("");
                textField.requestFocus();
            }
        });

        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setOpaque(false);
        fieldWrapper.add(searchIcon, BorderLayout.WEST);
        fieldWrapper.add(textField, BorderLayout.CENTER);
        fieldWrapper.add(clearIcon, BorderLayout.EAST);

        add(fieldWrapper, BorderLayout.CENTER);

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateClearIcon(); }
            @Override public void removeUpdate(DocumentEvent e) { updateClearIcon(); }
            @Override public void changedUpdate(DocumentEvent e) { updateClearIcon(); }
        });

        textField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                hasFocus = true;
                updateClearIcon();
                repaint();
            }
            @Override public void focusLost(FocusEvent e) {
                hasFocus = false;
                updateClearIcon();
                repaint();
            }
        });
    }

    private void updateClearIcon() {
        clearIcon.setVisible(!textField.getText().isEmpty() && hasFocus);
    }

    public String getText() { return textField.getText().trim(); }
    public void setText(String text) { textField.setText(text); updateClearIcon(); }
    public JTextField getTextField() { return textField; }

    public void addActionListener(java.awt.event.ActionListener listener) {
        textField.addActionListener(listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = 20;

        Color bg = hasFocus ? ThemeManager.getBgSecondary() : ThemeManager.getBgTertiary();
        Color border = hasFocus ? ThemeManager.getAccent() : ThemeManager.getBorderLight();

        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        g2.setColor(border);
        g2.setStroke(new BasicStroke(hasFocus ? 2f : 1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

        g2.dispose();
        super.paintComponent(g);
    }
}