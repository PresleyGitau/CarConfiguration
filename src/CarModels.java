import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

class WindowTint {
    final String name;
    final Color tintColor;
    final int alpha;

    WindowTint(String name, Color tintColor, int alpha) {
        this.name = name;
        this.tintColor = tintColor;
        this.alpha = alpha;
    }
}


abstract class CarModel {
    final String name;
    final int basePrice;
    final int baseMaxSpeed;

    double rearX, frontX;            // body horizontal extent
    double rockerY, beltY, roofY;    // bottom of body, beltline, top of roof
    double hoodY, hoodTipY;          // hood deck, nose tip
    double trunkY, trunkTipY;        // trunk deck, tail tip
    double roofRearX, roofFrontX;    // roof span
    double windshieldX, rearWinX;    // glass base points
    double archR, archH;             // wheel arch shape

    static final double WHEEL_REAR_X = -110;
    static final double WHEEL_FRONT_X = 90;
    static final double GROUND_OFF = 49;

    CarModel(String name, int basePrice, int baseMaxSpeed) {
        this.name = name;
        this.basePrice = basePrice;
        this.baseMaxSpeed = baseMaxSpeed;
    }

    abstract void drawDetails(Graphics2D g, int cx, int cy, Color paint,
                              boolean spoiler, boolean stripes);

    boolean isWheelCompatible(WheelType w) { return true; }

    Path2D buildSilhouette(int cx, int cy) {
        double rX = cx + rearX, fX = cx + frontX;
        double rkY = cy + rockerY, bltY = cy + beltY, rfY = cy + roofY;
        double hY = cy + hoodY, htY = cy + hoodTipY;
        double tY = cy + trunkY, ttY = cy + trunkTipY;
        double rrX = cx + roofRearX, rfX = cx + roofFrontX;
        double wsX = cx + windshieldX, rwX = cx + rearWinX;

        Path2D.Double p = new Path2D.Double();
        p.moveTo(rX, rkY);

        // Rear bumper rises into the tail.
        p.curveTo(rX + 2, rkY - 14, rX + 8, ttY + 6, rX + 20, ttY);
        // Trunk deck to base of C-pillar.
        p.lineTo(rwX, tY);
        // C-pillar / rear window (near-straight, small bow).
        p.curveTo(rwX + 5, tY - 6, rrX - 15, rfY + 12, rrX, rfY);
        // Roof with a shallow crown.
        p.curveTo(rrX + 25, rfY - 2, rfX - 25, rfY - 2, rfX, rfY);
        // A-pillar / windshield.
        p.curveTo(rfX + 18, rfY + 12, wsX - 12, bltY - 4, wsX, bltY);
        // Hood with a slight crown.
        p.curveTo(wsX + 35, hY - 2, fX - 35, htY - 2, fX - 12, htY);
        // Nose / front bumper.
        p.curveTo(fX + 4, htY + 6, fX + 4, rkY - 20, fX - 4, rkY);

        // Bottom: wheel arches, drawn right-to-left.
        arch(p, cx + WHEEL_FRONT_X, rkY, archR, archH);
        p.lineTo(cx + WHEEL_REAR_X + archR, rkY);
        arch(p, cx + WHEEL_REAR_X, rkY, archR, archH);
        p.lineTo(rX, rkY);
        p.closePath();
        return p;
    }

