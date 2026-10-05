import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class CarConfig extends JFrame {

    //Theme constants innit
    public static final Color BG_DARK    = new Color(20, 22, 28);
    public static final Color PANEL_DARK = new Color(28, 31, 38);
    public static final Color ACCENT     = new Color(255, 160, 30);
    public static final Color TEXT_LIGHT = new Color(232, 233, 238);

   
    private CarConfiguration config;            //Active workspace
    private List<CarConfiguration> savedBuilds; //Saved history workspace

    //Part 3 (Azzie)
    private CardLayout cardLayout;
    private JPanel mainArea;
    private DrivePanelStub drivePanel;          
    private boolean driving = false;

    
    private JLabel priceLabel;
    private JLabel maxSpeedLabel;
    private JTextArea stickerTextArea;
    private DefaultListModel<String> savedBuildsListModel;
    private JPanel previewPanel;

    
    private PaintColor[] paintColors;
    private RimFinish[] rimFinishes;
    private WindowTint[] windowTints;
    private InteriorOption[] interiorOptions;
    private CarPackage[] carPackages;

    //Main window class constructor
    public CarConfig() {
        initData();
        initUI();
        refresh(); 
    }

    
    private void initData() {
        savedBuilds = new ArrayList<>();

        // Initializing paint color options
        paintColors = new PaintColor[] {
            new PaintColor("Midnight Black", new Color(15, 15, 15)),
            new PaintColor("Alpine White", new Color(240, 240, 240)),
            new PaintColor("Racing Red", new Color(210, 35, 42)),
            new PaintColor("Electric Blue", new Color(30, 110, 220))
        };

        // Initializing rim finish colors
        rimFinishes = new RimFinish[] {
            new RimFinish("Silver Standard", 0, Color.LIGHT_GRAY),
            new RimFinish("Matte Black", 300, new Color(40, 40, 40)),
            new RimFinish("Polished Gold", 750, new Color(212, 175, 55))
        };

        // Initializing window tint colors
        windowTints = new WindowTint[] {
            new WindowTint("Clear", new Color(255, 255, 255, 0), 0),
            new WindowTint("Light Tint", new Color(0, 0, 0, 50), 50),
            new WindowTint("Dark Smoke", new Color(0, 0, 0, 150), 150)
        };


        interiorOptions = new InteriorOption[] {
            new ClothInterior(),
            new LeatherInterior(),
            new PremiumInterior()
        };


        carPackages = new CarPackage[] {
            new TechPackage(),
            new PerformancePackage(),
            new LuxuryPackage()
        };

        //Instantiation
        config = new CarConfiguration();
        config.model = Part1Stubs.AVAILABLE_MODELS[0];
        config.wheels = Part1Stubs.AVAILABLE_WHEELS[0];
        config.color = paintColors[0];
        config.rimFinish = rimFinishes[0];
        config.tint = windowTints[0];
        config.interior = interiorOptions[0];
        config.packages = new ArrayList<>();
    }

    //The UI now
    private void initUI() {
        setTitle("Car Configurator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Chat GPT
        setSize(1200, 800);
        setLocationRelativeTo(null);

        
        //CONTAINERS
        cardLayout = new CardLayout();
        mainArea = new JPanel(cardLayout);

        
        JPanel configuratorView = new JPanel(new BorderLayout());
        configuratorView.setBackground(BG_DARK);

        // Top Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(PANEL_DARK);
        JLabel title = new JLabel("Car Configurator");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(ACCENT);
        header.add(title);
        configuratorView.add(header, BorderLayout.NORTH);

        
        JPanel controlsPanel = buildControlsPanel();
        JScrollPane scrollControls = new JScrollPane(controlsPanel);
        scrollControls.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);//side to side off
        scrollControls.setPreferredSize(new Dimension(510, 0));
        scrollControls.getVerticalScrollBar().setUnitIncrement(16);
        configuratorView.add(scrollControls, BorderLayout.WEST);

        //Car render preview (Azzie)
        previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BG_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());

               
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                CarRenderer.renderScene(g, config.model, config.wheels, config.rimFinish.color,
                        config.color.awtColor, config.tint, config.spoiler, config.stripes,
                        0, cx, cy, getWidth()); //FROM AZZIE'S WORK yaani calls from Azzie's work
            }
        };
        configuratorView.add(previewPanel, BorderLayout.CENTER);

        
        JPanel eastPanel = buildEastPanel();
        eastPanel.setPreferredSize(new Dimension(320, 0));
        configuratorView.add(eastPanel, BorderLayout.EAST);

        
        JPanel bottomBar = buildBottomBar();
        configuratorView.add(bottomBar, BorderLayout.SOUTH);

        //FROM Azzie again
        drivePanel = new DrivePanelStub();

        
        mainArea.add(configuratorView, "CONFIG");
        mainArea.add(drivePanel, "DRIVE");

        add(mainArea);
    }

    
    private JPanel buildControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_DARK);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        //Selecting the moodel
        String[] modelNames = new String[]{
                Part1Stubs.AVAILABLE_MODELS[0].name, 
                Part1Stubs.AVAILABLE_MODELS[1].name
        };
        panel.add(buildSegment("Car Model", modelNames, 0, idx -> {
            config.model = Part1Stubs.AVAILABLE_MODELS[idx];
            //Compatibility test
            if (!config.model.isWheelCompatible(config.wheels)) {
                config.wheels = Part1Stubs.AVAILABLE_WHEELS[0]; 
            }
            refresh();
        }));

        //Selecting the wheels
        String[] wheelNames = new String[]{
                Part1Stubs.AVAILABLE_WHEELS[0].name, 
                Part1Stubs.AVAILABLE_WHEELS[1].name
        };
        panel.add(buildSegment("Wheel Type", wheelNames, 0, idx -> {
            CarWheel chosen = Part1Stubs.AVAILABLE_WHEELS[idx];
            if (config.model.isWheelCompatible(chosen)) {
                config.wheels = chosen;
            } else {
                JOptionPane.showMessageDialog(this, "Wheel option incompatible with " + config.model.name);
            }
            refresh();
        }));

        //Color of car
        panel.add(buildPaintColorGrid("Paint Color"));

        //Rim (pause) finish (pause again)
        String[] rimNames = new String[rimFinishes.length];
        for (int i = 0; i < rimFinishes.length; i++) rimNames[i] = rimFinishes[i].name;
        panel.add(buildSegment("Rim Finish", rimNames, 0, idx -> {
            config.rimFinish = rimFinishes[idx];
            refresh();
        }));

        //Selecting window tint
        String[] tintNames = new String[windowTints.length];
        for (int i = 0; i < windowTints.length; i++) tintNames[i] = windowTints[i].name;
        panel.add(buildSegment("Window Tint", tintNames, 0, idx -> {
            config.tint = windowTints[idx];
            refresh();
        }));

        //Selecting exterior
        String[] intNames = new String[interiorOptions.length];
        for (int i = 0; i < interiorOptions.length; i++) intNames[i] = interiorOptions[i].name;
        panel.add(buildSegment("Interior", intNames, 0, idx -> {
            config.interior = interiorOptions[idx];
            refresh();
        }));

        
        JPanel addOnPanel = new JPanel(new GridLayout(2, 1));
        addOnPanel.setOpaque(false);
        JCheckBox spoilerCheck = new JCheckBox("Rear Spoiler (+$800)");
        JCheckBox stripesCheck = new JCheckBox("Racing Stripes (+$500)");
        styleCheckBox(spoilerCheck);
        styleCheckBox(stripesCheck);

        spoilerCheck.addActionListener(e -> { config.spoiler = spoilerCheck.isSelected(); refresh(); });
        stripesCheck.addActionListener(e -> { config.stripes = stripesCheck.isSelected(); refresh(); });

        addOnPanel.add(spoilerCheck);
        addOnPanel.add(stripesCheck);

        JPanel addOnWrapper = createTitledPanel("Add-Ons");
        addOnWrapper.add(addOnPanel);
        panel.add(addOnWrapper);

        //Packages coz we Gucci
        JPanel pkgPanel = new JPanel(new GridLayout(carPackages.length, 1));
        pkgPanel.setOpaque(false);
        for (CarPackage pkg : carPackages) {
            JCheckBox cb = new JCheckBox(pkg.name + " (+$" + pkg.priceDelta + ")");
            styleCheckBox(cb);
            cb.addActionListener(e -> {
                if (cb.isSelected()) {
                    if (!config.packages.contains(pkg)) config.packages.add(pkg);
                } else {
                    config.packages.remove(pkg);
                }
                refresh();
            });
            pkgPanel.add(cb);
        }
        JPanel pkgWrapper = createTitledPanel("Packages");
        pkgWrapper.add(pkgPanel);
        panel.add(pkgWrapper);

        //Plate
        JPanel platePanel = createTitledPanel("License Plate");
        JTextField plateField = new JTextField(12);
        plateField.addActionListener(e -> { config.plateText = plateField.getText(); refresh(); });
        platePanel.add(plateField);
        panel.add(platePanel);

        return panel;
    }

    //Reusability (100% ChatGPT)
    private JPanel buildSegment(String title, String[] labels, int defaultIndex, IntConsumer onSelect) {
        JPanel wrapper = createTitledPanel(title);
        JPanel segPanel = new JPanel(new GridLayout(1, labels.length, 4, 4));
        segPanel.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            JToggleButton btn = new JToggleButton(labels[i]);
            btn.setFocusPainted(false);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            btn.setBackground(BG_DARK);
            btn.setForeground(TEXT_LIGHT);

            if (i == defaultIndex) btn.setSelected(true);

            btn.addActionListener(e -> {
                onSelect.accept(index);
            });

            group.add(btn);
            segPanel.add(btn);
        }
        wrapper.add(segPanel);
        return wrapper;
    }

    
    private JPanel buildPaintColorGrid(String title) {
        JPanel wrapper = createTitledPanel(title);
        JPanel grid = new JPanel(new GridLayout(1, paintColors.length, 5, 5));
        grid.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < paintColors.length; i++) {
            final int idx = i;
            PaintColor pc = paintColors[i];

            JToggleButton colorBtn = new JToggleButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(pc.awtColor);
                    g.fillRect(4, 4, getWidth() - 8, getHeight() - 8);
                    if (isSelected()) {
                        g.setColor(ACCENT);
                        g.drawRect(2, 2, getWidth() - 5, getHeight() - 5);
                    }
                }
            };
            colorBtn.setPreferredSize(new Dimension(32, 32));
            colorBtn.setToolTipText(pc.name);
            colorBtn.setFocusPainted(false);
            colorBtn.setBackground(BG_DARK);

            if (i == 0) colorBtn.setSelected(true);

            colorBtn.addActionListener(e -> {
                config.color = paintColors[idx];
                refresh();
            });

            group.add(colorBtn);
            grid.add(colorBtn);
        }
        wrapper.add(grid);
        return wrapper;
    }

    //List of Saved builds
    private JPanel buildEastPanel() {
        JPanel east = new JPanel(new BorderLayout(5, 5));
        east.setBackground(PANEL_DARK);
        east.setBorder(new EmptyBorder(10, 10, 10, 10));

        
        JLabel stickerTitle = new JLabel("Window Sticker Receipt");
        stickerTitle.setForeground(ACCENT);
        stickerTitle.setFont(new Font("SansSerif", Font.BOLD, 14));

        stickerTextArea = new JTextArea();
        stickerTextArea.setEditable(false);
        stickerTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11)); 
        stickerTextArea.setBackground(BG_DARK);
        stickerTextArea.setForeground(TEXT_LIGHT);

        JScrollPane stickerScroll = new JScrollPane(stickerTextArea);

        // Saved Builds
        JLabel buildsTitle = new JLabel("Saved Builds");
        buildsTitle.setForeground(ACCENT);
        buildsTitle.setFont(new Font("SansSerif", Font.BOLD, 14));

        savedBuildsListModel = new DefaultListModel<>();
        JList<String> buildsList = new JList<>(savedBuildsListModel);
        buildsList.setBackground(BG_DARK);
        buildsList.setForeground(TEXT_LIGHT);

        JButton saveBuildBtn = new JButton("Save Current Build");
        saveBuildBtn.setBackground(ACCENT);
        saveBuildBtn.setFocusPainted(false);

        
        saveBuildBtn.addActionListener(e -> {
            CarConfiguration snapshot = config.cloneSnapshot(); 
            savedBuilds.add(snapshot);
            savedBuildsListModel.addElement(snapshot.model.name + " - $" + snapshot.calculateTotalPrice());
        });

        JPanel stickerContainer = new JPanel(new BorderLayout(5, 5));
        stickerContainer.setOpaque(false);
        stickerContainer.add(stickerTitle, BorderLayout.NORTH);
        stickerContainer.add(stickerScroll, BorderLayout.CENTER);

        JPanel savedContainer = new JPanel(new BorderLayout(5, 5));
        savedContainer.setOpaque(false);
        savedContainer.add(buildsTitle, BorderLayout.NORTH);
        savedContainer.add(new JScrollPane(buildsList), BorderLayout.CENTER);
        savedContainer.add(saveBuildBtn, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, stickerContainer, savedContainer);
        splitPane.setResizeWeight(0.6);
        splitPane.setOpaque(false);

        east.add(splitPane, BorderLayout.CENTER);
        return east;
    }

    //Going to drive mode
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PANEL_DARK);
        bar.setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel metricsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        metricsPanel.setOpaque(false);

        priceLabel = new JLabel("Total: $0");
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        priceLabel.setForeground(TEXT_LIGHT);

        maxSpeedLabel = new JLabel("Max Speed: 0 mph");
        maxSpeedLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        maxSpeedLabel.setForeground(ACCENT);

        metricsPanel.add(priceLabel);
        metricsPanel.add(maxSpeedLabel);

        JButton driveToggleBtn = new JButton("Take It For A Drive");
        driveToggleBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        driveToggleBtn.setBackground(ACCENT);
        driveToggleBtn.setFocusPainted(false);
        driveToggleBtn.addActionListener(e -> toggleDriveMode());

        bar.add(metricsPanel, BorderLayout.WEST);
        bar.add(driveToggleBtn, BorderLayout.EAST);

        return bar;
    }

    //Drive view
    private void toggleDriveMode() {
        driving = !driving;
        if (driving) {
            cardLayout.show(mainArea, "DRIVE");
            drivePanel.startDriving(config); 
        } else {
            cardLayout.show(mainArea, "CONFIG");
            drivePanel.stopDriving();       
        }
    }


    private void refresh() {
        if (priceLabel != null) {
            priceLabel.setText("Total: $" + config.calculateTotalPrice());
        }
        if (maxSpeedLabel != null) {
            maxSpeedLabel.setText("Max Speed: " + config.maxSpeed() + " mph"); 
        }
        if (stickerTextArea != null) {
            stickerTextArea.setText(config.stickerText());
        }
        if (previewPanel != null) {
            previewPanel.repaint(); 
        }
    }


    private JPanel createTitledPanel(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 65, 75)), title,
                0, 0, new Font("SansSerif", Font.BOLD, 12), TEXT_LIGHT));
        return p;
    }

    private void styleCheckBox(JCheckBox cb) {
        cb.setOpaque(false);
        cb.setForeground(TEXT_LIGHT);
        cb.setFocusPainted(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CarConfig().setVisible(true);
        });
    }
}



