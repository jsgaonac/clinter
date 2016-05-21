package listeners;

/**
 * Created by JuanSebastian on 18/05/2016.
 */

import antlr4.CBaseListener;
import antlr4.CParser;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import util.IDInfo;
import util.StringUtilities;

import java.util.ArrayList;

public class CodeStyleListener extends CBaseListener
{
    private CParser parser;

    private boolean isInFunctionDecl;
    private boolean isInParameterDecl;
    private boolean isInVarDeclList;
    private boolean isInDecl;
    private boolean isInAsignExpr;

    private int depth;

    private ArrayList<IDInfo> funcSpecList;
    private ArrayList<IDInfo> varDeclList;
    private ArrayList<IDInfo> varMallocList;
    private ArrayList<IDInfo> varFreeList;

    public CodeStyleListener(CParser parser)
    {
        this.parser = parser;
        funcSpecList = new ArrayList<>();
        varDeclList = new ArrayList<>();
        varMallocList = new ArrayList<>();
        varFreeList = new ArrayList<>();

        isInFunctionDecl = false;
        isInParameterDecl = false;
        isInVarDeclList = false;
        isInDecl = false;
        isInAsignExpr = false;

        depth = -4;
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
        IDInfo idInfo = getTokenInfo(ctx.getStart());

        if (isInFunctionDecl)
        {
            funcSpecList.add(idInfo);
        }
    }

    @Override
    public void enterTypeSpecifier(CParser.TypeSpecifierContext ctx)
    {
        IDInfo idInfo = getTokenInfo(ctx.getStart());

        if (isInFunctionDecl)
        {
            funcSpecList.add(idInfo);
        }
        else if (isInDecl)
        {
            varDeclList.add(idInfo);
        }
    }

    @Override
    public void enterPointer(CParser.PointerContext ctx)
    {
        if (isInFunctionDecl)
        {
            funcSpecList.add(getTokenInfo(ctx.getStart()));
        }
    }

    @Override
    public void enterDirectDeclarator(CParser.DirectDeclaratorContext ctx)
    {
        if (ctx.Identifier() != null)
        {
            IDInfo idInfo = getTokenInfo(ctx.Identifier().getSymbol());

            if (isInFunctionDecl)
            {
                handleFunctionDecl(idInfo);
            }

            if (isInVarDeclList)
            {
                handleDeclList(idInfo);
            }

            checkIdentifier(idInfo);
        }
    }

    @Override
    public void enterTypedefName(CParser.TypedefNameContext ctx)
    {
        IDInfo idInfo = new IDInfo();
        idInfo.id = ctx.Identifier().toString();
        idInfo.line = ctx.Identifier().getSymbol().getLine();
        idInfo.col = ctx.Identifier().getSymbol().getCharPositionInLine();

        checkIdentifier(idInfo);
    }

    @Override
    public void enterCompoundStatement(CParser.CompoundStatementContext ctx)
    {
        // No longer declaring the function, i.e. return type and name
        isInFunctionDecl = false;

        // The braces should be 4 spaces deeper
        depth += 4;

        int leftBraceLine = ctx.getToken(CParser.LeftBrace, 0).getSymbol().getLine();
        int leftBraceCol = ctx.getToken(CParser.LeftBrace, 0).getSymbol().getCharPositionInLine();

        if (leftBraceCol != depth)
        {
            printMisplacedBraces(leftBraceLine, leftBraceCol, depth);
        }
    }

    @Override
    public void exitCompoundStatement(CParser.CompoundStatementContext ctx)
    {
        int rightBraceLine = ctx.getToken(CParser.RightBrace, 0).getSymbol().getLine();
        int rightBraceCol = ctx.getToken(CParser.RightBrace, 0).getSymbol().getCharPositionInLine();

        if (rightBraceCol != depth)
        {
            printMisplacedBraces(rightBraceLine, rightBraceCol, depth);
        }

        depth -= 4;
    }

    @Override
    public void enterStructOrUnionSpecifier(CParser.StructOrUnionSpecifierContext ctx)
    {
        String id = ctx.Identifier().toString();
        String betterID = StringUtilities.getStyledStructID(id);

        if (!betterID.equals(id))
        {
            String msg = StringUtilities.getPrintInfo(
                    ctx.Identifier().getSymbol().getLine(),
                    ctx.Identifier().getSymbol().getCharPositionInLine(),
                    "Identificador: " + id + " -> " + betterID
            );

            System.out.println(msg);
        }

        Token leftBraceToken = ctx.getToken(CParser.LeftBrace, 0).getSymbol();
        Token rightBraceToken = ctx.getToken(CParser.RightBrace, 0).getSymbol();

        if (leftBraceToken.getLine() == rightBraceToken.getLine()) return;

        if (leftBraceToken.getCharPositionInLine() != 0)
        {
            printMisplacedBraces(leftBraceToken.getLine(), leftBraceToken.getCharPositionInLine(), 0);
        }

        if (rightBraceToken.getCharPositionInLine() != 0)
        {
            printMisplacedBraces(rightBraceToken.getLine(), rightBraceToken.getCharPositionInLine(), 0);
        }
    }

