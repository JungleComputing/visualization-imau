package imau.visualization;

import imau.visualization.data.GlobeState;
import imau.visualization.data.NetCDFTimedPlayer;
import imau.visualization.netcdf.NetCDFUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

import openglCommon.CommonPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.ColormapInterpreter;
import util.CustomJSlider;
import util.GoggleSwing;
import util.ImauInputHandler;
import util.RangeSlider;
import util.RangeSliderUI;

public class ImauPanel extends CommonPanel {
    public static enum TweakState {
        NONE, DATA, VISUAL, MOVIE
    }

    private final ImauSettings        settings           = ImauSettings
                                                                 .getInstance();
    private final static Logger       logger             = LoggerFactory
                                                                 .getLogger(ImauPanel.class);

    private static final long         serialVersionUID   = 1L;

    protected CustomJSlider           timeBar;

    protected JFormattedTextField     frameCounter, stepSizeField;
    private TweakState                currentConfigState = TweakState.NONE;

    private final JPanel              configPanel;

    private final JPanel              dataConfig, visualConfig, movieConfig;

    private final ImauWindow          imauWindow;
    private static NetCDFTimedPlayer timer;

    private File                      file1;

    public ImauPanel(ImauWindow imauWindow, String path, String cmdlnfileName,
            String cmdlnfileName2) {
        super(imauWindow, ImauInputHandler.getInstance());
        this.imauWindow = imauWindow;

        timeBar = new CustomJSlider(new BasicSliderUI(timeBar));
        timeBar.setValue(0);
        timeBar.setMajorTickSpacing(5);
        timeBar.setMinorTickSpacing(1);
        timeBar.setMaximum(0);
        timeBar.setMinimum(0);
        timeBar.setPaintTicks(true);
        timeBar.setSnapToTicks(true);

        timer = new NetCDFTimedPlayer(timeBar, frameCounter);

        // Make the menu bar
        final JMenuBar menuBar = new JMenuBar();
        final JMenu file = new JMenu("File");
        final JMenuItem open = new JMenuItem("Open");
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final File file = openFile();
                file1 = file;
                handleFile(file);
            }
        });
        file.add(open);
        final JMenuItem open2 = new JMenuItem("Open Second");
        open2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final File file = openFile();
                handleFile(file1, file);
            }
        });
        file.add(open2);
        menuBar.add(file);
        final JMenu options = new JMenu("Options");

        final JMenuItem makeMovie = new JMenuItem("Make movie.");
        makeMovie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setTweakState(TweakState.MOVIE);
            }
        });
        options.add(makeMovie);

        final JMenuItem showDataTweakPanel = new JMenuItem(
                "Show data configuration panel.");
        showDataTweakPanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setTweakState(TweakState.DATA);
            }
        });
        options.add(showDataTweakPanel);

        final JMenuItem showVisualTweakPanel = new JMenuItem(
                "Show visual configuration panel.");
        showVisualTweakPanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setTweakState(TweakState.VISUAL);
            }
        });
        options.add(showVisualTweakPanel);
        menuBar.add(options);

        ImageIcon nlescIcon = GoggleSwing.createResizedImageIcon(
                "images/ESCIENCE_logo.jpg", "eScienceCenter Logo", 311, 28);
        JLabel nlesclogo = new JLabel(nlescIcon);
        nlesclogo.setMinimumSize(new Dimension(300, 20));
        nlesclogo.setMaximumSize(new Dimension(311, 28));

        ImageIcon saraIcon = GoggleSwing.createResizedImageIcon(
                "images/logo_sara.png", "SARA Logo", 41, 28);
        JLabel saralogo = new JLabel(saraIcon);
        saralogo.setMinimumSize(new Dimension(40, 20));
        saralogo.setMaximumSize(new Dimension(41, 28));
        menuBar.add(Box.createHorizontalStrut(3));

        ImageIcon imauIcon = GoggleSwing.createResizedImageIcon(
                "images/logo_imau.png", "IMAU Logo", 52, 28);
        JLabel imaulogo = new JLabel(imauIcon);
        imaulogo.setMinimumSize(new Dimension(50, 20));
        imaulogo.setMaximumSize(new Dimension(52, 28));

        // ImageIcon qrIcon = GoggleSwing.createResizedImageIcon(
        // "images/qrcode_nlesc.png", "QR", 28, 28);
        // JLabel qr = new JLabel(qrIcon);
        // qr.setMinimumSize(new Dimension(20, 20));
        // qr.setMaximumSize(new Dimension(28, 28));

        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(imaulogo);
        menuBar.add(Box.createHorizontalStrut(30));
        menuBar.add(saralogo);
        menuBar.add(Box.createHorizontalStrut(30));
        menuBar.add(nlesclogo);
        menuBar.add(Box.createHorizontalStrut(10));
        // menuBar.add(qr);
        // menuBar.add(Box.createHorizontalStrut(10));

        add(menuBar, BorderLayout.NORTH);

        // Make the "media player" panel
        final JPanel bottomPanel = createBottomPanel();

        // Add the tweaks panels
        configPanel = new JPanel();
        add(configPanel, BorderLayout.WEST);
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setPreferredSize(new Dimension(200, 0));
        configPanel.setVisible(false);

        dataConfig = new JPanel();
        dataConfig.setLayout(new BoxLayout(dataConfig, BoxLayout.Y_AXIS));
        dataConfig.setMinimumSize(configPanel.getPreferredSize());
        createDataTweakPanel();

        visualConfig = new JPanel();
        visualConfig.setLayout(new BoxLayout(visualConfig, BoxLayout.Y_AXIS));
        visualConfig.setMinimumSize(visualConfig.getPreferredSize());
        createVisualTweakPanel();

        movieConfig = new JPanel();
        movieConfig.setLayout(new BoxLayout(movieConfig, BoxLayout.Y_AXIS));
        movieConfig.setMinimumSize(configPanel.getPreferredSize());
        createMovieTweakPanel();

        add(bottomPanel, BorderLayout.SOUTH);

        // Read command line file information
        if (cmdlnfileName != null) {
            if (cmdlnfileName2 != null) {
                final File cmdlnfile1 = new File(cmdlnfileName);
                final File cmdlnfile2 = new File(cmdlnfileName2);
                handleFile(cmdlnfile1, cmdlnfile2);
            } else {
                final File cmdlnfile = new File(cmdlnfileName);
                handleFile(cmdlnfile);
            }
        }

        setTweakState(TweakState.DATA);
    }

    void close() {
        imauWindow.dispose(glCanvas);
    }

    public Point getCanvasLocation() {
        Point topLeft = glCanvas.getLocation();
        return topLeft;
    }

    private JPanel createBottomPanel() {
        final JPanel bottomPanel = new JPanel();
        bottomPanel.setFocusCycleRoot(true);
        bottomPanel.setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container aContainer,
                    Component aComponent) {
                return null;
            }

            @Override
            public Component getComponentBefore(Container aContainer,
                    Component aComponent) {
                return null;
            }

            @Override
            public Component getDefaultComponent(Container aContainer) {
                return null;
            }

            @Override
            public Component getFirstComponent(Container aContainer) {
                return null;
            }

            // No focus traversal here, as it makes stuff go bad (some things
            // react on focus).
            @Override
            public Component getLastComponent(Container aContainer) {
                return null;
            }
        });

        final JButton oneForwardButton = GoggleSwing.createImageButton(
                "images/media-playback-oneforward.png", "Next", null);
        final JButton oneBackButton = GoggleSwing.createImageButton(
                "images/media-playback-onebackward.png", "Previous", null);
        final JButton rewindButton = GoggleSwing.createImageButton(
                "images/media-playback-rewind.png", "Rewind", null);
        final JButton screenshotButton = GoggleSwing.createImageButton(
                "images/camera.png", "Screenshot", null);
        final JButton playButton = GoggleSwing.createImageButton(
                "images/media-playback-start.png", "Start", null);
        final ImageIcon playIcon = GoggleSwing.createImageIcon(
                "images/media-playback-start.png", "Start");
        final ImageIcon stopIcon = GoggleSwing.createImageIcon(
                "images/media-playback-stop.png", "Start");

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        stepSizeField = new JFormattedTextField();
        stepSizeField.setColumns(4);
        stepSizeField.setMaximumSize(new Dimension(40, 20));
        stepSizeField.setValue(settings.getTimestep());
        stepSizeField.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                final JFormattedTextField source = (JFormattedTextField) e
                        .getSource();
                if (source.hasFocus()) {
                    if (source == stepSizeField) {
                        settings.setTimestep((Integer) ((Number) source
                                .getValue()));
                    }
                }
            }
        });
        bottomPanel.add(stepSizeField);

        screenshotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // timer.stop();
                // final ImauInputHandler inputHandler = ImauInputHandler
                // .getInstance();
                // final String fileName = "screenshot: " + "{"
                // + inputHandler.getRotation().get(0) + ","
                // + inputHandler.getRotation().get(1) + " - "
                // + Float.toString(inputHandler.getViewDist()) + "} ";
                imauWindow.makeSnapshot();
            }
        });
        bottomPanel.add(screenshotButton);

        rewindButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.rewind();
                playButton.setIcon(playIcon);
            }
        });
        bottomPanel.add(rewindButton);

        oneBackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.oneBack();
                playButton.setIcon(playIcon);
            }
        });
        bottomPanel.add(oneBackButton);

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer.isPlaying()) {
                    timer.stop();
                    playButton.setIcon(playIcon);
                } else {
                    timer.start();
                    playButton.setIcon(stopIcon);
                }
            }
        });
        bottomPanel.add(playButton);

        oneForwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.oneForward();
                playButton.setIcon(playIcon);
            }
        });
        bottomPanel.add(oneForwardButton);

        timeBar.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    timer.setFrame(timeBar.getValue() - timeBar.getMinimum(),
                            false);
                    playButton.setIcon(playIcon);
                }
            }
        });
        bottomPanel.add(timeBar);

        frameCounter = new JFormattedTextField();
        frameCounter.setValue(new Integer(1));
        frameCounter.setColumns(4);
        frameCounter.setMaximumSize(new Dimension(40, 20));
        frameCounter.setValue(0);
        frameCounter.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                final JFormattedTextField source = (JFormattedTextField) e
                        .getSource();
                if (source.hasFocus()) {
                    if (source == frameCounter) {
                        if (timer.isInitialized()) {
                            timer.setFrame(
                                    ((Number) frameCounter.getValue())
                                            .intValue() - timeBar.getMinimum(),
                                    false);
                        }
                        playButton.setIcon(playIcon);
                    }
                }
            }
        });

        bottomPanel.add(frameCounter);

        return bottomPanel;
    }

    private void createMovieTweakPanel() {
        final ItemListener listener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                setTweakState(TweakState.NONE);
            }
        };
        movieConfig.add(GoggleSwing.titleBox("Movie Creator", listener));

        final ItemListener checkBoxListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                settings.setMovieRotate(e.getStateChange());
                timer.redraw();
            }
        };
        movieConfig.add(GoggleSwing.checkboxBox(
                "",
                new GoggleSwing.CheckBoxItem("Rotation", settings
                        .getMovieRotate(), checkBoxListener)));

        final JLabel rotationSetting = new JLabel(""
                + settings.getMovieRotationSpeedDef());
        final ChangeListener movieRotationSpeedListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    settings.setMovieRotationSpeed(source.getValue() * .25f);
                    rotationSetting.setText(""
                            + settings.getMovieRotationSpeedDef());
                }
            }
        };
        movieConfig.add(GoggleSwing.sliderBox("Rotation Speed",
                movieRotationSpeedListener,
                (int) (settings.getMovieRotationSpeedMin() * 4f),
                (int) (settings.getMovieRotationSpeedMax() * 4f), 1,
                (int) (settings.getMovieRotationSpeedDef() * 4f),
                rotationSetting));

        movieConfig.add(GoggleSwing.buttonBox("",
                new String[] { "Start Recording" },
                new ActionListener[] { new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        timer.movieMode();
                    }
                } }));
    }

    private void createDataTweakPanel() {
        final ItemListener listener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                setTweakState(TweakState.NONE);
            }
        };
        dataConfig.add(GoggleSwing.titleBox("Configuration", listener));

        final JLabel depthSetting = new JLabel("" + settings.getDepthDef());
        final ChangeListener depthListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    settings.setDepth(source.getValue());
                    depthSetting.setText("" + settings.getDepthDef());
                }
            }
        };
        dataConfig.add(GoggleSwing.sliderBox("Depth setting", depthListener,
                settings.getDepthMin(), settings.getDepthMax(), 1,
                settings.getDepthDef(), depthSetting));

        final ArrayList<Component> vcomponents = new ArrayList<Component>();
        JLabel windowlabel = new JLabel("Window Selection");
        windowlabel.setMaximumSize(new Dimension(200, 25));
        windowlabel.setAlignmentX(CENTER_ALIGNMENT);

        vcomponents.add(windowlabel);
        vcomponents.add(Box.createHorizontalGlue());

        final JComboBox comboBox = new JComboBox(new String[] { "All",
                "Left Top", "Right Top", "Left Bottom", "Right Bottom" });
        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = comboBox.getSelectedIndex();
                settings.setWindowSelection(selection);
            }
        });
        comboBox.setMaximumSize(new Dimension(200, 25));
        vcomponents.add(comboBox);
        vcomponents.add(GoggleSwing.verticalStrut(5));

        dataConfig.add(GoggleSwing.vBoxedComponents(vcomponents, true));

        String[] dataModes = { GlobeState.verbalizeDataMode(0),
                GlobeState.verbalizeDataMode(1),
                GlobeState.verbalizeDataMode(2) };
        String[] variables = { GlobeState.verbalizeVariable(0),
                GlobeState.verbalizeVariable(1),
                GlobeState.verbalizeVariable(2),
                GlobeState.verbalizeVariable(3),
                GlobeState.verbalizeVariable(4),
                GlobeState.verbalizeVariable(5),
                GlobeState.verbalizeVariable(6),
                GlobeState.verbalizeVariable(7),
                GlobeState.verbalizeVariable(8),
                GlobeState.verbalizeVariable(9),
                GlobeState.verbalizeVariable(10),
                GlobeState.verbalizeVariable(11),
                GlobeState.verbalizeVariable(12) };
        final String[] colorMaps = ColormapInterpreter.getColormapNames();

        final JComboBox dataModeComboBoxLT = new JComboBox(dataModes);
        final JComboBox dataModeComboBoxRT = new JComboBox(dataModes);
        final JComboBox dataModeComboBoxLB = new JComboBox(dataModes);
        final JComboBox dataModeComboBoxRB = new JComboBox(dataModes);

        dataModeComboBoxLT.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = dataModeComboBoxLT.getSelectedIndex();
                if (selection == -1) {
                    selection = 0;
                }
                settings.setLTDataMode(GlobeState.getDataModeByIndex(selection));
            }
        });
        dataModeComboBoxRT.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = dataModeComboBoxRT.getSelectedIndex();
                if (selection == -1) {
                    selection = 0;
                }
                settings.setRTDataMode(GlobeState.getDataModeByIndex(selection));
            }
        });
        dataModeComboBoxLB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = dataModeComboBoxLB.getSelectedIndex();
                if (selection == -1) {
                    selection = 0;
                }
                settings.setLBDataMode(GlobeState.getDataModeByIndex(selection));
            }
        });
        dataModeComboBoxRB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = dataModeComboBoxRB.getSelectedIndex();
                if (selection == -1) {
                    selection = 0;
                }
                settings.setRBDataMode(GlobeState.getDataModeByIndex(selection));
            }
        });

        final JComboBox comboBoxLT = new JComboBox(variables);
        final JComboBox comboBoxRT = new JComboBox(variables);
        final JComboBox comboBoxLB = new JComboBox(variables);
        final JComboBox comboBoxRB = new JComboBox(variables);

        comboBoxLT.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = comboBoxLT.getSelectedIndex();
                if (selection == -1) {
                    selection = 0;
                }
                settings.setLTVariable(GlobeState.getVariableByIndex(selection));
            }
        });
        comboBoxRT.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = comboBoxRT.getSelectedIndex();
                if (selection == -1) {
                    selection = 0;
                }
                settings.setRTVariable(GlobeState.getVariableByIndex(selection));
            }
        });
        comboBoxLB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = comboBoxLB.getSelectedIndex();
                if (selection == -1) {
                    selection = 0;
                }
                settings.setLBVariable(GlobeState.getVariableByIndex(selection));
            }
        });
        comboBoxRB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = comboBoxRB.getSelectedIndex();
                if (selection == -1) {
                    selection = 0;
                }
                settings.setRBVariable(GlobeState.getVariableByIndex(selection));
            }
        });

        final JComboBox comboBoxLTColorMaps = ColormapInterpreter
                .getLegendJComboBox(new Dimension(200, 25));
        final JComboBox comboBoxRTColorMaps = ColormapInterpreter
                .getLegendJComboBox(new Dimension(200, 25));
        final JComboBox comboBoxLBColorMaps = ColormapInterpreter
                .getLegendJComboBox(new Dimension(200, 25));
        final JComboBox comboBoxRBColorMaps = ColormapInterpreter
                .getLegendJComboBox(new Dimension(200, 25));

        final ArrayList<Component> vcomponentsLT = new ArrayList<Component>();
        final ArrayList<Component> vcomponentsRT = new ArrayList<Component>();
        final ArrayList<Component> vcomponentsLB = new ArrayList<Component>();
        final ArrayList<Component> vcomponentsRB = new ArrayList<Component>();

        JLabel ltLabel = new JLabel("Left Top Selection");
        JLabel rtLabel = new JLabel("Right Top Selection");
        JLabel lbLabel = new JLabel("Left Bottom Selection");
        JLabel rbLabel = new JLabel("Right Bottom Selection");

        ltLabel.setMaximumSize(new Dimension(200, 25));
        rtLabel.setMaximumSize(new Dimension(200, 25));
        lbLabel.setMaximumSize(new Dimension(200, 25));
        rbLabel.setMaximumSize(new Dimension(200, 25));

        ltLabel.setAlignmentX(CENTER_ALIGNMENT);
        rtLabel.setAlignmentX(CENTER_ALIGNMENT);
        lbLabel.setAlignmentX(CENTER_ALIGNMENT);
        rbLabel.setAlignmentX(CENTER_ALIGNMENT);

        vcomponentsLT.add(ltLabel);
        vcomponentsRT.add(rtLabel);
        vcomponentsLB.add(lbLabel);
        vcomponentsRB.add(rbLabel);

        GlobeState LTC = settings.getLTState();
        GlobeState RTC = settings.getRTState();
        GlobeState LBC = settings.getLBState();
        GlobeState RBC = settings.getRBState();

        dataModeComboBoxLT.setSelectedIndex(LTC.getDataModeIndex());
        dataModeComboBoxRT.setSelectedIndex(RTC.getDataModeIndex());
        dataModeComboBoxLB.setSelectedIndex(LBC.getDataModeIndex());
        dataModeComboBoxRB.setSelectedIndex(RBC.getDataModeIndex());

        comboBoxLT.setSelectedIndex(LTC.getVariableIndex());
        comboBoxRT.setSelectedIndex(RTC.getVariableIndex());
        comboBoxLB.setSelectedIndex(LBC.getVariableIndex());
        comboBoxRB.setSelectedIndex(RBC.getVariableIndex());

        comboBoxLTColorMaps.setSelectedItem(ColormapInterpreter
                .getIndexOfColormap(LTC.getColorMap()));
        comboBoxRTColorMaps.setSelectedItem(ColormapInterpreter
                .getIndexOfColormap(RTC.getColorMap()));
        comboBoxLBColorMaps.setSelectedItem(ColormapInterpreter
                .getIndexOfColormap(LBC.getColorMap()));
        comboBoxRBColorMaps.setSelectedItem(ColormapInterpreter
                .getIndexOfColormap(RBC.getColorMap()));

        dataModeComboBoxLT.setMinimumSize(new Dimension(100, 25));
        dataModeComboBoxRT.setMinimumSize(new Dimension(100, 25));
        dataModeComboBoxLB.setMinimumSize(new Dimension(100, 25));
        dataModeComboBoxRB.setMinimumSize(new Dimension(100, 25));

        dataModeComboBoxLT.setMaximumSize(new Dimension(200, 25));
        dataModeComboBoxRT.setMaximumSize(new Dimension(200, 25));
        dataModeComboBoxLB.setMaximumSize(new Dimension(200, 25));
        dataModeComboBoxRB.setMaximumSize(new Dimension(200, 25));

        comboBoxLT.setMinimumSize(new Dimension(100, 25));
        comboBoxRT.setMinimumSize(new Dimension(100, 25));
        comboBoxLB.setMinimumSize(new Dimension(100, 25));
        comboBoxRB.setMinimumSize(new Dimension(100, 25));

        comboBoxLT.setMaximumSize(new Dimension(200, 25));
        comboBoxRT.setMaximumSize(new Dimension(200, 25));
        comboBoxLB.setMaximumSize(new Dimension(200, 25));
        comboBoxRB.setMaximumSize(new Dimension(200, 25));

        comboBoxLTColorMaps.setMinimumSize(new Dimension(100, 25));
        comboBoxRTColorMaps.setMinimumSize(new Dimension(100, 25));
        comboBoxLBColorMaps.setMinimumSize(new Dimension(100, 25));
        comboBoxRBColorMaps.setMinimumSize(new Dimension(100, 25));

        comboBoxLTColorMaps.setMaximumSize(new Dimension(200, 25));
        comboBoxRTColorMaps.setMaximumSize(new Dimension(200, 25));
        comboBoxLBColorMaps.setMaximumSize(new Dimension(200, 25));
        comboBoxRBColorMaps.setMaximumSize(new Dimension(200, 25));

        vcomponentsLT.add(dataModeComboBoxLT);
        vcomponentsRT.add(dataModeComboBoxRT);
        vcomponentsLB.add(dataModeComboBoxLB);
        vcomponentsRB.add(dataModeComboBoxRB);

        vcomponentsLT.add(comboBoxLT);
        vcomponentsRT.add(comboBoxRT);
        vcomponentsLB.add(comboBoxLB);
        vcomponentsRB.add(comboBoxRB);

        vcomponentsLT.add(comboBoxLTColorMaps);
        vcomponentsRT.add(comboBoxRTColorMaps);
        vcomponentsLB.add(comboBoxLBColorMaps);
        vcomponentsRB.add(comboBoxRBColorMaps);

        RangeSlider legendSliderLT = new RangeSlider();
        RangeSlider legendSliderRT = new RangeSlider();
        RangeSlider legendSliderLB = new RangeSlider();
        RangeSlider legendSliderRB = new RangeSlider();

        ((RangeSliderUI) legendSliderLT.getUI()).setRangeColorMap(LTC
                .getColorMap());
        ((RangeSliderUI) legendSliderRT.getUI()).setRangeColorMap(RTC
                .getColorMap());
        ((RangeSliderUI) legendSliderLB.getUI()).setRangeColorMap(LBC
                .getColorMap());
        ((RangeSliderUI) legendSliderRB.getUI()).setRangeColorMap(RBC
                .getColorMap());

        legendSliderLT.setMinimum(0);
        legendSliderRT.setMinimum(0);
        legendSliderLB.setMinimum(0);
        legendSliderRB.setMinimum(0);

        legendSliderLT.setMaximum(100);
        legendSliderRT.setMaximum(100);
        legendSliderLB.setMaximum(100);
        legendSliderRB.setMaximum(100);

        legendSliderLT.setValue(settings.getRangeSliderLowerValue(0));
        legendSliderRT.setValue(settings.getRangeSliderLowerValue(1));
        legendSliderLB.setValue(settings.getRangeSliderLowerValue(2));
        legendSliderRB.setValue(settings.getRangeSliderLowerValue(3));

        legendSliderLT.setUpperValue(settings.getRangeSliderUpperValue(0));
        legendSliderRT.setUpperValue(settings.getRangeSliderUpperValue(1));
        legendSliderLB.setUpperValue(settings.getRangeSliderUpperValue(2));
        legendSliderRB.setUpperValue(settings.getRangeSliderUpperValue(3));

        final RangeSliderUI frsLT = ((RangeSliderUI) legendSliderLT.getUI());
        comboBoxLTColorMaps.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                settings.setLTColorMap(colorMaps[comboBoxLTColorMaps
                        .getSelectedIndex()]);

                frsLT.setRangeColorMap(colorMaps[comboBoxLTColorMaps
                        .getSelectedIndex()]);
            }
        });

        final RangeSliderUI frsRT = ((RangeSliderUI) legendSliderLT.getUI());
        comboBoxRTColorMaps.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                settings.setRTColorMap(colorMaps[comboBoxRTColorMaps
                        .getSelectedIndex()]);

                frsRT.setRangeColorMap(colorMaps[comboBoxLTColorMaps
                        .getSelectedIndex()]);
            }
        });

        final RangeSliderUI frsLB = ((RangeSliderUI) legendSliderLT.getUI());
        comboBoxLBColorMaps.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                settings.setLBColorMap(colorMaps[comboBoxLBColorMaps
                        .getSelectedIndex()]);

                frsLB.setRangeColorMap(colorMaps[comboBoxLTColorMaps
                        .getSelectedIndex()]);
            }
        });

        final RangeSliderUI frsRB = ((RangeSliderUI) legendSliderLT.getUI());
        comboBoxRBColorMaps.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                settings.setRBColorMap(colorMaps[comboBoxRBColorMaps
                        .getSelectedIndex()]);

                frsRB.setRangeColorMap(colorMaps[comboBoxLTColorMaps
                        .getSelectedIndex()]);
            }
        });

        legendSliderLT.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                RangeSlider slider = (RangeSlider) e.getSource();
                GlobeState state = settings.getLTState();
                String var = state.getVariable().toString();
                settings.setVariableRange(0, var, slider.getValue(),
                        slider.getUpperValue());
            }
        });
        legendSliderRT.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                RangeSlider slider = (RangeSlider) e.getSource();
                GlobeState state = settings.getRTState();
                String var = state.getVariable().toString();
                settings.setVariableRange(1, var, slider.getValue(),
                        slider.getUpperValue());
            }
        });
        legendSliderLB.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                RangeSlider slider = (RangeSlider) e.getSource();
                GlobeState state = settings.getLBState();
                String var = state.getVariable().toString();
                settings.setVariableRange(2, var, slider.getValue(),
                        slider.getUpperValue());
            }
        });
        legendSliderRB.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                RangeSlider slider = (RangeSlider) e.getSource();
                GlobeState state = settings.getRBState();
                String var = state.getVariable().toString();
                settings.setVariableRange(3, var, slider.getValue(),
                        slider.getUpperValue());
            }
        });

        vcomponentsLT.add(legendSliderLT);
        vcomponentsRT.add(legendSliderRT);
        vcomponentsLB.add(legendSliderLB);
        vcomponentsRB.add(legendSliderRB);

        vcomponentsLT.add(GoggleSwing.verticalStrut(5));
        vcomponentsRT.add(GoggleSwing.verticalStrut(5));
        vcomponentsLB.add(GoggleSwing.verticalStrut(5));
        vcomponentsRB.add(GoggleSwing.verticalStrut(5));

        dataConfig.add(GoggleSwing.vBoxedComponents(vcomponentsLT, true));
        dataConfig.add(GoggleSwing.vBoxedComponents(vcomponentsRT, true));
        dataConfig.add(GoggleSwing.vBoxedComponents(vcomponentsLB, true));
        dataConfig.add(GoggleSwing.vBoxedComponents(vcomponentsRB, true));
    }

    private void createVisualTweakPanel() {
        final float heightDistortionSpacing = 0.001f;
        final JLabel heightDistortionSetting = new JLabel(""
                + settings.getHeightDistortion());
        final ChangeListener heightDistortionListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    settings.setHeightDistortion(source.getValue()
                            * heightDistortionSpacing);
                    heightDistortionSetting.setText(""
                            + settings.getHeightDistortion());
                }
            }
        };
        visualConfig.add(GoggleSwing.sliderBox("Height Distortion",
                heightDistortionListener, settings.getHeightDistortionMin(),
                settings.getHeightDistortionMax(), heightDistortionSpacing,
                settings.getHeightDistortion(), heightDistortionSetting));

        final ItemListener checkBoxListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    settings.setDynamicDimensions(true);
                } else {
                    settings.setDynamicDimensions(false);
                }
                timer.redraw();
            }
        };
        visualConfig.add(GoggleSwing.checkboxBox(
                "",
                new GoggleSwing.CheckBoxItem("Dynamic dimensions", settings
                        .isDynamicDimensions(), checkBoxListener)));

    }

    protected void handleFile(File file1, File file2) {
        if (file1 != null && file2 != null
                && NetCDFUtil.isAcceptableFile(file1)
                && NetCDFUtil.isAcceptableFile(file2)) {
            if (timer.isInitialized()) {
                timer.close();
            }
            timer = new NetCDFTimedPlayer(timeBar, frameCounter);

            timer.init(file1, file2);
            new Thread(timer).start();

            final String path = NetCDFUtil.getPath(file1) + "screenshots/";

            settings.setScreenshotPath(path);
        } else {
            if (null != file1 && null != file2) {
                final JOptionPane pane = new JOptionPane();
                pane.setMessage("Tried to open invalid file type.");
                final JDialog dialog = pane.createDialog("Alert");
                dialog.setVisible(true);
            } else {
                logger.error("File is null");
                System.exit(1);
            }
        }
    }

    protected void handleFile(File file) {
        if (file != null && NetCDFUtil.isAcceptableFile(file)) {
            if (timer.isInitialized()) {
                timer.close();
            }
            timer = new NetCDFTimedPlayer(timeBar, frameCounter);

            timer.init(file);
            new Thread(timer).start();

            final String path = NetCDFUtil.getPath(file) + "screenshots/";

            settings.setScreenshotPath(path);
        } else {
            if (null != file) {
                final JOptionPane pane = new JOptionPane();
                pane.setMessage("Tried to open invalid file type.");
                final JDialog dialog = pane.createDialog("Alert");
                dialog.setVisible(true);
            } else {
                logger.error("File is null");
                System.exit(1);
            }
        }
    }

    private File openFile() {
        final JFileChooser fileChooser = new JFileChooser();

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        final int result = fileChooser.showOpenDialog(this);

        // user clicked Cancel button on dialog
        if (result == JFileChooser.CANCEL_OPTION) {
            return null;
        } else {
            return fileChooser.getSelectedFile();
        }
    }

    // Callback methods for the various ui actions and listeners
    public void setTweakState(TweakState newState) {
        configPanel.setVisible(false);
        configPanel.remove(dataConfig);
        configPanel.remove(visualConfig);
        configPanel.remove(movieConfig);

        currentConfigState = newState;

        if (currentConfigState == TweakState.NONE) {
        } else if (currentConfigState == TweakState.DATA) {
            configPanel.setVisible(true);
            configPanel.add(dataConfig, BorderLayout.WEST);
        } else if (currentConfigState == TweakState.VISUAL) {
            configPanel.setVisible(true);
            configPanel.add(visualConfig, BorderLayout.WEST);
        } else if (currentConfigState == TweakState.MOVIE) {
            configPanel.setVisible(true);
            configPanel.add(movieConfig, BorderLayout.WEST);
        }
    }

    public static NetCDFTimedPlayer getTimer() {
        return timer;
    }
}