//100% ChatGPT again :(
class PaintColor {
    String name;
    Color awtColor;

    public PaintColor(String name, Color awtColor) {
        this.name = name;
        this.awtColor = awtColor;
    }
}

class RimFinish {
    String name;
    int priceDelta;
    Color color;

    public RimFinish(String name, int priceDelta, Color color) {
        this.name = name;
        this.priceDelta = priceDelta;
        this.color = color;
    }
}

class WindowTint {
    String name;
    Color tintColor;
    int alpha;

    public WindowTint(String name, Color tintColor, int alpha) {
        this.name = name;
        this.tintColor = tintColor;
        this.alpha = alpha;
    }
}



abstract class InteriorOption {
    String name;
    int priceDelta;
}

class ClothInterior extends InteriorOption {
    public ClothInterior() {
        this.name = "Standard Cloth Interior";
        this.priceDelta = 0;
    }
}

class LeatherInterior extends InteriorOption {
    public LeatherInterior() {
        this.name = "Leather Interior";
        this.priceDelta = 2200;
    }
}

class PremiumInterior extends InteriorOption {
    public PremiumInterior() {
        this.name = "Premium Executive Interior";
        this.priceDelta = 4500;
    }
}

abstract class CarPackage {
    String name;
    int priceDelta;
    int topSpeedBonus;
}