    List<Shape> buildWindows(int cx, int cy, Shape body) {
        final double GLASS_INSET = 6;

        double bltY = cy + beltY + 3;
        double topY = cy + roofY - 10;          // over-shoot: clipped by interior
        double rwX  = cx + rearWinX;
        double wsX  = cx + windshieldX;

        Path2D.Double band = new Path2D.Double();
        band.moveTo(rwX, bltY);
        band.lineTo(rwX, topY);
        band.lineTo(wsX, topY);
        band.lineTo(wsX, bltY);
        band.closePath();

        Area interior = new Area(body);
        BasicStroke edge = new BasicStroke(
            (float) (GLASS_INSET * 2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        interior.subtract(new Area(edge.createStrokedShape(body)));

        Area glass = new Area(band);
        glass.intersect(interior);

        List<Shape> out = new ArrayList<>();
        if (!glass.isEmpty()) out.add(glass);
        return out;
    }

    final void drawBody(Graphics2D g, int cx, int cy, Color paint,
                        WindowTint tint, boolean spoiler, boolean stripes) {
        Path2D body = buildSilhouette(cx, cy);

        double topY = cy + roofY, botY = cy + rockerY;
        g.setPaint(new LinearGradientPaint(
            new Point2D.Double(cx, topY), new Point2D.Double(cx, botY),
            new float[]{0f, 0.4f, 0.8f, 1f},
            new Color[]{ lighten(paint, 55), paint, paint, darken(paint, 55) }));
        g.fill(body);

        g.setColor(darken(paint, 70));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(body);

        // Roof highlight.
        g.setColor(new Color(255, 255, 255, 55));
        g.setStroke(new BasicStroke(2.0f));
        g.draw(new Line2D.Double(cx + roofRearX + 10, cy + roofY + 4,
                                 cx + roofFrontX - 10, cy + roofY + 4));

        drawGlass(g, buildWindows(cx, cy, body), tint);

        drawDetails(g, cx, cy, paint, spoiler, stripes);
    }

    // ---- shared helpers ----
    protected static Color lighten(Color c, int amt) {
        return new Color(clamp(c.getRed() + amt), clamp(c.getGreen() + amt),
                         clamp(c.getBlue() + amt));
    }
    protected static Color darken(Color c, int amt) { return lighten(c, -amt); }
    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    private void drawGlass(Graphics2D g, List<Shape> windows, WindowTint tint) {
        for (Shape s : windows) {
            Rectangle2D b = s.getBounds2D();
            if (b.isEmpty()) continue;

            RadialGradientPaint gp = new RadialGradientPaint(
                new Point2D.Double(b.getX() + b.getWidth() * 0.3, b.getY()),
                (float) Math.max(b.getWidth(), b.getHeight()),
                new float[]{0f, 0.6f, 1f},
                new Color[]{
                    new Color(255, 255, 255, Math.min(255, tint.alpha / 2)),
                    tint.tintColor,
                    new Color(tint.tintColor.getRed(), tint.tintColor.getGreen(),
                              tint.tintColor.getBlue(), tint.alpha)
                });

            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tint.alpha / 255f));
            g.setPaint(gp);
            g.fill(s);
            g.setComposite(old);

            g.setColor(new Color(40, 44, 52));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(s);
        }
    }

    protected void drawLight(Graphics2D g, double x, double y, double w, double h, Color core) {
        g.setPaint(new RadialGradientPaint(
            new Point2D.Double(x + w * 0.35, y + h * 0.35),
            (float) Math.max(w, h),
            new float[]{0f, 1f},
            new Color[]{ lighten(core, 80), darken(core, 20) }));
        g.fill(new Ellipse2D.Double(x, y, w, h));
        g.setColor(new Color(230, 230, 235));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Ellipse2D.Double(x, y, w, h));
    }

    protected void drawMirror(Graphics2D g, double x, double y, Color paint) {
        g.setColor(darken(paint, 30));
        g.fill(new RoundRectangle2D.Double(x, y, 12, 7, 4, 4));
        g.setColor(new Color(180, 200, 215, 160));
        g.fill(new RoundRectangle2D.Double(x + 2, y + 1, 6, 3, 2, 2));
    }

    // Racing stripes.
    protected void drawStripes(Graphics2D g, double rearX, double frontX, double y) {
        g.setColor(new Color(255, 255, 255, 225));
        g.fill(new RoundRectangle2D.Double(rearX, y, frontX - rearX, 6, 3, 3));
        g.fill(new RoundRectangle2D.Double(rearX, y + 10, frontX - rearX, 6, 3, 3));
    }

    protected void drawSpoiler(Graphics2D g, double x, double baseY) {
        g.setColor(new Color(24, 24, 26));
        g.fill(new RoundRectangle2D.Double(x, baseY - 18, 6, 22, 2, 2));
        g.fill(new RoundRectangle2D.Double(x - 22, baseY - 24, 30, 8, 3, 3));
    }

    protected void drawGrille(Graphics2D g, double x, double y, double w, double h) {
        g.setColor(new Color(20, 20, 22));
        int lines = 4;
        for (int i = 0; i < lines; i++) {
            double ly = y + (h / lines) * i;
            g.fill(new RoundRectangle2D.Double(x, ly, w, h / lines - 1.5, 1, 1));
        }
    }

    protected void arch(Path2D p, double wheelCx, double rockerY, double archR, double archH) {
        p.lineTo(wheelCx + archR, rockerY);
        p.curveTo(wheelCx + archR * 0.55, rockerY - archH,
                  wheelCx - archR * 0.55, rockerY - archH,
                  wheelCx - archR, rockerY);
    }
}


