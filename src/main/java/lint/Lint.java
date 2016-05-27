package lint;

import listeners.CodeStyleListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import antlr4.CLexer;
import antlr4.CParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import ui.ConsoleUI;
import ui.GUI;
import ui.UI;
import util.Constants;

import java.io.*;

/**
 * Created by JuanSebastian on 18/05/2016.
 */
public class Lint
{
    public static void main(String[] args)
    {
        Lint linter = new Lint();

        if (args.length == 1)
        {
            if (args[0].equals("--gui")) linter.setUI(Constants.UI.GUI);
            else
            {
                linter.setUI(Constants.UI.CONSOLE);
                linter.run(args[0]);
            }
        }
        else
        {
            System.out.println("Argumentos: [ruta_a_archivo_a_ser_analizado | --gui]");
        }
    }

    public void run(String filename)
    {
        try
        {
            doCheckLineLength(filename);
            doParsing(filename);
            doCheckMemLeaks();
        }
        catch (FileNotFoundException e)
        {
           ui.displayText("Archivo: " + filename + " no encontrado.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setUI(Constants.UI uiType)
    {
        switch (uiType)
        {
            case GUI: ui = new GUI(this); break;
            case CONSOLE: ui = new ConsoleUI(); break;

            default: break;
        }
    }

    private void doCheckMemLeaks()
    {
        codeStyleListener.checkMemoryLeaks();
    }

    private void doCheckLineLength(String filename)
    {
        final int MAX_LINE_LENGTH = 79;

        File file = new File(filename);
        BufferedReader bfr = null;

        try
        {
            bfr = new BufferedReader(new FileReader(file));
            String line = null;
            Integer lineNumber = 1;
            while ((line = bfr.readLine()) != null)
            {
                if (line.length() > MAX_LINE_LENGTH)
                {
                    System.out.println("Línea " + lineNumber.toString() + ": muy larga. MAX: " + MAX_LINE_LENGTH);
                }

                lineNumber++;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (bfr != null) bfr.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void doParsing(String filename) throws Exception
    {
        ANTLRFileStream input = new ANTLRFileStream(filename);

        CLexer lexer = new CLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        CParser parser = new CParser(tokens);

        ParseTree tree = parser.compilationUnit();
        ParseTreeWalker walker = new ParseTreeWalker();

        codeStyleListener = new CodeStyleListener(parser, ui);
        walker.walk(codeStyleListener, tree);
    }

    private CodeStyleListener codeStyleListener;
    private UI ui;

}