class TechPackage extends CarPackage {
    public TechPackage() {
        this.name = "Tech Package";
        this.priceDelta = 1500;
        this.topSpeedBonus = 0;
    }
}

class PerformancePackage extends CarPackage {
    public PerformancePackage() {
        this.name = "Performance Package";
        this.priceDelta = 3200;
        this.topSpeedBonus = 30;
    }
}

class LuxuryPackage extends CarPackage {
    public LuxuryPackage() {
        this.name = "Luxury Package";
        this.priceDelta = 2600;
        this.topSpeedBonus = 0;
    }
}



//Addition for maths
class CarConfiguration {
    CarModel model;                  
    PaintColor color;                
    CarWheel wheels;                 
    RimFinish rimFinish;             
    WindowTint tint;                 
    InteriorOption interior;         
    boolean spoiler;                 
    boolean stripes;                 
    String plateText = "STUDIO";    
    List<CarPackage> packages = new ArrayList<>(); 

    //This thing is crazy hard so I asked Chat for help again coz of Polymorphic addition
    public int calculateTotalPrice() {
        int total = (model != null ? model.basePrice : 0)
                  + (wheels != null ? wheels.priceDelta : 0)
                  + (rimFinish != null ? rimFinish.priceDelta : 0)
                  + (interior != null ? interior.priceDelta : 0);

        if (spoiler) total += 800;
        if (stripes) total += 500;

        for (CarPackage p : packages) {
            total += p.priceDelta;
        }
        return total;
    }


