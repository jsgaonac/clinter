package listeners;

/**
 * Created by JuanSebastian on 18/05/2016.
 */

import antlr4.CBaseListener;
import antlr4.CParser;
import util.StringAnalysis;

public class CodeStyleListener extends CBaseListener
{
    private CParser parser;

    public CodeStyleListener(CParser parser)
    {
        this.parser = parser;
    }

    @Override
    public void enterDirectDeclarator(CParser.DirectDeclaratorContext ctx)
    {

    }

    @Override
    public void enterTypedefName(CParser.TypedefNameContext ctx)
    {
        String id = ctx.Identifier().toString();
        String betterID = id.toLowerCase();
        
        if (!id.equals(betterID))
        {
            betterID = StringAnalysis.getStyledID(id);
            System.out.println(
                    "(" +
                    ctx.getStart().getLine() + ", " +
                    ctx.getStart().getCharPositionInLine() +
                    "):" + " Identificador: " + id + " -> " +
                            betterID
            );
        }
    }

}