    @Override
    public void enterDeclaration(CParser.DeclarationContext ctx)
    {
        isInDecl = true;

        if (ctx.getText().contains("free("))
        {
            CParser.DirectDeclaratorContext declCtx = ctx.initDeclaratorList()
                    .initDeclarator()
                    .declarator()
                    .directDeclarator()
                    .declarator()
                    .directDeclarator();

            IDInfo varInfo = getTokenInfo(declCtx.Identifier().getSymbol());

            varFreeList.add(varInfo);
        }
        else if (ctx.getText().contains("malloc("))
        {
            CParser.DirectDeclaratorContext declCtx = ctx
                    .initDeclaratorList()
                    .initDeclarator()
                    .declarator()
                    .directDeclarator();

            IDInfo varInfo = getTokenInfo(declCtx.Identifier().getSymbol());

            varMallocList.add(varInfo);
        }

        varDeclList.clear();
    }

    @Override
    public void exitDeclaration(CParser.DeclarationContext ctx)
    {
        isInDecl = false;

        if (isInVarDeclList)
        {
            int lastLine = varDeclList.get(0).line;

            for (int i = 1; i < varDeclList.size(); i++)
            {
                IDInfo idInfo = varDeclList.get(i);
                if (idInfo.line != lastLine)
                {
                    String msg = StringUtilities.getPrintInfo(
                            idInfo.line,
                            idInfo.col,
                            "El identificador " + idInfo.id + " debería estar en la línea " + varDeclList.get(0).line
                    );

                    System.out.println(msg);
                }
            }

            isInVarDeclList = false;
            varDeclList.clear();
        }
    }

    @Override
    public void enterInitDeclaratorList(CParser.InitDeclaratorListContext ctx)
    {
        isInVarDeclList = true;
    }

    @Override
    public void enterAssignmentExpression(CParser.AssignmentExpressionContext ctx)
    {
        isInAsignExpr = true;
    }

    @Override
    public void exitExpression(CParser.ExpressionContext ctx)
    {
        if (isInAsignExpr)
        {
            if (ctx.getText().contains("malloc"))
            {
                varMallocList.add(getTokenInfo(ctx.getStart()));
            }

            isInAsignExpr = false;
        }
    }

    @Override
    public void exitJumpStatement(CParser.JumpStatementContext ctx)
    {
        Token token = ctx.getToken(CParser.Goto, 0).getSymbol();

        if (token.getText().equals("goto"))
        {
            String msg = StringUtilities.getPrintInfo(
                    token.getLine(),
                    token.getCharPositionInLine(),
                    "Use goto con precaución"
            );

            System.out.println(msg);
        }
    }

    private void checkIdentifier(IDInfo idInfo)
    {
        String betterID = idInfo.id.toLowerCase();

        if (!idInfo.id.equals(betterID))
        {
            betterID = StringUtilities.getStyledID(idInfo.id);
            String msg = StringUtilities.getPrintInfo(
                    idInfo.line,
                    idInfo.col,
                    " Identificador: " + idInfo.id + " -> " + betterID
            );

            System.out.println(msg);
        }
    }

    private void printMisplacedBraces(int line, int col, int correctCol)
    {
        System.out.println(
                StringUtilities.getPrintInfo(
                        line,
                        col,
                        "El corchete debería estar en la columna " + (++correctCol)
                )
        );

    }

    private void handleFunctionDecl(IDInfo idInfo)
    {
        if (!isInParameterDecl)
        {
            if (funcSpecList.size() > 0)
            {
                int lastLine = funcSpecList.get(0).line;
                for (int i = 1; i < funcSpecList.size(); i++)
                {
                    int line = funcSpecList.get(i).line;
                    if (line != lastLine)
                    {
                        System.out.println(
                                StringUtilities.getPrintInfo(
                                        line,
                                        funcSpecList.get(i).col,
                                        "El tipo de retorno de la función debería estar en la misma línea"));
                    }

                    lastLine = line;
                }

                if (idInfo.line == lastLine)
                {
                    System.out.println(
                            StringUtilities.getPrintInfo(
                                    idInfo.line,
                                    idInfo.col,
                                    "El identificador (" + idInfo.id + ") debería estar en la siguiente línea"));
                }
            }
        }
    }

    private void handleDeclList(IDInfo idInfo)
    {
        varDeclList.add(idInfo);
    }

    private IDInfo getTokenInfo(Token token)
    {
        IDInfo idInfo = new IDInfo();
        idInfo.id = token.getText();
        idInfo.line = token.getLine();
        idInfo.col = token.getCharPositionInLine();

        return idInfo;
    }

    public void checkMemoryLeaks()
    {
        ArrayList<IDInfo> removable = new ArrayList<>();

        for (int j = 0; j < varMallocList.size(); j++)
        {
            String mallocId = varMallocList.get(j).id;

            for (int i = 0; i < varFreeList.size(); i++)
            {
                String freeId = varFreeList.get(i).id;

                if (mallocId.equals(freeId))
                {
                    removable.add(varMallocList.get(j));
                    varFreeList.remove(i);
                    break;
                }
            }
        }

        for (IDInfo k : removable)
        {
            varMallocList.remove(k);
        }

        for (IDInfo var : varMallocList)
        {
            String msg = StringUtilities.getPrintInfo(
                    var.line,
                    var.col,
                    "La variable '" + var.id + "' no se liberó con free (Posible memory leak)"
            );

            System.out.println(msg);
        }
    }
}
