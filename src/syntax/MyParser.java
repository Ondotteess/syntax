package syntax;

import lexer.MyLexer;
import syntax.rule.SourceText;
import syspro.tm.lexer.Lexer;
import syspro.tm.lexer.Token;
import syspro.tm.parser.Diagnostic;
import syspro.tm.parser.ParseResult;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.parser.TextSpan;

import java.util.Collection;
import java.util.List;

public class MyParser implements syspro.tm.parser.Parser {

    private List<Token> tokens;
    private int position = 0;
    private Context util;

    public MyParser(){

    }

    @Override
    public ParseResult parse(String s) {

        //System.out.println(s);

        Lexer lexer = new MyLexer();
        this.tokens = lexer.lex(s);
        this.util = new Context(this.tokens);

        SyntaxNode root = SourceText.parse(util);

        System.out.println(tokens);

        return new ParseResult() {
            @Override
            public SyntaxNode root() {
                return root;
            }

            @Override
            public Collection<TextSpan> invalidRanges() {
                return List.of();
            }

            @Override
            public Collection<Diagnostic> diagnostics() {
                return List.of();
            }
        };
    }


}
