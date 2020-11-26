// Generated from C:/Users/bobo/Documents/Database/20201217tj2099_proj/src/main/java\Grammar.g4 by ANTLR 4.8
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link GrammarParser}.
 */
public interface GrammarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link GrammarParser#conditions}.
	 * @param ctx the parse tree
	 */
	void enterConditions(GrammarParser.ConditionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link GrammarParser#conditions}.
	 * @param ctx the parse tree
	 */
	void exitConditions(GrammarParser.ConditionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link GrammarParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(GrammarParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link GrammarParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(GrammarParser.ConditionContext ctx);
}