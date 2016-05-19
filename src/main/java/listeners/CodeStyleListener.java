package listeners;

/**
 * Created by JuanSebastian on 18/05/2016.
 */

import antlr4.CBaseListener;
import antlr4.CParser;
import org.antlr.v4.runtime.TokenStream;
import util.StringAnalysis;

import java.util.ArrayList;

public class CodeStyleListener extends CBaseListener
{
    private CParser parser;

    private boolean isInFunctionDecl;
    private boolean isInParameterDecl;

    private ArrayList<Integer> funcSpecList;

    public CodeStyleListener(CParser parser)
    {
        this.parser = parser;
        funcSpecList = new ArrayList<>();
    }

    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx)
    {
        isInFunctionDecl = true;
        funcSpecList.clear();
    }

    @Override
    public void exitFunctionDefinition(CParser.FunctionDefinitionContext ctx)
    {
        isInFunctionDecl = false;
    }

    @Override
    public void enterParameterDeclaration(CParser.ParameterDeclarationContext ctx)
    {
        isInParameterDecl = true;
    }

    @Override
    public void exitParameterDeclaration(CParser.ParameterDeclarationContext ctx)
    {
        isInParameterDecl = false;
    }

    @Override
    public void enterTypeQualifier(CParser.TypeQualifierContext ctx)
    {
        if (isInFunctionDecl)
        {
            funcSpecList.add(ctx.getStart().getLine());
        }
    }

    @Override
    public void enterTypeSpecifier(CParser.TypeSpecifierContext ctx)
    {
        if (isInFunctionDecl)
        {
            funcSpecList.add(ctx.getStart().getLine());
        }
    }

    @Override
    public void enterPointer(CParser.PointerContext ctx)
    {
        if (isInFunctionDecl)
        {
            funcSpecList.add(ctx.getStart().getLine());
        }
    }

    @Override
    public void enterDirectDeclarator(CParser.DirectDeclaratorContext ctx)
    {
        if (isInFunctionDecl)
        {
            if (ctx.Identifier() != null)
            {
                String id = ctx.Identifier().toString();
                int idLine = ctx.Identifier().getSymbol().getLine();
                int idCol = ctx.Identifier().getSymbol().getCharPositionInLine();

                if (!isInParameterDecl)
                {
                    if (funcSpecList.size() > 0)
                    {
                        int lastLine = funcSpecList.get(0);
                        for (int line : funcSpecList)
                        {
                            if (line != lastLine)
                            {
                                System.out.println(
                                        StringAnalysis.getPrintInfo(
                                                line,
                                                0,
                                                "El tipo de retorno de la función debería estar en la misma línea"));
                            }

                            lastLine = line;
                        }

                        if (idLine == lastLine)
                        {
                            System.out.println(
                                    StringAnalysis.getPrintInfo(
                                            idLine,
                                            idCol,
                                            "El identificador (" + id + ") debería estar en la siguiente línea"));
                        }
                    }
                }

                checkIdentifier(id, idLine, idCol);
            }
        }
    }

    @Override
    public void enterTypedefName(CParser.TypedefNameContext ctx)
    {
        String id = ctx.Identifier().toString();

        int line = ctx.Identifier().getSymbol().getLine();
        int col = ctx.Identifier().getSymbol().getCharPositionInLine();

        checkIdentifier(id, line, col);
    }

    private void checkIdentifier(String id, int line, int col)
    {
        String betterID = id.toLowerCase();

        if (!id.equals(betterID))
        {
            betterID = StringAnalysis.getStyledID(id);
            String msg = StringAnalysis.getPrintInfo(
                    line,
                    col,
                    " Identificador: " + id + " -> " + betterID
            );

            System.out.println(msg);
        }
    }

}
