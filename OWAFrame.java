import java.applet.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;


public class OWAFrame extends Frame implements ActionListener, ItemListener {

  private int n = 0;
  private float[] w = new float[0];
  private float[] x = new float[0];
  private float[] y = new float[0];
  private float[] z = new float[0];

  float orness;
  float tradeoff;
  float dispersion;

  private Choice chRuns;
  private TextField tfRuns;
  private Choice chWeights;
  private TextField tfWeights;
  private TextField tfNumber;
  private TextField tfNew;
  private Choice chQuants;
  private Checkbox cbAlpha;
  private double alpha[];
  private Canvas cnv = null;
  private Panel bottomP;
  private Button btnRun;
  private Button btnSet;
  private Button btnClose;

  public boolean isApplication = true;


  public static void main (String args[]) {
    new OWAFrame();
  }

  public OWAFrame () {
    super("OWA Measures Test by Claus Rinner, ifgi");

    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        hide();
        dispose();
        if (isApplication) System.exit(0);
      }
    });

    makeInterface();
    show();
  }

  /**
  * Constructs the dialog appearance
  */
  protected void makeInterface () {
    setSize(400, 300);
    setLayout(new BorderLayout());

    // panel for control of random runs
    Panel runP = new Panel();
    runP.setLayout(new FlowLayout());

    runP.add(new Label("Perform"));

    chRuns = new Choice();
    chRuns.addItem("1");
    chRuns.addItem("5");
    chRuns.addItem("10");
    chRuns.addItem("25");
    chRuns.addItem("50");
    chRuns.addItem("100");
    chRuns.select(3);
    chRuns.addItemListener(this);
    runP.add(chRuns);

    tfRuns = new TextField(chRuns.getSelectedItem());
    tfRuns.addActionListener(this);
    runP.add(tfRuns);

    runP.add(new Label("runs with"));

    chWeights = new Choice();
    chWeights.addItem("2");
    chWeights.addItem("3");
    chWeights.addItem("5");
    chWeights.addItem("10");
    chWeights.addItem("20");
    chWeights.addItem("40");
    chWeights.addItem("75");
    chWeights.addItem("100");
    chWeights.select(3);
    chWeights.addItemListener(this);
    runP.add(chWeights);

    tfWeights = new TextField(chWeights.getSelectedItem());
    tfWeights.addActionListener(this);
    runP.add(tfWeights);

    runP.add(new Label("random weights"));

    btnRun = new Button("Go!");
    btnRun.addActionListener(this);
    btnRun.setActionCommand("run");
    runP.add(btnRun);

    // panel for control of individual weight
    Panel setP = new Panel();
    setP.setLayout(new FlowLayout());

    setP.add(new Label("Modify weight number"));

    tfNumber = new TextField("0");
    tfNumber.addActionListener(this);
    setP.add(tfNumber);

    setP.add(new Label("New value:"));

    tfNew = new TextField("0.0");
    tfNew.addActionListener(this);
    setP.add(tfNew);

    btnSet = new Button("Set!");
    btnSet.addActionListener(this);
    btnSet.setActionCommand("set");
    setP.add(btnSet);

    // panel for predefined sets of weights
    Panel strategyP = new Panel();
    strategyP.add(new Label("Choose predefined strategy:"));
    Button b = new Button("AND");
    b.addActionListener(this);
    b.setActionCommand("and");
    strategyP.add(b);
    b = new Button("WLC");
    b.addActionListener(this);
    b.setActionCommand("wlc");
    strategyP.add(b);
    b = new Button("OR");
    b.addActionListener(this);
    b.setActionCommand("or");
    strategyP.add(b);

    // panel for linguistic quantifiers
    Panel quantP = new Panel();
    quantP.add(new Label("Linguistic quantifiers: Satisfy "));
    chQuants = new Choice();
    chQuants.addItem("at least one");
    chQuants.addItem("few");
    chQuants.addItem("some");
    chQuants.addItem("half");
    chQuants.addItem("many");
    chQuants.addItem("most");
    chQuants.addItem("all");
    chQuants.select(3);
    chQuants.addItemListener(this);
    quantP.add(chQuants);
    quantP.add(new Label(" of the criteria!"));
    alpha = new double[7]; // must be of size <chQuants.getItemCount()>
    alpha[0] = 0.0;
    alpha[1] = 0.1;
    alpha[2] = 0.5;
    alpha[3] = 1.0;
    alpha[4] = 2.0;
    alpha[5] = 10.0;
    alpha[6] = Double.POSITIVE_INFINITY;
    cbAlpha = new Checkbox("maximize tradeoff");
    quantP.add(cbAlpha);

    // add control panel to application window
    Panel controlPanel = new Panel(new GridLayout(4,1));
    controlPanel.add(runP);
    controlPanel.add(setP);
    controlPanel.add(strategyP);
    controlPanel.add(quantP);
    this.add(controlPanel, "North");

    // add triangle panel to application window
    Panel mainP = new Panel();
    mainP.add(this.makeTrianglePanel());
    this.add(mainP, "Center");

    // add button panel to application window
    Panel pnlButtons = new Panel();
    btnClose = new Button("Close");
    btnClose.addActionListener(this);
    btnClose.setActionCommand("close");
    pnlButtons.add(btnClose);
    this.add(pnlButtons, "South");

    //
    doLayout();
    pack();

    doRun();
  }


  protected Panel makeTrianglePanel () {
    Panel pnlTriangle = new Panel();
    pnlTriangle.setLayout(new BorderLayout());

    // add a graphical representation of tradeoff (dispersion) vs. risk (orness)
    Panel pnlImage = new Panel();
//    Image imgTriangle = Toolkit.getDefaultToolkit().getImage("owa-triangle-sectors1.gif");
//    pnlTriangle.add(new ImageCanvas(imgTriangle));
    cnv = new Canvas() {
      public Dimension getMinimumSize() {
        return new Dimension(320,240);
      }
      public Dimension getPreferredSize() {
        return new Dimension(480,320);
      }
      public void paint(Graphics g) {
        int ww = this.getSize().width;
        int h = this.getSize().height;

        g.drawLine(3,h-5, ww-5,h-5); // risk axis
        g.drawLine(5,h-3, 5,5); // tradeoff axis
        g.drawString("0.5", ww/2-5,h-10);
//        g.drawString("Risk", ww-40,h-10);
        g.drawString("0.5", 10,h/2+5);

        g.drawLine(3,h/2, 7,h/2);     // x=0.5
        g.drawLine(3,5, 7,5);
        g.drawLine(ww/2,h-3, ww/2,h-7); // y=0.5
        g.drawLine(ww-5,h-3, ww-5,h-7);

        g.setColor(Color.darkGray);
        g.drawLine(5,h-5, ww/2,5);
        g.drawLine(ww/2,5, ww-5,h-5);
        g.drawLine(ww-5,h-5, 5,h-5);

/*
        g.setColor(Color.blue);
        g.drawString("Weights", 10,15);
        g.setColor(Color.red);
        g.drawString("Tradeoff", 10,30);
        g.setColor(Color.green);
        g.drawString("Disp.", 10,45);
*/

        // display weights of last run, and Q fuzzy membership function
        float lastw = 0.0f;
        float lastq = 0.0f;
        float sum = 0.0f; // just to check
        for (int i=0; i<w.length; i++) {
          sum += w[i];
//System.out.print(w[i] + " | ");

          // paint quantifier membership function (q[i] = sum(w[j]), j=1..i)
          g.setColor(Color.gray);
          g.fillOval((int) (5 + 0.01*i*(ww-10)) - 1, (int) (h-5 - sum*(h-10)) - 1, 3, 3);
          if (i>0) {
            g.drawLine((int) (5 + 0.01*(i-1)*(ww-10)) - 1, (int) (h-5 - lastq*(h-10)) , (int) (5 + 0.01*i*(ww-10)) - 1, (int) (h-5 - sum*(h-10)) - 1);
          }
          lastq = sum;

          // paint dots for weights of last run
          g.setColor(Color.blue);
          g.fillOval((int) (5 + 0.01*i*(ww-10)) - 1, (int) (h-5 - w[i]*(h-10)) - 1, 3, 3);
//          if (i>0) {
//            g.drawLine((int) (5 + 0.01*(i-1)*(ww-10)) - 1, (int) (h-5 - lastw*(h-10)) - 1, (int) (5 + 0.01*i*(ww-10)) - 1, (int) (h-5 - w[i]*(h-10)) - 1);
//          }
          lastw = w[i];
        }

System.out.println("\nSum of sample weights = " + sum);

        // paint dots for tradeoff and dispersion
        for (int i=0; i<x.length; i++) {
          g.setColor(Color.red);
          g.fillOval((int) (5 + x[i]*(ww-10)) - 1, (int) (h-5 - y[i]*(h-10)) - 1, 3, 3);
          g.setColor(Color.green);
          g.fillOval((int) (5 + x[i]*(ww-10)) - 1, (int) (h-5 - z[i]*(h-10)) - 1, 3, 3);
        }

        // mark the measures for last run
        g.setColor(Color.gray);
        g.drawOval((int) (5 + x[(x.length-1)]*(ww-10)) - 3, (int) (h-5 - y[(x.length-1)]*(h-10)) - 3, 6, 6);
        g.drawOval((int) (5 + x[(x.length-1)]*(ww-10)) - 3, (int) (h-5 - z[(x.length-1)]*(h-10)) - 3, 6, 6);
      }
    };

    pnlImage.add(cnv);
    pnlTriangle.add("Center", pnlImage);

    Panel pnlLegend = new Panel();
    pnlLegend.setLayout(new BorderLayout());

    Panel pnlColors = new Panel();
    pnlColors.setLayout(new GridLayout(4,1));

    Label l;
    l = new Label("blue");
    l.setForeground(Color.blue);
    pnlColors.add(l);
    l = new Label("gray");
    l.setForeground(Color.gray);
    pnlColors.add(l);
    l = new Label("red");
    l.setForeground(Color.red);
    pnlColors.add(l);
    l = new Label("green");
    l.setForeground(Color.green);
    pnlColors.add(l);
    pnlLegend.add("West", pnlColors);

    Panel pnlLabels = new Panel();
    pnlLabels.setLayout(new GridLayout(4,1));
    pnlLabels.add(new Label("Random weights (by index 0..100) of last run"));
    pnlLabels.add(new Label("Quantifier membership function for last run"));
    pnlLabels.add(new Label("Tradeoffs (by risk/orness 0..1) for all runs"));
    pnlLabels.add(new Label("Dispersions (by risk/orness 0..1) for all runs"));
    pnlLegend.add("Center", pnlLabels);

    pnlTriangle.add("South", pnlLegend);

    return pnlTriangle;
  }


  public void actionPerformed (ActionEvent ae) {
    // event was clicking a button
    if (ae.getSource() instanceof Button) {
      Button b = (Button) ae.getSource();
      if (b.getActionCommand().equalsIgnoreCase("run")) {
        doRun();
      } else if (b.getActionCommand().equalsIgnoreCase("set")) {
        doSet();
      } else if (b.getActionCommand().equalsIgnoreCase("close")) {
        hide();
        dispose();
        if (isApplication) System.exit(0);
      } else if (b.getActionCommand().equalsIgnoreCase("and")) {
        setWeightsToAnd();
      } else if (b.getActionCommand().equalsIgnoreCase("or")) {
        setWeightsToOr();
      } else if (b.getActionCommand().equalsIgnoreCase("wlc")) {
        setWeightsToWLC();
      }
    } else if (ae.getSource() instanceof TextField) {
      if (ae.getSource().equals(tfRuns) || ae.getSource().equals(tfWeights)) {
        doRun();
      } else if (ae.getSource().equals(tfNumber) || ae.getSource().equals(tfNew)) {
        doSet();
      }
    }
  }


  protected void doSet() {
    int i = Integer.valueOf(tfNumber.getText()).intValue();
    if (i < w.length) {

      // set new weight
      w[i] = Float.valueOf(tfNew.getText()).floatValue();

      // adjust remaining weights: build sum without w[i], multiply each by rest/sum
      float sum = 0.0f;
      for (int j=0; j<w.length; j++) {
        if (j != i) sum += w[j];
      }
      for (int k=0; k<w.length; k++) {
        if (k != i) w[k] = w[k] * (1.0f - w[i]) / sum;
      }
/*
      sum = 0.0f;
      for (int l=0; l<w.length; l++) {
        sum += w[l];
      }
      if (sum != 1.0f) System.out.println("ERROR: sum of weights == " + sum);
*/

      // update & repaint
      x = new float[1];
      y = new float[1];
      z = new float[1];

      updateMeasures();

      x[0] = orness;
      y[0] = tradeoff;
      z[0] = dispersion;

      cnv.repaint();
    }
  }


  public void itemStateChanged(ItemEvent ie) {
    if (ie.getSource().equals(chRuns)) {
      tfRuns.setText(chRuns.getSelectedItem());
      doRun();
    } else if (ie.getSource().equals(chWeights)) {
      tfWeights.setText(chWeights.getSelectedItem());
      doRun();
    } else if (ie.getSource().equals(chQuants)) {
      setWeightsToQuant();
    }
  }

  protected void doRun() {
      x = new float[Integer.valueOf(tfRuns.getText()).intValue()];
      y = new float[Integer.valueOf(tfRuns.getText()).intValue()];
      z = new float[Integer.valueOf(tfRuns.getText()).intValue()];
      for (int i=0; i<Integer.valueOf(tfRuns.getText()).intValue(); i++) {
        setWeightsRandomly();
        updateMeasures();
        x[i] = orness;
        y[i] = tradeoff;
        z[i] = dispersion;
      }
      cnv.repaint();
  }

  protected void updateMeasures() {
    int n = w.length;
    orness = 0.0f;
    tradeoff = 0.0f;
    dispersion = 0.0f;
    float v;

    for (int i=0; i<n; i++) {
      v = w[i];
      orness += (n-(i+1)) * v;
      tradeoff += (v - (1.0f/n)) * (v - (1.0f/n));
      if (v > 0.0f) dispersion += v * Math.log(v);
    }

    // Orness according to Yager 1988, p.187
    orness = orness / (n-1.0f);
    // Tradeoff according to Jiang & Eastman 2000, p.179
    tradeoff = 1.0f - (float) Math.sqrt( (n / (n-1.0f)) * tradeoff );
    // Dispersion according to Yager 1988, p.188, normalized
    dispersion = -dispersion / (float) Math.log(n);
  }


  /*
   *  set random order weights
   */
  protected void setWeightsRandomly() {
    n = Integer.valueOf(tfWeights.getText()).intValue();
    w = new float[n];
    float sum = 0.0f;

    // normal distribution:
/*
    for (int i=0; i<n; i++) {
      w[i] = (float) Math.random();
      sum += w[i];
    }
    for (int i=0; i<n; i++) {
      w[i] /= sum;
    }
*/

    // "realistic" distribution:
    int m = 0;  // number of weights that have been set
    while ((sum < 1.0) && (m < n)) {
      int i = 0;  // index of weight to be set
      int j = (int) (Math.random() * (n - m));

      // find the j'th next un-set weight
      for (int k=0; k<j; k++) {
        i++;
        while (w[i] > 0.0) {
          i++;
        }
      }

      w[i] = (float) Math.random() * (1.0f - sum);
      sum += w[i];
      m++;
    }
  }

  /**
   *  Set order weights to (1.0, 0.0, ..., 0.0)
   */
  private void setWeightsToOr() {

    // update n, set all order weights to 0.0, initialize x, y, z
    setWeightsToZero();

    // set first order weight to 1.0
    w[0] = 1.0f;

    // update & repaint
    updateMeasures();

    x[0] = orness;
    y[0] = tradeoff;
    z[0] = dispersion;

    cnv.repaint();
  }

  /**
   *  Set order weights to (1/n, ..., 1/n)
   */
  private void setWeightsToWLC() {

    // update n, set all order weights to 0.0, initialize x, y, z
    setWeightsToZero();

    // set all order weights to 1/n
    for (int i=0; i<n; i++) {
      w[i] = 1.0f/n;
    }

    // update & repaint
    updateMeasures();

    x[0] = orness;
    y[0] = tradeoff;
    z[0] = dispersion;

    cnv.repaint();
  }

  /**
   *  Set order weights to (0.0, ..., 0.0, 1.0)
   */
  private void setWeightsToAnd() {

    // update n, set all order weights to 0.0, initialize x, y, z
    setWeightsToZero();

    // set last order weights to 1.0
    w[n-1] = 1.0f;

    // update & repaint
    updateMeasures();

    x[0] = orness;
    y[0] = tradeoff;
    z[0] = dispersion;

    cnv.repaint();
  }

  /**
   *  Quantifier guided weights setting
   */
  private void setWeightsToQuant() {

    // update n, set all order weights to 0.0, initialize x, y, z
    setWeightsToZero();

    // set one order weight to 1.0 depending on chosen quantifier
    int q = chQuants.getSelectedIndex();
    int qn= chQuants.getItemCount();

    // use quantifier and OWA weights from Malczewski ????, or Yager 1995?
    if (q == 0) {               // same for both
      w[0] = 1.0f;
    } else if (q == (qn-1)) {   // same for both
      w[n-1] = 1.0f;
    } else {
      if (cbAlpha.getState()) { // Malczewski
        for (int i=0; i<n; i++) {
          w[i] = (float) ( Math.pow(((double)(i+1))/n, alpha[q]) - Math.pow(((double)i)/n, alpha[q]) );
        }
      } else {                  // Yager
        for (int i=0; i<n; i++) {
          if ( (((float)(i+1))/n) > (((float)(q+1))/qn) ) {
            w[i] = 1.0f;
            break;
          }
        }
      }
    }

    // update & repaint
    updateMeasures();

    x[0] = orness;
    y[0] = tradeoff;
    z[0] = dispersion;

    cnv.repaint();
  }

  /**
   *  Set order weights temporarily to (0.0, ..., 0.0)
   */
  private void setWeightsToZero() {
    n = Integer.valueOf(tfWeights.getText()).intValue();
    w = new float[n];

    // set all but last order weights to 0.0
    for (int i=0; i<n; i++) {
      w[i] = 0.0f;
    }

    // prepare update
    x = new float[1];
    y = new float[1];
    z = new float[1];
  }

}
