import javax.swing.*;
public class testMain {
   public static void main (String[] args){
    JFrame f = new JFrame("Driving Test");
    f.add(new drivePanel());
    f.setSize(900, 500);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(true);
    
   } 
}
