package listeners;

/**
 * Created by JuanSebastian on 18/05/2016.
 */

import antlr4.CBaseListener;
import antlr4.CParser;
import util.StringUtilities;

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
        isInFunctionDecl = false;
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
        if (ctx.Identifier() != null)
        {
            String id = ctx.Identifier().toString();
            int idLine = ctx.Identifier().getSymbol().getLine();
            int idCol = ctx.Identifier().getSymbol().getCharPositionInLine();

            if (isInFunctionDecl)
            {
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
                                        StringUtilities.getPrintInfo(
                                                line,
                                                0,
                                                "El tipo de retorno de la función debería estar en la misma línea"));
                            }

                            lastLine = line;
                        }

                        if (idLine == lastLine)
                        {
                            System.out.println(
                                    StringUtilities.getPrintInfo(
                                            idLine,
                                            idCol,
                                            "El identificador (" + id + ") debería estar en la siguiente línea"));
                        }
                    }
                }
            }

            checkIdentifier(id, idLine, idCol);
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

    @Override
    public void enterCompoundStatement(CParser.CompoundStatementContext ctx)
    {
        isInFunctionDecl = false;

        if (ctx.getStart().getCharPositionInLine() > 0)
        {
            System.out.println(
                    StringUtilities.getPrintInfo(
                            ctx.getStart().getLine(),
                            ctx.getStart().getCharPositionInLine(),
                            "El corchete debería estar en la columna 1"
                    )
            );
        }
        if (ctx.getStop().getCharPositionInLine() > 0)
        {
            System.out.println(
                    StringUtilities.getPrintInfo(
                            ctx.getStop().getLine(),
                            ctx.getStop().getCharPositionInLine(),
                            "El corchete debería estar en la columna 1"
                    )
            );
        }

    }

    @Override
    public void enterStructOrUnionSpecifier(CParser.StructOrUnionSpecifierContext ctx)
    {
        String id = ctx.Identifier().toString();

        if (!Character.isUpperCase(id.charAt(0)))
        {
            System.out.println(
                    StringUtilities.getPrintInfo(
                            ctx.Identifier().getSymbol().getLine(),
                            ctx.Identifier().getSymbol().getCharPositionInLine(),
                            "Identificador: " + id + " -> " + StringUtilities.getStyledStructID(id)
                    )
            );
        }
    }

    private void checkIdentifier(String id, int line, int col)
    {
        String betterID = id.toLowerCase();

        if (!id.equals(betterID))
        {
            betterID = StringUtilities.getStyledID(id);
            String msg = StringUtilities.getPrintInfo(
                    line,
                    col,
                    " Identificador: " + id + " -> " + betterID
            );

            System.out.println(msg);
        }
    }

}
