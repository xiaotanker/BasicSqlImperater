import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;

public class Boot {
    public static void main(String[]args){
        DB db= new DB();
        //对每一个输入的字符串，构造一个 ANTLRStringStream 流 in
        ANTLRInputStream in = new ANTLRInputStream("R1 := select(R, (A > 5) or (B < 3)) \n");

        //用 in 构造词法分析器 lexer，词法分析的作用是产生记号
        GrammarLexer lexer = new GrammarLexer(in);

        //用词法分析器 lexer 构造一个记号流 tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        //再使用 tokens 构造语法分析器 parser,至此已经完成词法分析和语法分析的准备工作
        GrammarParser parser = new GrammarParser(tokens);

        //最终调用语法分析器的规则 prog，完成对表达式的验证
        

    }
}
