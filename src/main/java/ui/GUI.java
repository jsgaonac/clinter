package ui;

import lint.Lint;
import util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

/**
 * Created by JuanSebastian on 27/05/2016.
 */
public class GUI implements UI, ActionListener
{
    public GUI(Lint linter)
    {
        this.linter = linter;
        initGUI();
    }

    private void initGUI()
    {
        initWindow();
        initTextArea();
        initMenu();
        initFileChooser();

        window.setVisible(true);
    }

    private void initMenu()
    {
        menuBar = new JMenuBar();

        menu = new JMenu(Constants.ARCHIVE_MENU_STR);
        menu.setMnemonic(KeyEvent.VK_A);
        menuBar.add(menu);

        JMenuItem menuItemLoad = new JMenuItem(Constants.LOAD_ITEM_STR, KeyEvent.VK_C);
        JMenuItem menuItemExit = new JMenuItem(Constants.EXIT_ITEM_STR, KeyEvent.VK_S);

        menuItemLoad.addActionListener(this);
        menuItemExit.addActionListener(this);

        menu.add(menuItemLoad);
        menu.add(menuItemExit);

        window.setJMenuBar(menuBar);
    }

    private void initFileChooser()
    {
        fileChooser = new JFileChooser();
        //window.add(fileChooser);
    }

    private void initTextArea()
    {
        textArea = new JTextArea();

        int x = 20;
        int yUpper = 30;
        int yLower = 80;

        Dimension textAreaDim = window.getSize();
        textAreaDim.width = textAreaDim.width - 2 * x;
        textAreaDim.height = textAreaDim.height - yUpper - yLower;

        textArea.setBounds(x - 5, yUpper, textAreaDim.width, textAreaDim.height);
        textArea.setBackground(Color.black);
        textArea.setForeground(Color.white);
        textArea.setVisible(true);

        scrollPaneResults = new JScrollPane(textArea);
        scrollPaneResults.setBounds(x - 5, yUpper, textAreaDim.width, textAreaDim.height);
        scrollPaneResults.setVisible(true);

        window.add(scrollPaneResults);
    }

    private void initWindow()
    {
        window = new JFrame("Clinter");
        window.setSize(640, 480);
        window.setResizable(false);
        window.setLayout(null);
        window.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    @Override
    public void displayText(String txt)
    {
        textArea.append(txt + '\n');
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals(Constants.LOAD_ITEM_STR))
        {
            loadFile();
        }
        else if (e.getActionCommand().equals(Constants.EXIT_ITEM_STR))
        {
            window.dispose();
        }
    }

    private void loadFile()
    {
        int retVal = fileChooser.showOpenDialog(window);

        if (retVal == JFileChooser.APPROVE_OPTION)
        {
            linter.run(fileChooser.getSelectedFile().toString());
        }
    }

    private JFrame window;
    private JTextArea textArea;
    private JFileChooser fileChooser;
    private JScrollPane scrollPaneResults;
    private JMenuBar menuBar;
    private JMenu menu;

    private Lint linter;
}
