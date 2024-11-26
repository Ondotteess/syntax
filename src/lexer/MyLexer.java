package lexer;

import lexer.TokenGenerators.IndentationGenerator;
import lexer.TokenGenerators.TokenGenerator;
import syspro.tm.lexer.Lexer;
import syspro.tm.lexer.Token;

import java.util.ArrayList;
import java.util.List;

public class MyLexer implements Lexer {

    @Override
    public List<Token> lex(String s) {

        System.out.println(s);
        Head head = new Head(s);
        Context context = new Context();
        Tokenizer scanner = new Tokenizer(context);
        ArrayList<TokenGenerator> generators = new ArrayList<>();
        ArrayList<Token> tokens = new ArrayList<>();

        SequenceInfo sequenceInfo = head.readSequence();

        while (sequenceInfo != null) {
            generators.addAll(scanner.scan(sequenceInfo));
            sequenceInfo = head.readSequence();
        }

        for (int i = 0; i < context.currentIndentLevel; i++) {
            IndentationGenerator tg = new IndentationGenerator("", new StringBuilder(), context.start, context.leadingTriviaLength , context.trailingTriviaLength, 0, 0, true);
            tg.setDifference(-1);
            generators.add(tg);
        }

        for (TokenGenerator tg : generators) {
            Token d = tg.getToken();
            tokens.add(d);
            //System.out.println(d.getClass().toString() + "  " +
            //                     d + "\t\t" +
            //       d.start + "\t\t" +
            //       d.end + "\t\t" +
            //       d.leadingTriviaLength + "\t\t" +
            //       d.trailingTriviaLength);
        }

        return tokens;
    }
}