class Sedan extends CarModel {
    Sedan() {
        super("Sedan", 24000, 110);
        rearX = -175; frontX = 180;
        rockerY = 20; beltY = -22; roofY = -62;
        hoodY = -20; hoodTipY = -12;
        trunkY = -18; trunkTipY = -8;
        roofRearX = -55; roofFrontX = 15;
        windshieldX = 55; rearWinX = -100;
        archR = 22; archH = 26;
    }

    void drawDetails(Graphics2D g, int cx, int cy, Color paint,
                     boolean spoiler, boolean stripes) {
        drawMirror(g, cx + 40, cy + beltY - 6, paint);
        drawLight(g, cx + 148, cy - 12, 18, 13, new Color(255, 244, 190));
        drawLight(g, cx - 172, cy - 8, 13, 11, new Color(205, 30, 30));
        drawGrille(g, cx + 130, cy - 4, 24, 16);

        Shape old = g.getClip();
        g.clip(buildSilhouette(cx, cy));
        g.setColor(darken(paint, 60));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Line2D.Double(cx - 5, cy + beltY + 4, cx - 5, cy + rockerY - 2));
        if (stripes) drawStripes(g, cx - 145, cx + 130, cy - 14);
        g.setClip(old);

        if (spoiler) drawSpoiler(g, cx - 155, cy + trunkTipY);
    }
}


class SUV extends CarModel {
    SUV() {
        super("SUV", 32000, 100);
        rearX = -180; frontX = 180;
        rockerY = 26; beltY = -30; roofY = -74;
        hoodY = -30; hoodTipY = -22;
        trunkY = -38; trunkTipY = -36;
        roofRearX = -80; roofFrontX = 30;
        windshieldX = 60; rearWinX = -115;
        archR = 24; archH = 30;
    }

    void drawDetails(Graphics2D g, int cx, int cy, Color paint,
                     boolean spoiler, boolean stripes) {
        drawMirror(g, cx + 44, cy + beltY - 6, paint);
        drawLight(g, cx + 155, cy - 10, 17, 14, new Color(255, 244, 190));
        drawLight(g, cx - 178, cy - 6, 13, 12, new Color(205, 30, 30));
        drawGrille(g, cx + 136, cy - 2, 26, 18);

        Shape old = g.getClip();
        g.clip(buildSilhouette(cx, cy));
        g.setColor(darken(paint, 60));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Line2D.Double(cx - 10, cy + beltY + 4, cx - 10, cy + rockerY - 2));
        g.draw(new Line2D.Double(cx + 40, cy + beltY + 4, cx + 40, cy + rockerY - 2));
        if (stripes) drawStripes(g, cx - 150, cx + 150, cy - 20);
        g.setClip(old);

        // Roof spoiler sits on the rear edge of the roof.
        if (spoiler) drawSpoiler(g, cx - 75, cy + roofY);
    }
}

class SportsCar extends CarModel {
    SportsCar() {
        super("Sports Car", 45000, 150);
        rearX = -160; frontX = 185;
        rockerY = 18; beltY = -22; roofY = -46;
        hoodY = -18; hoodTipY = -8;
        trunkY = -14; trunkTipY = -2;
        roofRearX = -40; roofFrontX = 10;
        windshieldX = 70; rearWinX = -85;
        archR = 24; archH = 22;
    }

