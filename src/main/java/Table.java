import org.antlr.v4.runtime.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.*;

public class Table {
    public static final String HASH="HASH";
    public static final String BTREE="BTREE";
    private List<Map<String,Integer>> records;
    private List<String> rowNames;

    public Map<String, IndexContainer<Integer, List<Integer>>> getIndexes() {
        return indexes;
    }

    public void setIndexes(Map<String, IndexContainer<Integer, List<Integer>>> indexes) {
        this.indexes = indexes;
    }

    private Map<String,IndexContainer<Integer,List<Integer>>> indexes;

    public List<Map<String, Integer>> getRecords() {
        return records;
    }

    public Table(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String nameLine = reader.readLine();
        String[] names = nameLine.split("\\|");
        rowNames = new ArrayList<>(Arrays.asList(names));
        records=new ArrayList<>();
        String dataLine;
        while(( dataLine = reader.readLine()) != null){
            String[] data =dataLine.split("\\|");
            Map<String,Integer> record = new HashMap<>();
            for(int i=0;i<data.length;i++){
                record.put(names[i],Integer.valueOf(data[i]));
            }
            this.records.add(record);
        }
        reader.close();
    }
    public void setRecords(List<Map<String, Integer>> records) {
        this.records = records;
    }

    public Table projection(List<String> rowNames){
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        for(Map<String,Integer>record:records ){
            Map<String,Integer> newRecord=new HashMap<>();
            for(String s:rowNames){
                newRecord.put(s,record.get(s));
            }
            newRecords.add(newRecord);
        }
        List<String> newRowNames = rowNames;
        return new Table(newRecords,newRowNames);
    }

    public Table sumGroup(String sumRow,List<String> groupRows){
        boolean[] flag = new boolean[records.size()];
        for(int i=0;i<flag.length;i++){
            flag[i]=false;
        }
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        List<String> newNames = new ArrayList<>();
        newNames.add("SUM");
        for(String row:groupRows){
            newNames.add(row);
        }
        for(int i=0;i<records.size();i++){
            if(flag[i]==false){
                Integer sum=records.get(i).get(sumRow);
                Map<String,Integer> currentRecord = records.get(i);
                for(int j=i+1;j<records.size();j++){
                    if(flag[j]==false){
                        boolean f=true;
                        for(String row:groupRows){
                            if(currentRecord.get(row)!=records.get(j).get(row)){
                                f=false;
                                break;
                            }
                        }
                        if(f==true){//group
                            flag[j]=true;
                            sum+=records.get(j).get(sumRow);
                        }

                    }
                }
                Map<String,Integer> record=new HashMap<>();
                record.put("SUM",sum);
                for(String row:groupRows){
                    record.put(row,currentRecord.get(row));
                }
                newRecords.add(record);
                flag[i]=true;
            }
        }
        return new Table(newRecords,newNames);
    }

    public Table sort(List<String> rows){
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        for(int i=0;i<records.size();i++){
            newRecords.add(records.get(i));
        }

        Collections.sort(newRecords, (record1, record2) -> {
            for(int i=0;i<rows.size();i++){
                String row = rows.get(i);
                if(record1.get(row)>record2.get(row)){
                    return 1;
                }
                else if(record1.get(row)<record2.get(row)){
                    return -1;
                }
            }
            return 0;
        });
        return new Table(newRecords,this.rowNames);
    }
    public Table avgGroup(String avgRow,List<String> groupRows){
        boolean[] flag = new boolean[records.size()];
        for(int i=0;i<flag.length;i++){
            flag[i]=false;
        }
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        List<String> newNames = new ArrayList<>();
        newNames.add("AVG");
        for(String row:groupRows){
            newNames.add(row);
        }
        for(int i=0;i<records.size();i++){
            if(flag[i]==false){
                Integer sum=records.get(i).get(avgRow);
                Integer count=1;
                Map<String,Integer> currentRecord = records.get(i);
                for(int j=i+1;j<records.size();j++){
                    if(flag[j]==false){
                        boolean f=true;
                        for(String row:groupRows){
                            if(currentRecord.get(row)!=records.get(j).get(row)){
                                f=false;
                                break;
                            }
                        }
                        if(f==true){//group
                            flag[j]=true;
                            sum+=records.get(j).get(avgRow);
                            count++;
                        }

                    }
                }
                Map<String,Integer> record=new HashMap<>();
                record.put("AVG",sum/count);
                for(String row:groupRows){
                    record.put(row,currentRecord.get(row));
                }
                newRecords.add(record);
                flag[i]=true;
            }
        }
        return new Table(newRecords,newNames);
    }

    public Table movAvg(String avgRow,int mov){
        Integer sum=0;
        List<String> newNames = new ArrayList<>();
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        newNames.add("movAvg");
        for(int i=0;i<records.size();i++){
            sum+=records.get(i).get(avgRow);
            if(i>=mov){
                sum-=records.get(i-mov).get(avgRow);
                Map<String,Integer> record = new HashMap<>();
                record.put("movAvg",sum/mov);
                newRecords.add(record);
            }
            else{
                Map<String,Integer> record = new HashMap<>();
                record.put("movAvg",sum/(i+1));
                newRecords.add(record);
            }
        }
        return new Table(newRecords,newNames);
    }

