import javax.swing.*;
import java.awt.*;
public class drivePanel extends JPanel {
    public drivePanel(){
        setLayout(null);
    }

    @Override 
    protected void paintComponent(Graphics g0){
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int groundY = (int)(h*0.65);
        g.setPaint(new GradientPaint(0, 0, new Color(70, 130, 200),
                0, groundY, new Color(190, 225, 250)));
        g.fillRect(0, 0, w, groundY);

        // Grass: everything below the horizon
        g.setColor(new Color(60, 150, 70));
        g.fillRect(0, groundY, w, h - groundY);

        // Road: a dark gray band
        g.setColor(new Color(60, 60, 65));
        g.fillRect(0, groundY - 20, w, 100);


    }



    
    
}
