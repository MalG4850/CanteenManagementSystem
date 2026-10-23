package canteen;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class QrDialog extends JDialog {
    private final Order order;
    private final WhatsAppService whatsAppService;
    private BufferedImage qrImage;
    private boolean whatsAppSent = false;

    public QrDialog(JFrame parent, Order order, WhatsAppService whatsAppService) {
        super(parent, "Order #" + order.getOrderId() + " - QR Code", true);
        this.order = order;
        this.whatsAppService = whatsAppService;

        setSize(500, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        generateQrCode();
    }

    private void initComponents() {
        // QR Code display
        JLabel qrLabel = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (qrImage != null) {
                    int x = (getWidth() - qrImage.getWidth()) / 2;
                    int y = (getHeight() - qrImage.getHeight()) / 2;
                    g.drawImage(qrImage, x, y, null);
                }
            }
        };
        qrLabel.setPreferredSize(new Dimension(300, 300));
        qrLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(qrLabel, BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Order Details"));
        infoPanel.add(new JLabel("Order ID: #" + order.getOrderId()));
        infoPanel.add(new JLabel("Customer: " + order.getCustomerName()));
        infoPanel.add(new JLabel("Mobile: " + order.getCustomerMobile()));
        infoPanel.add(new JLabel("Total: ₹" + String.format("%.2f", order.getTotalAmount())));
        infoPanel.add(new JLabel("Status: " + order.getStatus().getDisplayName()));
        infoPanel.add(new JLabel("QR Token: " + order.getQrToken().substring(0, 16) + "..."));
        add(infoPanel, BorderLayout.NORTH);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton whatsAppBtn = new JButton("📱 Send WhatsApp");
        whatsAppBtn.setPreferredSize(new Dimension(160, 40));
        whatsAppBtn.addActionListener(this::sendWhatsApp);
        buttonPanel.add(whatsAppBtn);

        JButton saveBtn = new JButton("💾 Save QR Image");
        saveBtn.setPreferredSize(new Dimension(160, 40));
        saveBtn.addActionListener(this::saveQrImage);
        buttonPanel.add(saveBtn);

        JButton printBtn = new JButton("🖨 Print Invoice");
        printBtn.setPreferredSize(new Dimension(140, 40));
        printBtn.addActionListener(this::printInvoice);
        buttonPanel.add(printBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void generateQrCode() {
        String qrData = order.getQrToken(); // Just the token, kitchen scans this
        qrImage = QrGenerator.generate(qrData, 300);
        repaint();
    }

    private void sendWhatsApp(ActionEvent e) {
        if (whatsAppSent) {
            JOptionPane.showMessageDialog(this, "WhatsApp already sent for this order", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JButton btn = (JButton) e.getSource();
        btn.setEnabled(false);
        btn.setText("Sending...");

        new Thread(() -> {
            boolean success = whatsAppService.sendInvoice(order, order.getCustomerMobile());
            SwingUtilities.invokeLater(() -> {
                btn.setEnabled(true);
                btn.setText("📱 Send WhatsApp");
                if (success) {
                    whatsAppSent = true;
                    btn.setText("✓ Sent");
                    btn.setEnabled(false);
                    JOptionPane.showMessageDialog(this, "Invoice sent via WhatsApp!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to send WhatsApp. Check Twilio config.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    private void saveQrImage(ActionEvent e) {
        if (qrImage == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save QR Code");
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Image", "png"));
        chooser.setSelectedFile(new File("order_" + order.getOrderId() + "_qr.png"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }
            try {
                ImageIO.write(qrImage, "PNG", file);
                JOptionPane.showMessageDialog(this, "QR code saved to: " + file.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printInvoice(ActionEvent e) {
        // Reuse the existing CanteenBillPrinter logic
        CanteenBillPrinter printer = new CanteenBillPrinter(order.getCustomerName(), order.getCustomerMobile(),
            order.getItems().stream().map(oi -> new CartItem(oi.getItemId(), oi.getItemName(), oi.getQuantity(), oi.getItemPrice())).toList());
        printer.PrintInvoice(order.getOrderId(), order.getBaseAmount(), order.getGstAmount(), order.getTotalAmount());
        JOptionPane.showMessageDialog(this, "Invoice printed to console", "Printed", JOptionPane.INFORMATION_MESSAGE);
    }
}