    public Table movSum(String sumRow,int mov){
        Integer sum=0;
        List<String> newNames = new ArrayList<>();
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        newNames.add("movSum");
        for(int i=0;i<records.size();i++){
            sum+=records.get(i).get(sumRow);
            if(i>=mov){
                sum-=records.get(i-mov).get(sumRow);
            }
            Map<String,Integer> record = new HashMap<>();
            record.put("movSum",sum);
            newRecords.add(record);
        }
        return new Table(newRecords,newNames);
    }
    public Table avg(String rowName){
        List<String> newNames = new ArrayList<>();
        newNames.add("AVG");
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        Integer sum=0;
        for(Map<String,Integer> record : this.records){
            sum+=record.get(rowName);
        }
        Map<String,Integer> record = new HashMap<>();
        record.put("AVG",sum/records.size());
        newRecords.add(record);
        return new Table(newRecords,newNames);
    }

    public void generateIndex(String type, String rowName) throws Exception {
        IndexContainer<Integer,List<Integer>> index;
        if(type.equals(Table.BTREE)){
            index=new BTree<>();
        }else if (type.equals(Table.HASH)){
            index=new Hash<>();
        }
        else{
            throw new Exception("type not found");
        }
        String indexName=rowName;
        for(int i=0;i<records.size();i++){
            Integer key = records.get(i).get(rowName);
            if(index.get(key)==null){
                List<Integer> list=new LinkedList<>();
                list.add(i);
                index.put(key,list);
            }
            else{
                List<Integer> list = index.get(key);
                list.add(i);
            }
        }
        indexes.put(indexName,index);
    }

    public List<String> getRowNames() {
        return rowNames;
    }

    public void setRowNames(List<String> rowNames) {
        this.rowNames = rowNames;
    }


    public Table select(String conditions){
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        if(conditions.matches("(([A-z]([A-z]|[0-9]|_)*)|[0-9]+)=(([A-z]([A-z]|[0-9]|_)*)|[0-9]+)")){//equals
            String l = conditions.split("=")[0];
            String r = conditions.split("=")[1];
            Integer x =0;
            String indexRow;
            if(l.matches("[0-9]+")){
                x=Integer.valueOf(l);
                indexRow=r;
            }else{
                x=Integer.valueOf(r);
                indexRow=l;
            }
            if(indexes.containsKey(indexRow)){//index used
                List<Integer> indexList = indexes.get(indexRow).get(x);
                for(Integer i: indexList){
                    newRecords.add(this.records.get(i));
                }
                return new Table(newRecords,this.rowNames);
            }

        }

        CharStream stream = CharStreams.fromString(conditions);
        //用 in 构造词法分析器 lexer，词法分析的作用是产生记号
        GrammarLexer lexer = new GrammarLexer(stream);

        //用词法分析器 lexer 构造一个记号流 tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        //再使用 tokens 构造语法分析器 parser,至此已经完成词法分析和语法分析的准备工作
        GrammarParser parser = new GrammarParser(tokens);
        GrammarParser.ConditionsContext ctx=parser.conditions();



        MyGrammarVisitor visitor= new MyGrammarVisitor();
        for(Map<String,Integer> record:this.records) {//no index used


            visitor.setRecord(record);
            if(visitor.visitConditions(ctx)){
                newRecords.add(record);
            }

        }
        return new Table(newRecords,this.rowNames);

    }

    public Table concat(Table table){
        List<Map<String,Integer>> records1 = this.records;
        List<Map<String,Integer>> records2 = table.getRecords();
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        for(int i=0;i<records1.size();i++){
            newRecords.add(records1.get(i));
        }
        for(int i=0;i<records2.size();i++){
            newRecords.add(records2.get(i));
        }
        return new Table(newRecords,this.rowNames);
    }
    public Table(List<Map<String,Integer>> records,List<String> rowNames) {
        this.records = records;
        this.rowNames = rowNames;
        this.indexes= new HashMap<>();
    }
    public String toString(String splitter){
        StringBuilder sb = new StringBuilder();
        sb.append(rowNames.get(0));
        for(int i=1;i<rowNames.size();i++){
            sb.append(splitter+rowNames.get(i));
        }
        sb.append("\n");

        for(Map<String,Integer> record:records){
            sb.append(record.get(rowNames.get(0)));
            for(int i=1;i<rowNames.size();i++){
                sb.append(splitter+record.get(rowNames.get(i)));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    public static void main(String[] args){
        try {
            Table t=new Table("sales1.txt");
            String[] rowNames={"pricerange","saleid"};
            System.out.println(t.movSum("qty",3).toString("|"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}