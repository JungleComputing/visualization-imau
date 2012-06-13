package imau.visualization;

import imau.visualization.ImauSettings.varNames;
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

    private final ImauSettings settings = ImauSettings.getInstance();
    private final static Logger logger = LoggerFactory.getLogger(ImauPanel.class);

    private static final long serialVersionUID = 1L;

    protected JSlider timeBar;

    protected JFormattedTextField frameCounter;
    private TweakState currentConfigState = TweakState.NONE;

    private final JPanel configPanel;

    private final JPanel visualConfig, movieConfig;

    private final ImauWindow imauWindow;
    private static NetCDFTimedPlayer timer;

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
                handleFile(file);
            }
        });
        file.add(open);
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

        final JMenuItem showTweakPanel = new JMenuItem("Show configuration panel.");
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
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                return null;
            }

            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) {
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

        final JButton oneForwardButton = GoggleSwing.createImageButton("images/media-playback-oneforward.png", "Next",
                null);
        final JButton oneBackButton = GoggleSwing.createImageButton("images/media-playback-onebackward.png",
                "Previous", null);
        final JButton rewindButton = GoggleSwing.createImageButton("images/media-playback-rewind.png", "Rewind", null);
        final JButton screenshotButton = GoggleSwing.createImageButton("images/camera.png", "Screenshot", null);
        final JButton playButton = GoggleSwing.createImageButton("images/media-playback-start.png", "Start", null);
        final ImageIcon playIcon = GoggleSwing.createImageIcon("images/media-playback-start.png", "Start");
        final ImageIcon stopIcon = GoggleSwing.createImageIcon("images/media-playback-stop.png", "Start");

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        screenshotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // timer.stop();
                final InputHandler inputHandler = InputHandler.getInstance();
                final String fileName = "" + timer.getFrameNumber() + " {" + inputHandler.getRotation().get(0) + ","
                        + inputHandler.getRotation().get(1) + " - " + Float.toString(inputHandler.getViewDist()) + "} ";
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
                final JFormattedTextField source = (JFormattedTextField) e.getSource();
                if (source.hasFocus()) {
                    if (source == frameCounter) {
                        if (timer.isInitialized()) {
                            timer.setFrame(((Number) frameCounter.getValue()).intValue(), false);
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
        movieConfig.add(GoggleSwing.checkboxBox("", new GoggleSwing.CheckBoxItem("Rotation", settings.getMovieRotate(),
                checkBoxListener)));

        final JLabel rotationSetting = new JLabel("" + settings.getMovieRotationSpeedDef());
        final ChangeListener movieRotationSpeedListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    settings.setMovieRotationSpeed(source.getValue() * .25f);
                    rotationSetting.setText("" + settings.getMovieRotationSpeedDef());
                }
            }
        };
        movieConfig.add(GoggleSwing.sliderBox("Rotation Speed", movieRotationSpeedListener,
                (int) (settings.getMovieRotationSpeedMin() * 4f), (int) (settings.getMovieRotationSpeedMax() * 4f), 1,
                (int) (settings.getMovieRotationSpeedDef() * 4f), rotationSetting));

        movieConfig.add(GoggleSwing.buttonBox("", new String[] { "Start Recording" },
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
        visualConfig.add(GoggleSwing.sliderBox("Depth setting", depthListener, settings.getDepthMin(),
                settings.getDepthMax(), 1, settings.getDepthDef(), depthSetting));

        final ArrayList<Component> vcomponents = new ArrayList<Component>();
        vcomponents.add(new JLabel("Window Selection"));
        vcomponents.add(Box.createHorizontalGlue());

        final JComboBox comboBox = new JComboBox(new String[] { "All", "Left Bottom", "Right Bottom", "Left Top",
                "Right Top" });
        comboBox.addItemListener(new ItemListener() {
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

        final ArrayList<Component> vcomponentsLBR = new ArrayList<Component>();
        vcomponentsLBR.add(new JLabel("Left Bottom Red Selection"));
        vcomponentsLBR.add(Box.createHorizontalGlue());

        final JComboBox comboBoxLBR = new JComboBox(variables);
        comboBoxLBR.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int selection = comboBoxLBR.getSelectedIndex();
                if (selection == 0) {
                    settings.setLBBand(0, varNames.SSH);
                } else if (selection == 1) {
                    settings.setLBBand(0, varNames.SHF);
                } else if (selection == 2) {
                    settings.setLBBand(0, varNames.SFWF);
                } else if (selection == 3) {
                    settings.setLBBand(0, varNames.HMXL);
                } else if (selection == 4) {
                    settings.setLBBand(0, varNames.SALT);
                } else if (selection == 5) {
                    settings.setLBBand(0, varNames.TEMP);
                }
            }
        });
        comboBoxLBR.setMaximumSize(new Dimension(300, 25));
        vcomponentsLBR.add(comboBoxLBR);
        vcomponentsLBR.add(GoggleSwing.verticalStrut(5));

        visualConfig.add(GoggleSwing.vBoxedComponents(vcomponentsLBR, true));

        final ArrayList<Component> vcomponentsLBG = new ArrayList<Component>();
        vcomponentsLBG.add(new JLabel("Left Bottom Green Selection"));
        vcomponentsLBG.add(Box.createHorizontalGlue());

        final JComboBox comboBoxLBG = new JComboBox(variables);
        comboBoxLBG.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int selection = comboBoxLBG.getSelectedIndex();
                if (selection == 0) {
                    settings.setLBBand(1, varNames.SSH);
                } else if (selection == 1) {
                    settings.setLBBand(1, varNames.SHF);
                } else if (selection == 2) {
                    settings.setLBBand(1, varNames.SFWF);
                } else if (selection == 3) {
                    settings.setLBBand(1, varNames.HMXL);
                } else if (selection == 4) {
                    settings.setLBBand(1, varNames.SALT);
                } else if (selection == 5) {
                    settings.setLBBand(1, varNames.TEMP);
                }
            }
        });
        comboBoxLBG.setMaximumSize(new Dimension(300, 25));
        vcomponentsLBG.add(comboBoxLBG);
        vcomponentsLBG.add(GoggleSwing.verticalStrut(5));

        visualConfig.add(GoggleSwing.vBoxedComponents(vcomponentsLBG, true));

        final ArrayList<Component> vcomponentsLBB = new ArrayList<Component>();
        vcomponentsLBB.add(new JLabel("Left Bottom Blue Selection"));
        vcomponentsLBB.add(Box.createHorizontalGlue());

        final JComboBox comboBoxLBB = new JComboBox(variables);
        comboBoxLBB.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int selection = comboBoxLBB.getSelectedIndex();
                if (selection == 0) {
                    settings.setLBBand(2, varNames.SSH);
                } else if (selection == 1) {
                    settings.setLBBand(2, varNames.SHF);
                } else if (selection == 2) {
                    settings.setLBBand(2, varNames.SFWF);
                } else if (selection == 3) {
                    settings.setLBBand(2, varNames.HMXL);
                } else if (selection == 4) {
                    settings.setLBBand(2, varNames.SALT);
                } else if (selection == 5) {
                    settings.setLBBand(2, varNames.TEMP);
                }
            }
        });
        comboBoxLBB.setMaximumSize(new Dimension(300, 25));
        vcomponentsLBB.add(comboBoxLBB);
        vcomponentsLBB.add(GoggleSwing.verticalStrut(5));

        visualConfig.add(GoggleSwing.vBoxedComponents(vcomponentsLBB, true));

    }

    protected void handleFile(File file) {
        if (file != null && NetCDFUtil.isAcceptableFile(file)) {

            timer = new NetCDFTimedPlayer(imauWindow, timeBar, frameCounter);

            timer.init(file);
            new Thread(timer).start();

            final String path = NetCDFUtil.getPath(file);

            // System.out.println("file number: " +
            // NetCDFUtil.getFrameNumber(file));
            // String prefix = NetCDFUtil.getPrefix(file);
            //
            // int numberOfFiles = NetCDFUtil.getNumFiles(path, ".nc");
            // int startFrame = NetCDFUtil.getLowestFileNumber(file);

            // String filename = prefix + String.format("%04d", 75) + ".nc";
            // NetcdfFile ncfile = NetCDFUtil.open(filename);
            // NetCDFUtil.printInfo(ncfile);

            // timer = new Hdf5TimedPlayer(amuseWindow, timeBar,
            // frameCounter);
            // timer.open(path, prefix);
            // timer.init();
            // new Thread(timer).start();

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