    public int maxSpeed() {
        int bonus = 0;
        for (CarPackage p : packages) {
            bonus += p.topSpeedBonus;
        }
        return (model != null ? model.baseMaxSpeed : 0) + bonus;
    }

    //Receipt
    public String stickerText() {
        StringBuilder sb = new StringBuilder();
        sb.append("===================================\n");
        sb.append("        OFFICIAL VEHICLE STICKER    \n");
        sb.append("===================================\n");
        if (model != null) {
            sb.append(String.format("%-22s $%-6d\n", model.name, model.basePrice));
        }
        if (wheels != null) {
            sb.append(String.format("Wheels: %-14s $%-6d\n", wheels.name, wheels.priceDelta));
        }
        if (rimFinish != null) {
            sb.append(String.format("Rim: %-17s $%-6d\n", rimFinish.name, rimFinish.priceDelta));
        }
        if (interior != null) {
            sb.append(String.format("Interior: %-12s $%-6d\n", interior.name, interior.priceDelta));
        }
        if (spoiler) {
            sb.append(String.format("%-22s $%-6d\n", "Rear Spoiler", 800));
        }
        if (stripes) {
            sb.append(String.format("%-22s $%-6d\n", "Racing Stripes", 500));
        }
        for (CarPackage p : packages) {
            sb.append(String.format("Pkg: %-17s $%-6d\n", p.name, p.priceDelta));
        }
        sb.append("-----------------------------------\n");
        sb.append(String.format("Plate Text: %s\n", plateText));
        sb.append(String.format("Max Speed: %d mph\n", maxSpeed()));
        sb.append("-----------------------------------\n");
        sb.append(String.format("TOTAL PRICE:            $%-6d\n", calculateTotalPrice()));
        sb.append("===================================\n");
        return sb.toString();
    }

 
    public CarConfiguration cloneSnapshot() {
        CarConfiguration copy = new CarConfiguration();
        copy.model = this.model;
        copy.color = this.color;
        copy.wheels = this.wheels;
        copy.rimFinish = this.rimFinish;
        copy.tint = this.tint;
        copy.interior = this.interior;
        copy.spoiler = this.spoiler;
        copy.stripes = this.stripes;
        copy.plateText = this.plateText;
        copy.packages = new ArrayList<>(this.packages);
        return copy;
    }
}


