import lexer.MyLexer;
import syntax.MyParser;
import syspro.tm.Tasks;
import syspro.tm.WebServer;
import syspro.tm.lexer.*;
import syspro.tm.parser.ParseResult;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public static void main(String[] args) {


        //testLexer();
        testParser();

        // String filePath = "test";
        // MyParser parser = new MyParser();
// /////// 
        // try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
        //                     String content = Files.readString(Path.of(filePath));
        //                     ParseResult result = parser.parse(content);
// /////// 
        //                     printTree(result.root());
        //                 } catch (IOException e) {
        //                     System.out.println("cant open");
        //                 }


    }

}
