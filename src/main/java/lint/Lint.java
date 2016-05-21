package lint;

import listeners.CodeStyleListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import antlr4.CLexer;
import antlr4.CParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.*;

/**
 * Created by JuanSebastian on 18/05/2016.
 */
public class Lint
{
    private CodeStyleListener codeStyleListener;
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println("Debe proporcionar un archivo para ser analizado.");
        }

        String filename = args[0];

        Lint linter = new Lint();
        linter.run(filename);
    }

    private void run(String filename)
    {
        try
        {
            doCheckLineLength(filename);
            doParsing(filename);
            doCheckMemLeaks();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
        catch (FileNotFoundException e)
        {
            System.out.println("Archivo: " + filename + " no encontrado.");
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

        codeStyleListener = new CodeStyleListener(parser);
        walker.walk(codeStyleListener, tree);
    }
}
