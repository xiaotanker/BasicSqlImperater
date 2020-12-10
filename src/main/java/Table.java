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
        String nameLine = reader.readLine().toLowerCase();
        String[] names = nameLine.split("\\|");

        rowNames = new ArrayList<>(Arrays.asList(names));

        records=new ArrayList<>();
        String dataLine;
        while(( dataLine = reader.readLine()) != null){
            dataLine = dataLine.replaceAll(" ","");
            String[] data =dataLine.split("\\|");
            Map<String,Integer> record = new HashMap<>();
            for(int i=0;i<data.length;i++){
                record.put(names[i],Integer.valueOf(data[i]));
            }
            this.records.add(record);
        }
        this.indexes=new HashMap<>();
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
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        List<String> newNames = new ArrayList<>();
        newNames.add("sum");
        newNames.addAll(groupRows);
        Map<List<Integer>,Map<String,Integer>> group = new HashMap<>();

        for(int i=0;i<records.size();i++){
            Map<String,Integer> record = records.get(i);
            List<Integer> groupList = new ArrayList<>();
            for (String groupRow : groupRows) {
                groupList.add(record.get(groupRow));
            }
            Map<String,Integer> groupRecord = group.get(groupList);
            if(groupRecord==null){
                groupRecord = new HashMap<>();
                for(String row:groupRows){
                    groupRecord.put(row,record.get(row));
                }
                groupRecord.put("sum",record.get(sumRow));
                group.put(groupList,groupRecord);
            }
            else{
                groupRecord.put("sum",record.get(sumRow)+groupRecord.get("sum"));
            }
        }
        for(Map.Entry<List<Integer>,Map<String,Integer>> entry:group.entrySet()){
            newRecords.add(entry.getValue());
        }
        return new Table(newRecords,newNames);
    }

    public Table sort(List<String> rows){
        if(indexes.containsKey(rows.get(0))&&indexes.get(rows.get(0))instanceof BTree){//BTree indexed
            List<Map<String,Integer>> newRecords = new ArrayList<>();
            BTree<Integer,List<Integer>> btree = (BTree) indexes.get(rows.get(0));
            List<List<Integer>> ll = btree.lesserQuery(Integer.MAX_VALUE);
            for(int i=0;i<ll.size();i++){
                List<Integer> l = ll.get(i);
                for(int j=0;j<l.size();j++){
                    newRecords.add(this.records.get(l.get(j)));
                }
            }
            return new Table(newRecords,this.rowNames);
        }
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        newRecords.addAll(records);

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
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        List<String> newNames = new ArrayList<>();
        newNames.add("avg");
        newNames.addAll(groupRows);
        Map<List<Integer>,Map<String,Integer>> group = new HashMap<>();
        Map<List<Integer>,Integer> groupCount= new HashMap<>();

        for(int i=0;i<records.size();i++){
            Map<String,Integer> record = records.get(i);
            List<Integer> groupList = new ArrayList<>();
            for (String groupRow : groupRows) {
                groupList.add(record.get(groupRow));
            }
            Map<String,Integer> groupRecord = group.get(groupList);
            if(groupRecord==null){
                groupRecord = new HashMap<>();
                for(String row:groupRows){
                    groupRecord.put(row,record.get(row));
                }
                groupRecord.put("avg",record.get(avgRow));
                group.put(groupList,groupRecord);
                groupCount.put(groupList,1);
            }
            else{
                int count = groupCount.get(groupList);
                groupRecord.put("avg",record.get(avgRow)+groupRecord.get("avg"));
                groupCount.put(groupList,count+1);
            }
        }
        for(Map.Entry<List<Integer>,Map<String,Integer>> entry:group.entrySet()){
            Map<String,Integer> record = entry.getValue();
            int count = groupCount.get(entry.getKey());
            int sum = record.get("avg");
            record.put("avg",sum/count);
            newRecords.add(record);
        }
        return new Table(newRecords,newNames);
    }

    public Table movAvg(String avgRow,int mov){
        Integer sum=0;

        List<Map<String,Integer>> newRecords = new ArrayList<>();

        for(int i=0;i<records.size();i++){
            sum+=records.get(i).get(avgRow);
            Map<String, Integer> record = new HashMap<>();
            for(String name:this.rowNames) {
                if(name.equals(avgRow)) {
                    if (i >= mov) {
                        sum -= records.get(i - mov).get(avgRow);
                        record.put(avgRow, sum / mov);

                    } else {
                        record.put(avgRow, sum / (i + 1));
                    }
                }
                else{
                    record.put(name,records.get(i).get(name));
                }
            }
            newRecords.add(record);
        }
        return new Table(newRecords,this.rowNames);
    }

    public Table movSum(String sumRow,int mov){
        Integer sum=0;

        List<Map<String,Integer>> newRecords = new ArrayList<>();

        for(int i=0;i<records.size();i++){
            sum+=records.get(i).get(sumRow);
            Map<String, Integer> record = new HashMap<>();
            for(String name:this.rowNames) {
                if(name.equals(sumRow)) {
                    if (i >= mov) {
                        sum -= records.get(i - mov).get(sumRow);
                    }
                    record.put(sumRow, sum);
                }
                else{
                    record.put(name,this.records.get(i).get(name));
                }
            }
            newRecords.add(record);
        }
        return new Table(newRecords,this.rowNames);
    }
    public Table avg(String rowName){
        List<String> newNames = new ArrayList<>();
        newNames.add("avg");
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        Integer sum=0;
        for(Map<String,Integer> record : this.records){
            sum+=record.get(rowName);
        }
        Map<String,Integer> record = new HashMap<>();
        record.put("avg",sum/records.size());
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

    private List<Map<String,Integer>> getRangeRecordsByBTreeIndex(String condition,String op){
        List<Map<String,Integer>> newRecords = null;
        String l = condition.split(op)[0];
        String r = condition.split(op)[1];
        List<Integer> index;
        Integer x =0;
        String indexRow;
        int flag=0;
        if(l.matches("[0-9]+")){//5opA
            x=Integer.valueOf(l);
            indexRow=r;
            flag=1;
        }else{//Aop5
            x=Integer.valueOf(r);
            indexRow=l;
        }
        if(indexes.get(indexRow)!=null&&indexes.get(indexRow) instanceof BTree) {
            newRecords = new ArrayList<>();
            BTree<Integer, List<Integer>> btree = (BTree) indexes.get(indexRow);

            List<List<Integer>> ll =null;
            if(flag==0){
                switch(op){
                    case ">"://A>5
                        ll = btree.greaterQuery(x);
                        break;
                    case ">="://a>=5
                        ll = btree.greaterQuery(x);
                        index = btree.get(x);
                        if(index!=null) {
                            ll.add(index);
                        }
                        break;
                    case "<":
                        ll = btree.lesserQuery(x);
                        break;
                    case "<=":
                        ll = btree.lesserQuery(x);
                        index = btree.get(x);
                        if(index!=null) {
                            ll.add(index);
                        }
                }

            }
            else{
                switch(op){
                    case ">"://5>a
                        ll = btree.lesserQuery(x);
                        break;
                    case ">="://a>=5
                        ll = btree.lesserQuery(x);
                        index = btree.get(x);
                        if(index!=null) {
                            ll.add(index);
                        }
                        break;
                    case "<":
                        ll = btree.greaterQuery(x);
                        break;
                    case "<=":
                        ll = btree.greaterQuery(x);
                        index = btree.get(x);
                        if(index!=null) {
                            ll.add(index);
                        }
                }
            }
            for (int i = 0; i < ll.size(); i++) {
                List<Integer> list = ll.get(i);
                for (int j = 0; j < list.size(); j++) {
                    newRecords.add(this.records.get(list.get(j)));
                }
            }
        }
        return newRecords;

    }

    public Table select(String conditions){
        List<Map<String,Integer>> newRecords = new ArrayList<>();
        if(!conditions.matches("([A-z]([A-z]|[0-9]|_)*)(=|>|<|<=|>=|!=)([A-z]([A-z]|[0-9]|_)*)")){// if C OP C no index will be used
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
                    if(indexList!=null) {
                        for (Integer i : indexList) {
                            newRecords.add(this.records.get(i));
                        }
                        return new Table(newRecords, this.rowNames);
                    }
                }

            }
            else if(conditions.matches("(([A-z]([A-z]|[0-9]|_)*)|[0-9]+)>(([A-z]([A-z]|[0-9]|_)*)|[0-9]+)")) {
                List<Map<String,Integer>> btreeRecords = getRangeRecordsByBTreeIndex(conditions,">");
                if(btreeRecords!=null){
                    return new Table(btreeRecords,this.rowNames);
                }

            }
            else if(conditions.matches("(([A-z]([A-z]|[0-9]|_)*)|[0-9]+)<(([A-z]([A-z]|[0-9]|_)*)|[0-9]+)")) {
                List<Map<String,Integer>> btreeRecords = getRangeRecordsByBTreeIndex(conditions,"<");
                if(btreeRecords!=null){
                    return new Table(btreeRecords,this.rowNames);
                }

            }
            else if(conditions.matches("(([A-z]([A-z]|[0-9]|_)*)|[0-9]+)>=(([A-z]([A-z]|[0-9]|_)*)|[0-9]+)")) {
                List<Map<String,Integer>> btreeRecords = getRangeRecordsByBTreeIndex(conditions,">=");
                if(btreeRecords!=null){
                    return new Table(btreeRecords,this.rowNames);
                }

            }
            else if(conditions.matches("(([A-z]([A-z]|[0-9]|_)*)|[0-9]+)<=(([A-z]([A-z]|[0-9]|_)*)|[0-9]+)")) {
                List<Map<String,Integer>> btreeRecords = getRangeRecordsByBTreeIndex(conditions,"<=");
                if(btreeRecords!=null){
                    return new Table(btreeRecords,this.rowNames);
                }

            }
        }
        CharStream stream = CharStreams.fromString(conditions);

        GrammarLexer lexer = new GrammarLexer(stream);


        CommonTokenStream tokens = new CommonTokenStream(lexer);


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
        newRecords.addAll(records1);
        newRecords.addAll(records2);
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
            sb.append(splitter).append(rowNames.get(i));
        }
        sb.append("\n");

        for(Map<String,Integer> record:records){
            sb.append(record.get(rowNames.get(0)));
            for(int i=1;i<rowNames.size();i++){
                sb.append(splitter).append(record.get(rowNames.get(i)));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
//    public static void main(String[] args){
//        try {
//            Table table = new Table("table.txt");
//            String[] rows = {"b","c"};
//            table.sumGroup("a", Arrays.asList(rows));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
