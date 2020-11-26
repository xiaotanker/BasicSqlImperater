
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void handleCommand(String commandLine) throws Exception {
        commandLine=commandLine.split("//")[0].trim();//get rid of the comment
        if(commandLine.length()==0){
            return;
        }
        if(commandLine.contains(":=")){//assignment
            String targetName=commandLine.split(":=")[0];
            String command=commandLine.split(":=")[1];
            String commandType = command.split("\\(")[0];
            String commandContent = command.substring(command.indexOf("(")+1,command.lastIndexOf(")"));
            String[] args;
            switch(commandType.toLowerCase()){
                case "select"://select(A,(A>5)or(B>C))
                    String tableName = commandContent.split(",")[0];
                    String conditions = commandContent.split(",")[1];
                    this.tables.put(targetName,this.tables.get(tableName).select(conditions));
                    break;
                case "inputfromfile":
                    this.tables.put(targetName,new Table(commandContent));
                    break;
                case "projection":
                    args = commandContent.split(",");

                    Table t = this.tables.get(args[0]);
                    List<String> rows = new ArrayList<>();
                    for(int i=1;i<args.length;i++){
                        rows.add(args[i]);
                    }
                    this.tables.put(targetName,t.projection(rows));
                    break;
                case "avg":
                    args = commandContent.split(",");
                    Table avgSourceTable = this.tables.get(args[0]);
                    this.tables.put(targetName,avgSourceTable.avg(args[1]));
                    break;
                case "sumgroup":
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
                    args = commandContent.split(",");
                    this.tables.put(targetName,this.join(args[2]));
                    break;
                case "sort":
                    args = commandContent.split(",");
                    List<String> sortRows =new ArrayList<>();
                    for(int i=1;i<args.length;i++){
                        sortRows.add(args[i]);
                    }
                    this.tables.put(targetName,this.tables.get(args[0]).sort(sortRows));
                    break;
                case "movavg":
                    args = commandContent.split(",");
                    this.tables.put(targetName,this.tables.get(args[0]).movAvg(args[1],Integer.valueOf(args[2])));
                    break;
                case "movsum":
                    args = commandContent.split(",");
                    this.tables.put(targetName,this.tables.get(args[0]).movSum(args[1],Integer.valueOf(args[2])));
                    break;
                case "concat":
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
                    args =commandContent.split(",");
                    this.tables.get(args[0]).generateIndex(Table.BTREE,args[1]);
                    break;
                case "hash":
                    args =commandContent.split(",");
                    this.tables.get(args[0]).generateIndex(Table.HASH,args[1]);
                    break;
                case "outputtofile":
                    args =commandContent.split(",");
                    this.tables.get(args[0]).toString("|");
                    break;

            }
        }
    }
    public Table join(String condition){
        condition= condition.toLowerCase().trim();
        String r1 = condition.split(">|<|=|<=|>=|!=")[0];
        String r2 = condition.split(">|<|=|<=|>=|!=")[1];
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
        int flag=0;
        if(condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)=([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){//A.B=C.D
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
                    if(condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)>([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){
                        satisfied = record1.get(joinRow1) > record2.get(joinRow2);
                    }
                    else if(condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)=([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){
                        satisfied = record1.get(joinRow1) == record2.get(joinRow2);
                    }
                    else if(condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)<([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){
                        satisfied = record1.get(joinRow1) < record2.get(joinRow2);
                    }
                    else if(condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)>=([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){
                        satisfied = record1.get(joinRow1) >= record2.get(joinRow2);
                    }
                    else if(condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)<=([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){
                        satisfied = record1.get(joinRow1) <= record2.get(joinRow2);
                    }
                    else if(condition.matches("([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)!=([A-z]([A-z]|[0-9]|_)*)\\.([A-z]([A-z]|[0-9]|_)*)")){
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

