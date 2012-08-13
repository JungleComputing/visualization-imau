package imau.visualization;

import imau.visualization.ImauSettings.varNames;
import imau.visualization.adaptor.BandCombination;
import imau.visualization.adaptor.NetCDFTimedPlayer;
import imau.visualization.netcdf.NetCDFUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
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

import openglCommon.CommonPanel;
import openglCommon.util.InputHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.GoggleSwing;

public class ImauPanel extends CommonPanel {
    public static enum TweakState {
        NONE, VISUAL, MOVIE
    }

    private final ImauSettings       settings           = ImauSettings
                                                                .getInstance();
    private final static Logger      logger             = LoggerFactory
                                                                .getLogger(ImauPanel.class);

    private static final long        serialVersionUID   = 1L;

    protected JSlider                timeBar;

    protected JFormattedTextField    frameCounter;
    private TweakState               currentConfigState = TweakState.NONE;

    private final JPanel             configPanel;

    private final JPanel             visualConfig, movieConfig;

    private final ImauWindow         imauWindow;
    private static NetCDFTimedPlayer timer;

    private File                     file1;

    public ImauPanel(ImauWindow imauWindow, String path, String cmdlnfileName) {
        super(imauWindow, InputHandler.getInstance());
        this.imauWindow = imauWindow;

        timeBar = new openglCommon.util.CustomJSlider();
        timeBar.setValue(0);
        timeBar.setMajorTickSpacing(5);
        timeBar.setMinorTickSpacing(1);
        timeBar.setMaximum(0);
        timeBar.setMinimum(0);
        timeBar.setPaintTicks(true);
        timeBar.setSnapToTicks(true);

        timer = new NetCDFTimedPlayer(imauWindow, timeBar, frameCounter);

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

        final JMenuItem showTweakPanel = new JMenuItem(
                "Show configuration panel.");
        showTweakPanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setTweakState(TweakState.VISUAL);
            }
        });
        options.add(showTweakPanel);
        menuBar.add(options);

        add(menuBar, BorderLayout.NORTH);

        // Make the "media player" panel
        final JPanel bottomPanel = createBottomPanel();

        // Add the tweaks panels
        configPanel = new JPanel();
        add(configPanel, BorderLayout.WEST);
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setPreferredSize(new Dimension(200, 0));
        configPanel.setVisible(false);

        visualConfig = new JPanel();
        visualConfig.setLayout(new BoxLayout(visualConfig, BoxLayout.Y_AXIS));
        visualConfig.setMinimumSize(configPanel.getPreferredSize());
        createVisualTweakPanel();

        movieConfig = new JPanel();
        movieConfig.setLayout(new BoxLayout(movieConfig, BoxLayout.Y_AXIS));
        movieConfig.setMinimumSize(configPanel.getPreferredSize());
        createMovieTweakPanel();

        add(bottomPanel, BorderLayout.SOUTH);

        // Read command line file information
        if (cmdlnfileName != null) {
            final File cmdlnfile = new File(cmdlnfileName);
            handleFile(cmdlnfile);
        }
    }

    void close() {
        imauWindow.dispose(glCanvas);
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

        screenshotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // timer.stop();
                final InputHandler inputHandler = InputHandler.getInstance();
                final String fileName = "" + timer.getFrameNumber() + " {"
                        + inputHandler.getRotation().get(0) + ","
                        + inputHandler.getRotation().get(1) + " - "
                        + Float.toString(inputHandler.getViewDist()) + "} ";
                imauWindow.makeSnapshot(fileName);
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
                    timer.setFrame(timeBar.getValue(), false);
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
                            timer.setFrame(((Number) frameCounter.getValue())
                                    .intValue(), false);
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

    private void createVisualTweakPanel() {
        final ItemListener listener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                setTweakState(TweakState.NONE);
            }
        };
        visualConfig.add(GoggleSwing.titleBox("Configuration", listener));

        final JLabel depthSetting = new JLabel("" + settings.getDepthDef());
        final ChangeListener depthListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    settings.setDepthDef(source.getValue());
                    depthSetting.setText("" + settings.getDepthDef());
                }
            }
        };
        visualConfig.add(GoggleSwing.sliderBox("Depth setting", depthListener,
                settings.getDepthMin(), settings.getDepthMax(), 1,
                settings.getDepthDef(), depthSetting));

        final ArrayList<Component> vcomponents = new ArrayList<Component>();
        vcomponents.add(new JLabel("Window Selection"));
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
        comboBox.setMaximumSize(new Dimension(300, 25));
        vcomponents.add(comboBox);
        vcomponents.add(GoggleSwing.verticalStrut(5));

        visualConfig.add(GoggleSwing.vBoxedComponents(vcomponents, true));

        String[] variables = { "SSH", "SHF", "SFWF", "HMXL", "SALT", "TEMP" };
        final String[] colorMaps = NetCDFUtil.getColorMaps();

        final JComboBox comboBoxLT = new JComboBox(variables);
        comboBoxLT.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = comboBoxLT.getSelectedIndex();
                if (selection == 0 || selection == -1) {
                    settings.setLTBand(varNames.SSH);
                } else if (selection == 1) {
                    settings.setLTBand(varNames.SHF);
                } else if (selection == 2) {
                    settings.setLTBand(varNames.SFWF);
                } else if (selection == 3) {
                    settings.setLTBand(varNames.HMXL);
                } else if (selection == 4) {
                    settings.setLTBand(varNames.SALT);
                } else if (selection == 5) {
                    settings.setLTBand(varNames.TEMP);
                }
            }
        });
        final JComboBox comboBoxRT = new JComboBox(variables);
        comboBoxRT.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = comboBoxRT.getSelectedIndex();
                if (selection == 0 || selection == -1) {
                    settings.setRTBand(varNames.SSH);
                } else if (selection == 1) {
                    settings.setRTBand(varNames.SHF);
                } else if (selection == 2) {
                    settings.setRTBand(varNames.SFWF);
                } else if (selection == 3) {
                    settings.setRTBand(varNames.HMXL);
                } else if (selection == 4) {
                    settings.setRTBand(varNames.SALT);
                } else if (selection == 5) {
                    settings.setRTBand(varNames.TEMP);
                }
            }
        });
        final JComboBox comboBoxLB = new JComboBox(variables);
        comboBoxLB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = comboBoxLB.getSelectedIndex();
                if (selection == 0 || selection == -1) {
                    settings.setLBBand(varNames.SSH);
                } else if (selection == 1) {
                    settings.setLBBand(varNames.SHF);
                } else if (selection == 2) {
                    settings.setLBBand(varNames.SFWF);
                } else if (selection == 3) {
                    settings.setLBBand(varNames.HMXL);
                } else if (selection == 4) {
                    settings.setLBBand(varNames.SALT);
                } else if (selection == 5) {
                    settings.setLBBand(varNames.TEMP);
                }
            }
        });
        final JComboBox comboBoxRB = new JComboBox(variables);
        comboBoxRB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selection = comboBoxRB.getSelectedIndex();
                if (selection == 0 || selection == -1) {
                    settings.setRBBand(varNames.SSH);
                } else if (selection == 1) {
                    settings.setRBBand(varNames.SHF);
                } else if (selection == 2) {
                    settings.setRBBand(varNames.SFWF);
                } else if (selection == 3) {
                    settings.setRBBand(varNames.HMXL);
                } else if (selection == 4) {
                    settings.setRBBand(varNames.SALT);
                } else if (selection == 5) {
                    settings.setRBBand(varNames.TEMP);
                }
            }
        });

        final ArrayList<Component> vcomponentsLT = new ArrayList<Component>();
        final ArrayList<Component> vcomponentsRT = new ArrayList<Component>();
        final ArrayList<Component> vcomponentsLB = new ArrayList<Component>();
        final ArrayList<Component> vcomponentsRB = new ArrayList<Component>();

        vcomponentsLT.add(new JLabel("Left Top Selection"));
        vcomponentsRT.add(new JLabel("Right Top Selection"));
        vcomponentsLB.add(new JLabel("Left Bottom Selection"));
        vcomponentsRB.add(new JLabel("Right Bottom Selection"));

        vcomponentsLT.add(Box.createHorizontalGlue());
        vcomponentsRT.add(Box.createHorizontalGlue());
        vcomponentsLB.add(Box.createHorizontalGlue());
        vcomponentsRB.add(Box.createHorizontalGlue());

        final ArrayList<Component> hcomponentsLT = new ArrayList<Component>();
        final ArrayList<Component> hcomponentsRT = new ArrayList<Component>();
        final ArrayList<Component> hcomponentsLB = new ArrayList<Component>();
        final ArrayList<Component> hcomponentsRB = new ArrayList<Component>();

        final JComboBox comboBoxLTColorMaps = new JComboBox(colorMaps);
        final JComboBox comboBoxRTColorMaps = new JComboBox(colorMaps);
        final JComboBox comboBoxLBColorMaps = new JComboBox(colorMaps);
        final JComboBox comboBoxRBColorMaps = new JComboBox(colorMaps);

        comboBoxLTColorMaps.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                settings.setLTColorMap(colorMaps[comboBoxLTColorMaps
                        .getSelectedIndex()]);
            }
        });
        comboBoxRTColorMaps.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                settings.setRTColorMap(colorMaps[comboBoxRTColorMaps
                        .getSelectedIndex()]);
            }
        });
        comboBoxLBColorMaps.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                settings.setLBColorMap(colorMaps[comboBoxLBColorMaps
                        .getSelectedIndex()]);
            }
        });
        comboBoxRBColorMaps.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                settings.setRBColorMap(colorMaps[comboBoxRBColorMaps
                        .getSelectedIndex()]);
            }
        });

        BandCombination LTC = settings.getBandComboLT();
        BandCombination RTC = settings.getBandComboRT();
        BandCombination LBC = settings.getBandComboLB();
        BandCombination RBC = settings.getBandComboRB();

        comboBoxLT.setSelectedIndex(LTC.getBandIndex());
        comboBoxRT.setSelectedIndex(RTC.getBandIndex());
        comboBoxLB.setSelectedIndex(LBC.getBandIndex());
        comboBoxRB.setSelectedIndex(RBC.getBandIndex());

        comboBoxLTColorMaps.setSelectedItem(LTC.colorMapFileName);
        comboBoxRTColorMaps.setSelectedItem(RTC.colorMapFileName);
        comboBoxLBColorMaps.setSelectedItem(LBC.colorMapFileName);
        comboBoxRBColorMaps.setSelectedItem(RBC.colorMapFileName);

        comboBoxLT.setMaximumSize(new Dimension(300, 25));
        comboBoxRT.setMaximumSize(new Dimension(300, 25));
        comboBoxLB.setMaximumSize(new Dimension(300, 25));
        comboBoxRB.setMaximumSize(new Dimension(300, 25));

        comboBoxLTColorMaps.setMaximumSize(new Dimension(300, 25));
        comboBoxRTColorMaps.setMaximumSize(new Dimension(300, 25));
        comboBoxLBColorMaps.setMaximumSize(new Dimension(300, 25));
        comboBoxRBColorMaps.setMaximumSize(new Dimension(300, 25));

        hcomponentsLT.add(comboBoxLT);
        hcomponentsRT.add(comboBoxRT);
        hcomponentsLB.add(comboBoxLB);
        hcomponentsRB.add(comboBoxRB);

        hcomponentsLT.add(comboBoxLTColorMaps);
        hcomponentsRT.add(comboBoxRTColorMaps);
        hcomponentsLB.add(comboBoxLBColorMaps);
        hcomponentsRB.add(comboBoxRBColorMaps);

        vcomponentsLT.add(GoggleSwing.hBoxedComponents(hcomponentsLT));
        vcomponentsRT.add(GoggleSwing.hBoxedComponents(hcomponentsRT));
        vcomponentsLB.add(GoggleSwing.hBoxedComponents(hcomponentsLB));
        vcomponentsRB.add(GoggleSwing.hBoxedComponents(hcomponentsRB));

        vcomponentsLT.add(GoggleSwing.verticalStrut(5));
        vcomponentsRT.add(GoggleSwing.verticalStrut(5));
        vcomponentsLB.add(GoggleSwing.verticalStrut(5));
        vcomponentsRB.add(GoggleSwing.verticalStrut(5));

        visualConfig.add(GoggleSwing.vBoxedComponents(vcomponentsLT, true));
        visualConfig.add(GoggleSwing.vBoxedComponents(vcomponentsRT, true));
        visualConfig.add(GoggleSwing.vBoxedComponents(vcomponentsLB, true));
        visualConfig.add(GoggleSwing.vBoxedComponents(vcomponentsRB, true));
    }

    protected void handleFile(File file) {
        if (file != null && NetCDFUtil.isAcceptableFile(file)) {

            timer = new NetCDFTimedPlayer(imauWindow, timeBar, frameCounter);

            timer.init(file);
            new Thread(timer).start();

            final String path = NetCDFUtil.getPath(file);

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

    protected void handleFile(File file1, File file2) {
        if (file1 != null && NetCDFUtil.isAcceptableFile(file1)
                && file2 != null && NetCDFUtil.isAcceptableFile(file2)) {
            // timer = new NetCDFTimedPlayer(imauWindow, timeBar, frameCounter);
            timer.close();
            timer.init(file1, file2);
            // new Thread(timer).start();

            final String path = NetCDFUtil.getPath(file1);

            settings.setScreenshotPath(path);
        } else {
            if (null != file1) {
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
        configPanel.remove(visualConfig);
        configPanel.remove(movieConfig);

        currentConfigState = newState;

        if (currentConfigState == TweakState.NONE) {
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