    void drawDetails(Graphics2D g, int cx, int cy, Color paint,
                     boolean spoiler, boolean stripes) {
        drawMirror(g, cx + 55, cy + beltY - 6, paint);
        drawLight(g, cx + 170, cy - 14, 17, 10, new Color(255, 244, 190));
        drawLight(g, cx - 160, cy - 8, 15, 8, new Color(205, 30, 30));
        drawGrille(g, cx + 150, cy - 4, 22, 12);

        Shape old = g.getClip();
        g.clip(buildSilhouette(cx, cy));
        g.setColor(new Color(28, 28, 30));
        g.fillRect(cx - 4, cy + 14, 18, 5); // exhaust hint
        g.setColor(darken(paint, 60));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Line2D.Double(cx - 25, cy + beltY + 4, cx - 25, cy + rockerY - 2));
        if (stripes) drawStripes(g, cx - 125, cx + 145, cy - 6);
        g.setClip(old);

        // Sports cars always have a rear deck spoiler.
        drawSpoiler(g, cx - 130, cy + trunkTipY);
    }

    boolean isWheelCompatible(WheelType w) {
        return !(w instanceof OffRoadWheels);
    }
}

abstract class WheelType {
    final String name;
    final int priceDelta;
    final int diameter;

    WheelType(String name, int priceDelta, int diameter) {
        this.name = name;
        this.priceDelta = priceDelta;
        this.diameter = diameter;
    }

    void drawWheels(Graphics2D g, int cx, int cy, Color rimColor, double rotationDeg) {
        int groundY = cy + (int) CarModel.GROUND_OFF;
        drawSingleWheel(g, cx + (int) CarModel.WHEEL_REAR_X,  groundY - diameter, rimColor, rotationDeg);
        drawSingleWheel(g, cx + (int) CarModel.WHEEL_FRONT_X, groundY - diameter, rimColor, rotationDeg);
    }

    protected void drawSingleWheel(Graphics2D g, int x, int y,
                                   Color rimColor, double rotationDeg) {
        int r = diameter;
        int cx = x, cy = y + r / 2;

        RadialGradientPaint tirePaint = new RadialGradientPaint(
            new Point2D.Float(cx, cy), r / 2f,
            new float[]{0f, 0.85f, 1f},
            new Color[]{ new Color(60, 60, 62), new Color(15, 15, 16), Color.BLACK });
        g.setPaint(tirePaint);
        g.fill(new Ellipse2D.Double(x - r / 2.0, y, r, r));

        int rimR = r - r / 3;
        RadialGradientPaint rimPaint = new RadialGradientPaint(
            new Point2D.Float(cx - rimR * 0.15f, cy - rimR * 0.15f), rimR / 1.4f,
            new float[]{0f, 0.7f, 1f},
            new Color[]{ CarModel.lighten(rimColor, 90), rimColor, CarModel.darken(rimColor, 40) });
        g.setPaint(rimPaint);
        g.fill(new Ellipse2D.Double(cx - rimR / 2.0, cy - rimR / 2.0, rimR, rimR));

        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.rotate(Math.toRadians(rotationDeg));
        g.setColor(CarModel.darken(rimColor, 60));
        int spokeLen = rimR / 2 - 2;
        for (int i = 0; i < 5; i++) {
            g.rotate(Math.toRadians(72));
            g.fillRect(-2, -spokeLen, 4, spokeLen);
        }
        g.setTransform(old);

        g.setColor(new Color(30, 30, 32));
        g.fill(new Ellipse2D.Double(cx - 4, cy - 4, 8, 8));
    }
}

class StandardWheels extends WheelType {
    StandardWheels() { super("Standard", 0, 44); }
}

class SportWheels extends WheelType {
    SportWheels() { super("Sport", 1200, 42); }
    protected void drawSingleWheel(Graphics2D g, int x, int y,
                                   Color rimColor, double rotationDeg) {
        super.drawSingleWheel(g, x, y, rimColor, rotationDeg);
        g.setColor(new Color(205, 30, 30));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Ellipse2D.Double(x - diameter / 2.0, y, diameter, diameter));
    }
}

