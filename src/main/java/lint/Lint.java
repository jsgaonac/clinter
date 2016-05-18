package lint;

import listeners.CodeStyleListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import antlr4.CLexer;
import antlr4.CParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Created by JuanSebastian on 18/05/2016.
 */
public class Lint
{
    public static void main(String[] args) throws Exception
    {
        if (args.length < 1)
        {
            System.out.println("Debe proporcionar un archivo para ser analizado.");
        }

        String filename = args[0];
        ANTLRFileStream input = new ANTLRFileStream(filename);

        CLexer lexer = new CLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        CParser parser = new CParser(tokens);

        ParseTree tree = parser.compilationUnit();
        ParseTreeWalker walker = new ParseTreeWalker();

        CodeStyleListener codeStyleListener = new CodeStyleListener(parser);
        walker.walk(codeStyleListener, tree);
    }
}