//BAses for the earlier 
class CarModel {
    String name;
    int basePrice;
    int baseMaxSpeed;

    public CarModel(String name, int basePrice, int baseMaxSpeed) {
        this.name = name;
        this.basePrice = basePrice;
        this.baseMaxSpeed = baseMaxSpeed;
    }

    public boolean isWheelCompatible(CarWheel wheel) {
        if (this.name.contains("Sports Car") && wheel.name.contains("Off-Road")) {
            return false;
        }
        return true;
    }
}

class CarWheel {
    String name;
    int priceDelta;

    public CarWheel(String name, int priceDelta) {
        this.name = name;
        this.priceDelta = priceDelta;
    }
}

class Part1Stubs {
    public static final CarModel[] AVAILABLE_MODELS = {
        new CarModel("Base Sedan", 25000, 120),
        new CarModel("Sports Coupe", 42000, 160)
    };

    public static final CarWheel[] AVAILABLE_WHEELS = {
        new CarWheel("Standard 17-inch", 0),
        new CarWheel("Off-Road 20-inch", 1200)
    };
}

class CarRenderer {
    public static void renderScene(Graphics g, CarModel model, CarWheel wheels, Color rimColor,
                                   Color bodyColor, WindowTint tint, boolean spoiler, boolean stripes,
                                   double wheelRotation, int cx, int cy, int totalWidth) {
        
        g.setColor(bodyColor != null ? bodyColor : Color.GRAY);
        g.fillRect(cx - 150, cy - 50, 300, 80);

        g.setColor(rimColor != null ? rimColor : Color.BLACK);
        g.fillOval(cx - 110, cy + 20, 40, 40);
        g.fillOval(cx + 70, cy + 20, 40, 40);

        g.setColor(Color.WHITE);
        g.drawString("Car Visual Preview (" + (model != null ? model.name : "") + ")", cx - 90, cy - 10);
    }
}

class DrivePanelStub extends JPanel {
    public DrivePanelStub() {
        setBackground(new Color(40, 40, 50));
        JLabel label = new JLabel("DRIVE MODE (Part 3 Simulation Window)");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(label);
    }

    public void startDriving(CarConfiguration config) {
        System.out.println("Part 3 Hook: Driving started with " + config.model.name + " @ Top Speed " + config.maxSpeed() + " mph"); 
        }

    public void stopDriving() {
        System.out.println("Part 3 Hook: Stopped driving.");
    }
}
