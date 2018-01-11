/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jlwan.livex;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;

/**
 *
 * @author unm2000
 */
public class Livex extends JFrame {

    //key-line value-url
    private Map<String, List<String>> mapHistory = new LinkedHashMap<String, List<String>>();
    private JSpinner spMin;
    private JSpinner spMax;
    private JSpinner spThread;
    private JTextField txtLine;
    private JTextArea txtMsg;
    private JLabel lblProgress;
    private JButton btnSearch;

    public Livex() {
        setTitle("Livex");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        txtMsg = new JTextArea();
        txtMsg.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int offset = txtMsg.viewToModel(e.getPoint());

                try {
                    int rowStart = Utilities.getRowStart(txtMsg, offset);
                    int rowEnd = Utilities.getRowEnd(txtMsg, offset);
                    txtMsg.setSelectionEnd(rowEnd);
                    txtMsg.setSelectionStart(rowStart);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    StringSelection selection = new StringSelection(txtMsg.getSelectedText());
                    clipboard.setContents(selection, null);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });
        setLayout(new BorderLayout());
        add(createTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(txtMsg), BorderLayout.CENTER);


        getRootPane().registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        //getRootPane().setDefaultButton(btnSearch);

    }

    public void showFrame() {
        setPreferredSize(new Dimension(780, 580));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                txtLine.requestFocus();
                txtLine.requestFocusInWindow();
            }
        });

    }

    private JPanel createTopPanel() {
        spMin = new JSpinner(new SpinnerNumberModel(230, 0, 999, 1));
        spMax = new JSpinner(new SpinnerNumberModel(319, 0, 999, 1));
        spThread = new JSpinner(new SpinnerNumberModel(10, 0, 50, 1));
        txtLine = new JTextField();
        lblProgress = new JLabel();


        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(createParmPanel("Min", spMin));
        panel.add(createParmPanel("Max", spMax));
        panel.add(createParmPanel("Line", txtLine));
        panel.add(createParmPanel("Thread", spThread));


        btnSearch = new JButton("Search");
        btnSearch.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doSearch();

            }
        });
        final JButton btnHistory = new JButton("History");
        btnHistory.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (mapHistory.isEmpty()) {
                    appendLn("History is Empty");
                    return;
                }
                for (Map.Entry<String, List<String>> entry : mapHistory.entrySet()) {
                    appendLn(entry.getKey());
                    for (String url : entry.getValue()) {
                        appendLn(url);
                    }
                }
            }
        });
        final JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mapHistory.clear();
                txtMsg.setText("");
            }
        });

        JPanel panButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panButton.add(btnSearch);
        panButton.add(btnHistory);
        panButton.add(btnClear);
        panButton.add(lblProgress);


        JPanel panTop = new JPanel(new BorderLayout());
        panTop.add(panel, BorderLayout.CENTER);
        panTop.add(panButton, BorderLayout.SOUTH);
        return panTop;
    }

    private JPanel createParmPanel(String title, Container com) {
        JLabel lbl = new JLabel(title + ":");
        lbl.setPreferredSize(new Dimension(55, 23));
        com.setPreferredSize(new Dimension(180, 23));
        JPanel pan = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
        pan.add(lbl);
        pan.add(com);
        return pan;
    }

    private void doSearch() throws HeadlessException {
        if (txtLine.getText().isEmpty()) {
            JOptionPane.showMessageDialog(Livex.this, "Line is empty");
            return;
        }
        appendLn("Begin");
        List<String> lst = mapHistory.get(txtLine.getText());
        if (lst != null) {
            appendLn("These urls will be omitted:");
            for (String url : lst) {
                appendLn(url);
            }
        }
        final long start = System.currentTimeMillis();
        btnSearch.setEnabled(false);
        IMsgHandler handler = new IMsgHandler() {

            java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");

            @Override
            public void print(String msg) {
                if (btnSearch.isEnabled()) {
                    return;
                }
                appendLn(msg);
            }

            @Override
            public void onSuccess(String url) {
                long taken = System.currentTimeMillis() - start;
                appendLn("[Success], " + df.format(taken / 1000.0) + " seconds taken");
                appendLn(url);
                lblProgress.setText("");
                btnSearch.setEnabled(true);
                //启用系统默认浏览器来打开网址。  
                try {
                    URI uri = new URI(url);
                    Desktop.getDesktop().browse(uri);
                } catch (URISyntaxException e) {
                    txtMsg.append(e.getMessage());
                } catch (IOException e) {
                    txtMsg.append(e.getMessage());
                }
                List<String> lst = mapHistory.get(txtLine.getText());
                if (lst == null) {
                    lst = new ArrayList<String>();
                    mapHistory.put(txtLine.getText(), lst);
                }
                lst.add(url);
            }

            @Override
            public void onFailed() {
                long taken = System.currentTimeMillis() - start;
                appendLn("[Failed], " + df.format(taken / 1000.0) + " seconds taken");
                lblProgress.setText("");
                btnSearch.setEnabled(true);
            }

            @Override
            public void onProgress(int done, int all) {
                if (btnSearch.isEnabled()) {
                    return;
                }
                lblProgress.setText(done + "/" + all);
            }
        };
        HttpTester httpTester = new HttpTester((Integer) spMin.getValue(), (Integer) spMax.getValue(),
                txtLine.getText(), (Integer) spThread.getValue(), handler);
        httpTester.test(lst);
        return;
    }

    private void appendLn(final String msg) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    appendLn(msg);
                }
            });
            return;
        }

        txtMsg.append(msg + "\n");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE);


                new Livex().showFrame();
            }
        });

    }
}
