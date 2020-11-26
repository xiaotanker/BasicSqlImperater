import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.Map;

public class MyGrammarVisitor extends AbstractParseTreeVisitor implements GrammarVisitor {
    private Map<String,Integer> record;

    public Map<String, Integer> getRecord() {
        return record;
    }

    public void setRecord(Map<String, Integer> record) {
        this.record = record;
    }


    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public Boolean visitConditions(GrammarParser.ConditionsContext ctx) {
        List<ParseTree> ts = ctx.children;
        if(ts.size()==1){//conditions-> condition
            return visitCondition((GrammarParser.ConditionContext) ts.get(0));
        }
        else{//conditions->condition (and|or) conditions
            Boolean b1 = visitCondition((GrammarParser.ConditionContext) ts.get(0));
            Boolean b2 = visitConditions((GrammarParser.ConditionsContext) ts.get(2));

            switch(ts.get(1).getText().toLowerCase()){
                case "and":
                    return b1&&b2;
                case "or":
                    return b1||b2;
                default:
            }
        }
        return true;
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public Boolean visitCondition(GrammarParser.ConditionContext ctx) {
        List<ParseTree> ts = ctx.children;
        if(ts.get(0).getText().equals("(")){//( conditions )
            return visitConditions((GrammarParser.ConditionsContext) ts.get(1));

        }
        else{//token (>|=|<) token
            Integer x1,x2;
            String s1 = ts.get(0).getText();
            String s2 = ts.get(2).getText();
            try {
                x1=Integer.parseInt(s1);
            }catch(NumberFormatException e){
                x1=record.get(s1);
            }
            try {
                x2=Integer.parseInt(s2);
            }catch(NumberFormatException e){
                x2=record.get(s2);
            }
            switch(ts.get(1).getText()){
                case ">":
                    return x1>x2;
                case "=":
                    return x1==x2;
                case "<":
                    return x1<x2;
                case ">=":
                    return x1>=x2;
                case "<=":
                    return x1<=x2;
                case "!=":
                    return x1!=x2;
            }
            return false;
        }
    }

}