class OffRoadWheels extends WheelType {
    OffRoadWheels() { super("Off-Road", 1800, 52); }
    protected void drawSingleWheel(Graphics2D g, int x, int y,
                                   Color rimColor, double rotationDeg) {
        super.drawSingleWheel(g, x, y, rimColor, rotationDeg);
        int cx = x, cy = y + diameter / 2;
        g.setColor(new Color(10, 10, 10));
        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.rotate(Math.toRadians(rotationDeg));
        for (int i = 0; i < 8; i++) {
            g.rotate(Math.toRadians(45));
            g.fillRect(-1, -diameter / 2 + 2, 2, 5);
        }
        g.setTransform(old);
    }
}

class CarRenderer {
    static void renderScene(Graphics2D g, CarModel model, WheelType wheels, Color rimColor,
                            Color paint, WindowTint tint, boolean spoiler, boolean stripes,
                            double wheelRotation, int cx, int cy, int panelWidth) {
        int groundY = cy + (int) CarModel.GROUND_OFF;
        drawShadow(g, cx, groundY);
        drawReflection(g, model, wheels, rimColor, paint, tint, spoiler, stripes,
                       wheelRotation, cx, cy, groundY, panelWidth);
        wheels.drawWheels(g, cx, cy, rimColor, wheelRotation);
        model.drawBody(g, cx, cy, paint, tint, spoiler, stripes);
    }

    private static void drawShadow(Graphics2D g, int cx, int groundY) {
        Paint old = g.getPaint();
        g.setPaint(new RadialGradientPaint(
            new Point2D.Float(cx, groundY + 6), 190f,
            new float[]{0f, 1f},
            new Color[]{ new Color(0, 0, 0, 110), new Color(0, 0, 0, 0) }));
        g.fill(new Ellipse2D.Double(cx - 190, groundY - 6, 380, 28));
        g.setPaint(old);
    }

    private static void drawReflection(Graphics2D g, CarModel model, WheelType wheels, Color rimColor,
                                        Color paint, WindowTint tint, boolean spoiler, boolean stripes,
                                        double wheelRotation, int cx, int cy, int groundY, int panelWidth) {
        int imgW = panelWidth, imgH = 90;
        BufferedImage buf = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bg = buf.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Center car in the buffer and flip vertically so its ground maps to buf Y=0.
        bg.translate(imgW / 2.0 - cx, groundY);
        bg.scale(1, -1);
        wheels.drawWheels(bg, cx, cy, rimColor, wheelRotation);
        model.drawBody(bg, cx, cy, paint, tint, spoiler, stripes);
        bg.dispose();

        BufferedImage mask = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D mg = mask.createGraphics();
        mg.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 110),
                                      0, 70, new Color(255, 255, 255, 0)));
        mg.fillRect(0, 0, imgW, imgH);
        mg.dispose();

        Graphics2D bufG = buf.createGraphics();
        bufG.setComposite(AlphaComposite.DstIn);
        bufG.drawImage(mask, 0, 0, null);
        bufG.dispose();

        g.drawImage(buf, cx - imgW / 2, groundY, null);
    }
}


public class CarModels extends JPanel {
    private final CarModel[] models = { new Sedan(), new SUV(), new SportsCar() };
    private final WheelType[] wheelTypes = { new StandardWheels(), new SportWheels(), new OffRoadWheels() };
    private final WindowTint tint = new WindowTint("Light", new Color(90, 100, 110), 140);
    private double rotation = 0;

    CarModels() {
        setBackground(new Color(210, 220, 232));
        new Timer(30, e -> { rotation = (rotation + 2) % 360; repaint(); }).start();
    }

    @Override
    protected void paintComponent(Graphics gRaw) {
        super.paintComponent(gRaw);
        Graphics2D g = (Graphics2D) gRaw;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cols = models.length, rows = wheelTypes.length;
        int cellW = getWidth() / cols, cellH = getHeight() / rows;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int cx = c * cellW + cellW / 2;
                int cy = r * cellH + cellH / 2;

                CarRenderer.renderScene(g, models[c], wheelTypes[r], new Color(90, 95, 104),
                        new Color(38, 92, 182), tint, true, true, rotation, cx, cy, cellW);

                g.setColor(Color.DARK_GRAY);
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g.drawString(models[c].name + " / " + wheelTypes[r].name,
                             c * cellW + 10, r * cellH + 16);
            }
        }
    }
}