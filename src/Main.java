import lexer.MyLexer;
import semantic.MySemanticAnalyzer;
import syntax.MyParser;
import syspro.tm.Tasks;
import syspro.tm.WebServer;
import syspro.tm.lexer.*;
import syspro.tm.parser.Diagnostic;
import syspro.tm.parser.ParseResult;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.parser.TextSpan;
import syspro.tm.symbols.LanguageServer;
import syspro.tm.symbols.SemanticModel;
import syspro.tm.symbols.TypeSymbol;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static syntax.MyParser.printTree;

public class Main {

    public static void testLexer(){
        Lexer MyLexer = new MyLexer();

        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().forceLineTerminators(TestLineTerminators.Native));
        //System.out.println();
        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().forceLineTerminators(TestLineTerminators.CarriageReturnLineFeed));
        //System.out.println();
        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().forceLineTerminators(TestLineTerminators.Mixed));
        //System.out.println();
        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().forceLineTerminators(TestLineTerminators.LineFeed));
        //System.out.println();
        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().parallel(true));
        //System.out.println();
        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().shuffled(true));
        //System.out.println();
        //Tasks.Lexer.registerSolution(MyLexer, new TestMode().repeated(true));
        //System.out.println();
        Tasks.Lexer.registerSolution(MyLexer, new TestMode().strict(true));
    }

    public static void testParser(){
        MyParser parser = new MyParser();
        WebServer.start();
        Tasks.Parser.registerSolution(parser);
        WebServer.waitForWebServerExit();
    }

    public static void testSemantic() {
        MySemanticAnalyzer model = new MySemanticAnalyzer();
        WebServer.start();
        Tasks.LanguageServer.registerSolution(model);
        WebServer.waitForWebServerExit();
    }


    public static void main(String[] args) {


        //testLexer();
        //testParser();

        String s = """
class c
  def shift(n: Int32): Int32
    if c < 2048u32
      result.add((c >> 6u32) | 192u32)
      result.add((c & 63u32) | 128u32)
    else

class a
  def b(n: Boolean): Boolean
    val res = ArrayList<ArrayList<Int64>>()

class g
  def f(n: Int32): Iterable<ArrayList<Int64<Float32<Boolean>>>>
    x.add(16 > > 2)
""";

        //MySemanticAnalyzer model = new MySemanticAnalyzer();
        //SemanticModel a = model.buildModel(s);
        //var c =a.lookupType("c");
//
        //int b = 1;
        //b++;
        testSemantic();

    }

}
