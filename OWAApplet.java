import java.applet.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

public class OWAApplet extends Applet implements ActionListener {

  public void init() {
    Button b = new Button("Show Test Applet");
    b.addActionListener(this);
    add(b);
  }

  public void actionPerformed(ActionEvent ae) {
    OWAFrame f = new OWAFrame();
    f.isApplication = false;
  }

}
