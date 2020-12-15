
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class DB {
    private Map<String,Table> tables;

    public DB(Map<String,Table> tables) {
        this.tables = tables;
    }
    public DB() {
        this.tables = new HashMap<>();
    }

    public Map<String, Table> getTables() {
        return tables;
    }

    public void setTables(Map<String, Table> tables) {
        this.tables = tables;
    }


    public boolean handleCommand(String commandLine) throws Exception {
        commandLine=commandLine.split("//")[0].replaceAll("\\s|\\n","");//get rid of the comment
        if(commandLine.length()==0){
            return true;
        }
        System.out.println("executing command:"+commandLine);
        if(commandLine.equals("quit")){
            return false;
        }
        long startTime=System.currentTimeMillis();
        if(commandLine.contains(":=")){//assignment
            String targetName=commandLine.split(":=")[0].toLowerCase();
            String command=commandLine.split(":=")[1].toLowerCase();
            String commandType = command.split("\\(")[0].toLowerCase();
            String commandContent = command.substring(command.indexOf("(")+1,command.lastIndexOf(")"));
            String[] args;
            switch(commandType){
                case "select"://select(A,(A>5)or(B>C))
                    commandContent=commandContent.toLowerCase();
                    String tableName = commandContent.split(",")[0];
                    String conditions = commandContent.split(",")[1];
                    this.tables.put(targetName,this.tables.get(tableName).select(conditions));
                    break;
                case "inputfromfile":
                    this.tables.put(targetName,new Table(commandContent));
                    break;
                case "project":
                    commandContent=commandContent.toLowerCase();
                    args = commandContent.split(",");

                    Table t = this.tables.get(args[0]);
                    List<String> rows = new ArrayList<>();
                    for(int i=1;i<args.length;i++){
                        rows.add(args[i]);
                    }
                    this.tables.put(targetName,t.projection(rows));
                    break;
                case "avg":
                    commandContent=commandContent.toLowerCase();
                    args = commandContent.split(",");
                    Table avgSourceTable = this.tables.get(args[0]);
                    this.tables.put(targetName,avgSourceTable.avg(args[1]));
                    break;
                case "sumgroup":
                    commandContent=commandContent.toLowerCase();
                    args = commandContent.split(",");


                    Table sumGroupSource = this.tables.get(args[0]);
                    String sumRow = args[1];
                    List<String> sumGroupRows = new ArrayList<>();
                    for(int i=2;i<args.length;i++){
                        sumGroupRows.add(args[i]);
                    }
                    this.tables.put(targetName,sumGroupSource.sumGroup(sumRow,sumGroupRows));
                    break;
                case "avggroup":
                    commandContent=commandContent.toLowerCase();
                    args = commandContent.split(",");
                    Table avgGroupSource = this.tables.get(args[0]);
                    String avgGroupRow = args[1];
                    List<String> avgGroupRows = new ArrayList<>();
                    for(int i=2;i<args.length;i++){
                        avgGroupRows.add(args[i]);
                    }
                    this.tables.put(targetName,avgGroupSource.avgGroup(avgGroupRow,avgGroupRows));
                    break;
                case "join":
                    commandContent=commandContent.toLowerCase();
                    args = commandContent.split(",");
                    this.tables.put(targetName,this.join(args[2]));
                    break;
                case "sort":
                    commandContent=commandContent.toLowerCase();
                    args = commandContent.split(",");
                    List<String> sortRows =new ArrayList<>();
                    for(int i=1;i<args.length;i++){
                        sortRows.add(args[i]);
                    }
                    this.tables.put(targetName,this.tables.get(args[0]).sort(sortRows));
                    break;
                case "movavg":
                    commandContent=commandContent.toLowerCase();
                    args = commandContent.split(",");
                    this.tables.put(targetName,this.tables.get(args[0]).movAvg(args[1],Integer.parseInt(args[2])));
                    break;
                case "movsum":
                    commandContent=commandContent.toLowerCase();
                    args = commandContent.split(",");
                    this.tables.put(targetName,this.tables.get(args[0]).movSum(args[1],Integer.parseInt(args[2])));
                    break;
                case "concat":
                    commandContent=commandContent.toLowerCase();
                    args = commandContent.split(",");
                    this.tables.put(targetName,this.tables.get(args[0]).concat(this.tables.get(args[1])));
                    break;

            }
        }
        else{//no-assignment
            String commandType = commandLine.split("\\(")[0];
            String commandContent = commandLine.substring(commandLine.indexOf("(")+1,commandLine.lastIndexOf(")"));
            String[] args;

            switch(commandType.toLowerCase()){
                case "btree":
                    commandContent=commandContent.toLowerCase();
                    args =commandContent.split(",");
                    this.tables.get(args[0]).generateIndex(Table.BTREE,args[1]);
                    break;
                case "hash":
                    commandContent=commandContent.toLowerCase();
                    args =commandContent.split(",");
                    this.tables.get(args[0]).generateIndex(Table.HASH,args[1]);
                    break;
                case "outputtofile":
                    commandContent=commandContent.toLowerCase();
                    args =commandContent.split(",");
                    outputOfFile(args[0],"|");
                    break;

            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("used time :"+(endTime-startTime)+"ms");
        return true;
    }

    private List<Map<String,Integer>> getRangeJoinByBtreeIndex(String n1,String n2,String joinRow1,String joinRow2,String condition,String op){
        List<Map<String,Integer>> newRecords = null;

        Table t1 = this.getTables().get(n1);
        Table t2 = this.getTables().get(n2);
        IndexContainer<Integer,List<Integer>> index1 = t1.getIndexes().get(joinRow1);
        IndexContainer<Integer,List<Integer>> index2 = t2.getIndexes().get(joinRow2);


        int flag=0;
        BTree<Integer,List<Integer>>  btree=null;
        if(index1!=null && index1 instanceof BTree){
            flag=1;
            btree = (BTree) index1;
        }
        if(index2!=null && index2 instanceof BTree){
            flag=2;
            btree = (BTree) index2;
        }
        List<List<Integer>> ll =null;
        if(flag==1){
            newRecords = new ArrayList<>();
            for(int i=0;i<t2.getRecords().size();i++){
                Map<String,Integer> record2 = t2.getRecords().get(i);

                switch(op){
                    case ">":
                        ll = btree.greaterQuery(record2.get(joinRow2));
                        break;
                    case "<":
                        ll = btree.lesserQuery(record2.get(joinRow2));
                        break;
                    case ">=":
                        ll =btree.greaterQuery(record2.get(joinRow2));
                        List<Integer> index = btree.get(record2.get(joinRow2));
                        if(index!=null)
                            ll.add(index);
                        break;
                    case "<=":
                        ll =btree.lesserQuery(record2.get(joinRow2));
                        List<Integer> indexL = btree.get(record2.get(joinRow2));
                        if(indexL!=null)
                            ll.add(indexL);
                        break;
                }
                for(int j=0;j<ll.size();j++){
                    for(int k=0;k<ll.get(j).size();k++) {
                        Map<String, Integer> record = new HashMap<>();
                        Map<String,Integer> record1 = t1.getRecords().get(ll.get(j).get(k));

                        for (String name : t1.getRowNames()) {
                            record.put(n1 + "_" + name, record1.get(name));
                        }
                        for (String name : t2.getRowNames()) {
                            record.put(n2 + "_" + name, record2.get(name));
                        }
                        newRecords.add(record);
                    }
                }
            }
        }else if(flag==2){//t2 Btree used
            newRecords = new ArrayList<>();
            for(int i=0;i<t1.getRecords().size();i++){
                Map<String,Integer> record1 = t1.getRecords().get(i);
                switch(op){
                    case ">":
                        ll = btree.lesserQuery(record1.get(joinRow1));
                        break;
                    case "<":
                        ll = btree.greaterQuery(record1.get(joinRow1));
                        break;
                    case ">=":
                        ll = btree.lesserQuery(record1.get(joinRow1));
                        List<Integer> index = btree.get(record1.get(joinRow1));
                        if(index!=null)
                            ll.add(index);
                        break;
                    case "<=":
                        ll = btree.greaterQuery(record1.get(joinRow1));
                        List<Integer> indexL = btree.get(record1.get(joinRow1));
                        if(indexL!=null)
                            ll.add(indexL);
                        break;
                }
                for(int j=0;j<ll.size();j++){
                    for(int k=0;k<ll.get(j).size();k++) {
                        Map<String, Integer> record = new HashMap<>();
                        Map<String,Integer> record2 = t2.getRecords().get(ll.get(j).get(k));

                        for (String name : t1.getRowNames()) {
                            record.put(n1 + "_" + name, record1.get(name));
                        }
                        for (String name : t2.getRowNames()) {
                            record.put(n2 + "_" + name, record2.get(name));
                        }
                        newRecords.add(record);
                    }
                }
            }
        }
        return newRecords;
    }
    public Table join(String condition){
        String op="";
        if(condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)=([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){
            op="=";
        }
        else if (condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)>([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){
            op=">";
        }
        else if(condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)>=([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){
            op=">=";
        }
        else if(condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)<([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){
            op="<";
        }
        else if(condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)<=([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){
            op="<=";
        }
        else if(condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)!=([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){
            op="!=";
        }
        condition= condition.trim();
        String r1 = condition.split(op)[0];
        String r2 = condition.split(op)[1];
        String n1 = r1.split("\\.")[0];
        String n2 = r2.split("\\.")[0];
        String joinRow1 = r1.split("\\.")[1];
        String joinRow2 = r2.split("\\.")[1];


        Table t1 = this.getTables().get(n1);
        Table t2 = this.getTables().get(n2);


        List<Map<String,Integer>> records = new ArrayList<>();
        List<String> names = new ArrayList<>();

        for(String s: t1.getRowNames()){
            names.add(n1+"_"+s);
        }
        for(String s:t2.getRowNames()){
            names.add(n2+"_"+s);
        }
        if(op.matches(">=|<=|>|<")){
            List<Map<String,Integer>> rangeRecords = getRangeJoinByBtreeIndex(n1,n2,joinRow1,joinRow2,condition,op);
            if(rangeRecords!=null){
                return new Table(rangeRecords,names);
            }
        }
        int flag=0;
        if(op.equals("=")){//A.B=C.D
            if(t1.getIndexes().containsKey(joinRow1)){
                flag=1;
            }
            else if(t2.getIndexes().containsKey(joinRow2)){
                flag=2;
            }
        }

        if(flag==0) {//no index used


            for (Map<String, Integer> record1 : t1.getRecords()) {
                for (Map<String, Integer> record2 : t2.getRecords()) {
                    boolean satisfied=false;
                    if(op.equals(">")){
                        satisfied = record1.get(joinRow1) > record2.get(joinRow2);
                    }
                    else if(op.equals("=")){
                        satisfied = record1.get(joinRow1) == record2.get(joinRow2);
                    }
                    else if(op.equals("<")){
                        satisfied = record1.get(joinRow1) < record2.get(joinRow2);
                    }
                    else if(op.equals(">=")){
                        satisfied = record1.get(joinRow1) >= record2.get(joinRow2);
                    }
                    else if(op.equals("<=")){
                        satisfied = record1.get(joinRow1) <= record2.get(joinRow2);
                    }
                    else if(op.equals("!=")){
                        satisfied = record1.get(joinRow1) != record2.get(joinRow2);
                    }
                    if(satisfied){
                        Map<String,Integer> record = new HashMap<>();
                        for(String name:t1.getRowNames()){
                            record.put(n1+"_"+name,record1.get(name));
                        }
                        for(String name:t2.getRowNames()){
                            record.put(n2+"_"+name,record2.get(name));
                        }
                        records.add(record);
                    }
                }
            }
        }
        else if (flag==1){//t1 index used
            IndexContainer<Integer,List<Integer>> index = t1.getIndexes().get(joinRow1);
            for(Map<String,Integer> record2: t2.getRecords()){
                List<Integer> indexes=index.get(record2.get(joinRow2));

                if(indexes!=null){

                    for(Integer i:indexes){
                        Map<String,Integer> record1 = t1.getRecords().get(i);
                        Map<String,Integer> record = new HashMap<>();
                        for(String name:t1.getRowNames()){
                            record.put(n1+"_"+name,record1.get(name));
                        }
                        for(String name:t2.getRowNames()){
                            record.put(n2+"_"+name,record2.get(name));
                        }
                        records.add(record);
                    }
                }
            }
        }
        else{//t2 index used
            IndexContainer<Integer,List<Integer>> index = t2.getIndexes().get(joinRow2);
            for(Map<String,Integer> record1: t1.getRecords()){
                List<Integer> indexes = index.get(record1.get(joinRow1));

                if(indexes!=null){
                    for(Integer i:indexes){
                        Map<String,Integer> record2 = t2.getRecords().get(i);
                        Map<String,Integer> record = new HashMap<>();
                        for(String name:t1.getRowNames()){
                            record.put(n1+"_"+name,record1.get(name));
                        }
                        for(String name:t2.getRowNames()){
                            record.put(n2+"_"+name,record2.get(name));
                        }
                        records.add(record);
                    }
                }
            }
        }
        return new Table(records,names);
    }
    public void inputOfFile(String name,String file) throws IOException {
        Table table = new Table(file);
        this.tables.put(name,table);
    }
    public void outputOfFile(String tableName,String splitter){

        System.out.println(this.tables.get(tableName).toString(splitter));

    }
    public static void main(String[] args){
        String command="select(A,(A>5)or(B<10))";
        String selectContent = command.substring(command.indexOf("(")+1,command.lastIndexOf(")"));
        System.out.println(selectContent);

    }
}

