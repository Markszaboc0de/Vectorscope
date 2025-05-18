package ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import capture.ScreenCaptureData;
import capture.ScreenCaptureUtils;
import java.awt.Rectangle;
import javax.swing.Timer;
import javax.swing.Box;

public class MainWindow extends JFrame {
    private VectorscopePanel vectorscopePanel1;
    private VectorscopePanel vectorscopePanel2;
    private Rectangle lastSelectedArea1 = null;
    private Rectangle lastSelectedArea2 = null;
    private Timer liveViewTimer1;
    private Timer liveViewTimer2;
    private boolean liveViewEnabled1 = false;
    private boolean liveViewEnabled2 = false;
    private boolean referencePointMode = false;
    private Double referenceAngle = null;
    private Double referenceRadius = null;
    private JLabel pixelInfoLabel1 = new JLabel(" ");
    private JLabel pixelInfoLabel2 = new JLabel(" ");

    public MainWindow() {
        setTitle("Screen Capture Tool");
        setSize(500, 610);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        // --- First Vectorscope Module ---
        JButton captureButton1 = new JButton("Capture Screen Area 1");
        JButton liveViewButton1 = new JButton("Live View 1: OFF");
        JToggleButton zoomButton1 = new JToggleButton("1.5x Zoom");
        JToggleButton luminosityButton1 = new JToggleButton("Luminosity View");
        JPanel leftPanel1 = new JPanel();
        leftPanel1.setLayout(new BorderLayout());
        leftPanel1.add(captureButton1, BorderLayout.NORTH);
        leftPanel1.add(liveViewButton1, BorderLayout.SOUTH);
        leftPanel1.add(zoomButton1, BorderLayout.CENTER);
        leftPanel1.add(luminosityButton1, BorderLayout.SOUTH);
        vectorscopePanel1 = new VectorscopePanel();

        // --- Second Vectorscope Module ---
        JButton captureButton2 = new JButton("Capture Screen Area 2");
        JButton liveViewButton2 = new JButton("Live View 2: OFF");
        JToggleButton zoomButton2 = new JToggleButton("1.5x Zoom");
        JToggleButton luminosityButton2 = new JToggleButton("Luminosity View");
        JPanel leftPanel2 = new JPanel();
        leftPanel2.setLayout(new BorderLayout());
        leftPanel2.add(captureButton2, BorderLayout.NORTH);
        leftPanel2.add(liveViewButton2, BorderLayout.SOUTH);
        leftPanel2.add(zoomButton2, BorderLayout.CENTER);
        leftPanel2.add(luminosityButton2, BorderLayout.SOUTH);
        vectorscopePanel2 = new VectorscopePanel();

        // --- Reference Point Button ---
        JButton referencePointButton = new JButton("Set Reference Point");
        JButton autoReferenceLineButton = new JButton("Auto Reference Line");
        JButton clearReferenceLineButton = new JButton("Clear Reference Line");
        JButton copyReferenceLineButton = new JButton("Copy Reference Line");

        // Set zoom buttons to same size as referencePointButton
        Dimension buttonSize = referencePointButton.getPreferredSize();
        zoomButton1.setPreferredSize(buttonSize);
        zoomButton1.setMaximumSize(buttonSize);
        zoomButton1.setMinimumSize(buttonSize);
        luminosityButton1.setPreferredSize(buttonSize);
        luminosityButton1.setMaximumSize(buttonSize);
        luminosityButton1.setMinimumSize(buttonSize);
        zoomButton2.setPreferredSize(buttonSize);
        zoomButton2.setMaximumSize(buttonSize);
        zoomButton2.setMinimumSize(buttonSize);
        luminosityButton2.setPreferredSize(buttonSize);
        luminosityButton2.setMaximumSize(buttonSize);
        luminosityButton2.setMinimumSize(buttonSize);

        // Stack left panel buttons vertically with minimal space
        leftPanel1.removeAll();
        Box box1 = Box.createVerticalBox();
        box1.add(captureButton1);
        box1.add(Box.createVerticalStrut(4));
        box1.add(liveViewButton1);
        box1.add(Box.createVerticalStrut(4));
        box1.add(zoomButton1);
        box1.add(Box.createVerticalStrut(4));
        box1.add(luminosityButton1);
        box1.add(Box.createVerticalStrut(4));
        box1.add(pixelInfoLabel1);
        leftPanel1.add(box1);

        leftPanel2.removeAll();
        Box box2 = Box.createVerticalBox();
        box2.add(captureButton2);
        box2.add(Box.createVerticalStrut(4));
        box2.add(liveViewButton2);
        box2.add(Box.createVerticalStrut(4));
        box2.add(zoomButton2);
        box2.add(Box.createVerticalStrut(4));
        box2.add(luminosityButton2);
        box2.add(Box.createVerticalStrut(4));
        box2.add(pixelInfoLabel2);
        leftPanel2.add(box2);

        // --- Layout ---
        JPanel topModule = new JPanel(new GridBagLayout());
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0; gbc1.gridy = 0; gbc1.fill = GridBagConstraints.VERTICAL; gbc1.weighty = 1.0;
        topModule.add(leftPanel1, gbc1);
        gbc1.gridx = 1; gbc1.gridy = 0; gbc1.fill = GridBagConstraints.BOTH; gbc1.weightx = 1.0; gbc1.weighty = 1.0;
        topModule.add(vectorscopePanel1, gbc1);
        JPanel bottomModule = new JPanel(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0; gbc2.gridy = 0; gbc2.fill = GridBagConstraints.VERTICAL; gbc2.weighty = 1.0;
        bottomModule.add(leftPanel2, gbc2);
        gbc2.gridx = 1; gbc2.gridy = 0; gbc2.fill = GridBagConstraints.BOTH; gbc2.weightx = 1.0; gbc2.weighty = 1.0;
        bottomModule.add(vectorscopePanel2, gbc2);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(topModule);
        centerPanel.add(bottomModule);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        // Button grid
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonGrid.add(referencePointButton);
        buttonGrid.add(autoReferenceLineButton);
        buttonGrid.add(clearReferenceLineButton);
        buttonGrid.add(copyReferenceLineButton);
        mainPanel.add(buttonGrid, BorderLayout.SOUTH);
        setContentPane(mainPanel);

        // --- Listeners for Module 1 ---
        captureButton1.addActionListener(e -> {
            ScreenCaptureOverlay overlay = new ScreenCaptureOverlay(MainWindow.this, (data, rect) -> {
                vectorscopePanel1.setScreenCaptureData(data);
                lastSelectedArea1 = rect;
            });
            overlay.setVisible(true);
        });
        liveViewButton1.addActionListener(e -> {
            liveViewEnabled1 = !liveViewEnabled1;
            liveViewButton1.setText(liveViewEnabled1 ? "Live View 1: ON" : "Live View 1: OFF");
            if (liveViewEnabled1) {
                if (lastSelectedArea1 != null) {
                    startLiveView1();
                } else {
                    liveViewEnabled1 = false;
                    liveViewButton1.setText("Live View 1: OFF");
                    JOptionPane.showMessageDialog(MainWindow.this, "Please select an area first.");
                }
            } else {
                stopLiveView1();
            }
        });
        zoomButton1.addActionListener(e -> {
            vectorscopePanel1.setZoom(zoomButton1.isSelected() ? 1.5 : 1.0);
        });
        luminosityButton1.addActionListener(e -> {
            vectorscopePanel1.setLuminosityView(luminosityButton1.isSelected());
        });

        // --- Listeners for Module 2 ---
        captureButton2.addActionListener(e -> {
            ScreenCaptureOverlay overlay = new ScreenCaptureOverlay(MainWindow.this, (data, rect) -> {
                vectorscopePanel2.setScreenCaptureData(data);
                lastSelectedArea2 = rect;
            });
            overlay.setVisible(true);
        });
        liveViewButton2.addActionListener(e -> {
            liveViewEnabled2 = !liveViewEnabled2;
            liveViewButton2.setText(liveViewEnabled2 ? "Live View 2: ON" : "Live View 2: OFF");
            if (liveViewEnabled2) {
                if (lastSelectedArea2 != null) {
                    startLiveView2();
                } else {
                    liveViewEnabled2 = false;
                    liveViewButton2.setText("Live View 2: OFF");
                    JOptionPane.showMessageDialog(MainWindow.this, "Please select an area first.");
                }
            } else {
                stopLiveView2();
            }
        });
        zoomButton2.addActionListener(e -> {
            vectorscopePanel2.setZoom(zoomButton2.isSelected() ? 1.5 : 1.0);
        });
        luminosityButton2.addActionListener(e -> {
            vectorscopePanel2.setLuminosityView(luminosityButton2.isSelected());
        });

        // --- Reference Point Button Listener ---
        referencePointButton.addActionListener(e -> {
            referencePointMode = !referencePointMode;
            referencePointButton.setText(referencePointMode ? "Click on a scope to set point" : "Set Reference Point");
        });
        autoReferenceLineButton.addActionListener(e -> {
            vectorscopePanel1.autoSetReferenceLine();
            vectorscopePanel2.autoSetReferenceLine();
        });
        clearReferenceLineButton.addActionListener(e -> {
            vectorscopePanel1.clearReferenceLine();
            vectorscopePanel2.clearReferenceLine();
        });
        copyReferenceLineButton.addActionListener(e -> {
            // Only copy if exactly one scope has a reference line set
            boolean v1Set = vectorscopePanel1.hasReferenceLine();
            boolean v2Set = vectorscopePanel2.hasReferenceLine();
            if (v1Set ^ v2Set) {
                if (v1Set) {
                    double angle = vectorscopePanel1.getReferenceAngle();
                    double radius = vectorscopePanel1.getReferenceRadius();
                    vectorscopePanel2.setReferenceLine(angle, radius);
                } else {
                    double angle = vectorscopePanel2.getReferenceAngle();
                    double radius = vectorscopePanel2.getReferenceRadius();
                    vectorscopePanel1.setReferenceLine(angle, radius);
                }
            } else {
                JOptionPane.showMessageDialog(MainWindow.this, "Exactly one scope must have a reference line set.");
            }
        });

        // --- Mouse Listeners for Reference Point ---
        vectorscopePanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (referencePointMode) {
                    setReferencePointFromClick(evt, vectorscopePanel1);
                    referencePointMode = false;
                    referencePointButton.setText("Set Reference Point");
                }
                // Always update pixel info label on click
                String info = vectorscopePanel1.getClickedPixelInfo();
                pixelInfoLabel1.setText(info != null ? info : " ");
            }
        });
        vectorscopePanel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (referencePointMode) {
                    setReferencePointFromClick(evt, vectorscopePanel2);
                    referencePointMode = false;
                    referencePointButton.setText("Set Reference Point");
                }
                // Always update pixel info label on click
                String info = vectorscopePanel2.getClickedPixelInfo();
                pixelInfoLabel2.setText(info != null ? info : " ");
            }
        });
    }

    private void setReferencePointFromClick(java.awt.event.MouseEvent evt, VectorscopePanel panel) {
        int cx = panel.getWidth() / 2;
        int cy = panel.getHeight() / 2;
        int plotRadius = Math.min(panel.getWidth(), panel.getHeight()) / 2 - 20;
        double dx = evt.getX() - cx;
        double dy = evt.getY() - cy;
        double angle = Math.atan2(dy, dx);
        double radius = Math.sqrt(dx * dx + dy * dy) / plotRadius;
        referenceAngle = angle;
        referenceRadius = radius;
        vectorscopePanel1.setReferencePoint(referenceAngle, referenceRadius);
        vectorscopePanel2.setReferencePoint(referenceAngle, referenceRadius);
    }

    private void startLiveView1() {
        if (liveViewTimer1 != null && liveViewTimer1.isRunning()) return;
        liveViewTimer1 = new Timer(100, e -> {
            if (lastSelectedArea1 != null) {
                ScreenCaptureData data = ScreenCaptureUtils.capture(lastSelectedArea1);
                if (data != null) {
                    vectorscopePanel1.setScreenCaptureData(data);
                }
            }
        });
        liveViewTimer1.start();
    }

    private void stopLiveView1() {
        if (liveViewTimer1 != null) {
            liveViewTimer1.stop();
        }
    }

    private void startLiveView2() {
        if (liveViewTimer2 != null && liveViewTimer2.isRunning()) return;
        liveViewTimer2 = new Timer(100, e -> {
            if (lastSelectedArea2 != null) {
                ScreenCaptureData data = ScreenCaptureUtils.capture(lastSelectedArea2);
                if (data != null) {
                    vectorscopePanel2.setScreenCaptureData(data);
                }
            }
        });
        liveViewTimer2.start();
    }

    private void stopLiveView2() {
        if (liveViewTimer2 != null) {
            liveViewTimer2.stop();
        }
    }
} 