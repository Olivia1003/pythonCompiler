package LLParser;//import com.sun.java_cup.internal.terminal;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.*;


//note:
//parse table 的列中包含结束符 $，所以要手动加上一列
//terminalCharacters不包括结束符 $

public class ParsingTable {
    ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Integer> rowStrInt = new TreeMap<String, Integer>();//分析表行 字符对应数字
    private Map<String, Integer> colStrInt = new TreeMap<String, Integer>();//分析表列 字符对应数字
    private Integer rowNodeCnt = 0, colNodeCnt = 0;
    private Map<String, Set<String>> first = null;
    private Map<String, Set<String>> follow = null;
    private Map<String, String[]> ruleMap = null;
    private String[][] parseTable = new String[100][];
    private Set<String> terminalCharacters = new TreeSet<String>();//终结符集合

    private TreeNode treeRootNode = null;
    private ArrayList<String> usedProductionList = new ArrayList<String>();

    public ParsingTable(Map<String, Set<String>> first,
                        Map<String, Set<String>> follow,
                        Map<String, String[]> ruleMap) {
        this.first = first;
        this.follow = follow;
        this.ruleMap = ruleMap;
        init();
        generateTable();
    }


    public void init() {
        //init str->int map,terminalCharacters
        for (String leftNode : ruleMap.keySet()) {
            if (!rowStrInt.containsKey(leftNode)) {
                rowStrInt.put(leftNode, rowNodeCnt++);
            }
            String[] rightNodeList = ruleMap.get(leftNode);
            for (String rightNode : rightNodeList) {
                String[] wordList = rightNode.split(" ");

                for (String word : wordList) {
                    if ((!ruleMap.containsKey(word)) && (!word.equals("#")) && (!colStrInt.containsKey(word))) {
                        colStrInt.put(word, colNodeCnt++);
                        terminalCharacters.add(word);
                    }
                }
            }


        }
        colStrInt.put("$", colNodeCnt++);//parse table 的列中包含结束符 $，所以要手动加上一列
    }

    public void generateTable() {
        System.out.println("\nstart generate table........................................");
        for (String leftNode : ruleMap.keySet()) {
//            System.out.println("leftNode:"+leftNode);

            String[] rightNodeList = ruleMap.get(leftNode);
            for (String rightNode : rightNodeList) {//遍历生成式leftNode->rightNode
//                System.out.println("rightNode:"+rightNode);

                //生成rightNode的first集
                Set<String> productionFirstSet = new TreeSet<String>();//产生式对应first集
                String[] wordList = rightNode.split(" ");
                int index = 0;
                for (String word : wordList) {//遍历rightNode
                    index++;
                    if (terminalCharacters.contains(word)) {//终止符
                        productionFirstSet.add(word);
                        break;
                    } else if (!word.equals("#")) {//非终止符
                        Set<String> firstSet = first.get(word);//该非终止符的first集
                        productionFirstSet.addAll(firstSet);
                        if (firstSet.contains("#")) {//包含空，继续往下找
                            if ((index + 1) < rightNode.length()) {//除了最后一个ch，否则空不包含在productionFirstSet
                                productionFirstSet.remove("#");
                            }
                        } else {
                            break;
                        }
                    } else {//空字符
                        productionFirstSet.add("#");
                    }
                }
                //遍历rightNode的first集
                for (String firstWord : productionFirstSet) {
//                    System.out.println("firstWord:"+firstWord);

                    if (terminalCharacters.contains(firstWord)) {//将每个终止符加入table
                        addGridNode(leftNode, firstWord + "", leftNode + "->" + rightNode);//加入parse table
                    }
                    if (firstWord.equals("#")) {//first(rightNode)集包含空，查看follow(leftNode)集
                        Set<String> leftNodeFollowSet = follow.get(leftNode);
                        for (String followNode : leftNodeFollowSet) {
                            addGridNode(leftNode, followNode + "", leftNode + "->" + rightNode);
                        }
                    }
                }
            }
        }
    }

    public void printParseTable() {
        Formatter formatter = new Formatter(System.out);
        int formatLen=25;
        int maxLen=24;
        System.out.println("\nprint table,size:" + rowNodeCnt + " " + colNodeCnt+"------------------------------");
        formatter.format("%-"+formatLen+"."+maxLen+"s","");
        for (int j = 0; j < colNodeCnt; j++) {
            formatter.format("%-"+formatLen+"."+maxLen+"s",getColIntStr(j));
        }
        formatter.format("\n");
        for (int i = 0; i < rowNodeCnt; i++) {
            formatter.format("%-"+formatLen+"."+maxLen+"s",getRowIntStr(i));
            for (int j = 0; j < colNodeCnt; j++) {
                formatter.format("%-"+formatLen+"."+maxLen+"s", parseTable[i][j]);
            }
            formatter.format("\n");
        }
        System.out.println("print table over -------------------------------------------");
    }

    public void predictParsing(ArrayList<String> inputList, String rootName) {
        usedProductionList.clear();
        Stack<String> stack = new Stack<String>();
        System.out.println("\nstart parsing for input:" + inputList);
        stack.push("$");
        stack.push(rootName);
        while (!inputList.isEmpty()) {
            String stackItem = stack.peek();
            String inputItem = inputList.get(0);

            if (stackItem.equals(inputItem)) {
                if (stackItem.equals("$")) {//所有匹配成功
                    break;
                } else {//相同的终止符，抵消
                    stack.pop();
                    inputList.remove(0);
                    System.out.println("-------------reduce item:" + stackItem);
                }
            } else {//非终止符
//                System.out.println(stackItem+" "+rowStrInt.get(stackItem));
//                System.out.println(inputItem+" "+colStrInt.get(inputItem));

                String rule = parseTable[rowStrInt.get(stackItem)][colStrInt.get(inputItem)];

                if (rule != null) {
                    System.out.println("-------------output rule:" + rule);
                    usedProductionList.add(rule);//记录下用过的产生式，方便之后生成tree
                    stack.pop();//取出stack顶部元素
                    String[] split1 = rule.split("->");
                    String[] split2 = split1[1].split(" ");
                    for (int i = split2.length - 1; i >= 0; i--) {//倒着放入stack
                        if (!split2[i].equals("#")) {
                            stack.push(split2[i]);
                        }
                    }
                } else {//找不到对应的rule，报错
                    System.out.println("error:predict parsing wrong,rule null of row:"+stackItem+",col:"+inputItem);
                    return;
                }
            }
        }
        System.out.println("predict parsing success");
//        getParseTreeData(rootName);
    }

    public void getParseTreeData(String rootName) {
        treeRootNode = new TreeNode(rootName);//根元素
        Stack<TreeNode> nodeStack = new Stack<TreeNode>();//每次栈顶都是应用当前rule的元素
        nodeStack.push(treeRootNode);
        for (int cur = 0; cur < usedProductionList.size() && !nodeStack.empty(); cur++) {
            String rule = usedProductionList.get(cur);
            String[] split1 = rule.split("->");
            String[] split2 = split1[1].split(" ");
            TreeNode topNode = nodeStack.pop();
            if (!split1[0].equals(topNode.getName())) {
                System.out.println("get parse tree wrong");
                return;
            }
//            System.out.println("add children of node:" + topNode.getName());
            for (int i = split2.length - 1; i >= 0; i--) {
                TreeNode newNode = new TreeNode(split2[i]);
                topNode.addChild(newNode);
                if (!terminalCharacters.contains(newNode.getName()) && !newNode.getName().equals("#")) {
                    nodeStack.push(newNode);
                }
//                System.out.println("add child:" + newNode.getName());
            }
        }
//        System.out.println("parse tree:" + treeRootNode);

        try {
            System.out.println(objectMapper.writeValueAsString(treeRootNode));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getRowIntStr(Integer num) {
        String key = "";
        for (Map.Entry<String, Integer> entry : rowStrInt.entrySet()) {
            if (num.equals(entry.getValue())) {
                key = entry.getKey();
            }
        }
        return key;
    }

    public String getColIntStr(Integer num) {
        String key = "";
        for (Map.Entry<String, Integer> entry : colStrInt.entrySet()) {
            if (num.equals(entry.getValue())) {
                key = entry.getKey();
            }
        }
        return key;
    }

    public void addGridNode(String rowStr, String colStr, String rule) {
        int rowNum = rowStrInt.get(rowStr);
        int colNum = colStrInt.get(colStr);
        if (parseTable[rowNum] == null) {
            parseTable[rowNum] = new String[colNodeCnt + 1];
        }
        if (parseTable[rowNum][colNum] == null) {
            parseTable[rowNum][colNum] = rule;
            System.out.println("add rule at "+rowNum+" "+colNum+" rule:"+rule);
        } else {
//            throw new Exception("add production rule error:multiple rules at row:" + rowStr + ",col:" + colStr+",rule:"+rule
//                    +" , already got rule:"+parseTable[rowNum][colNum]);
            System.err.println("error:multiple rules at row:" + rowStr + ",col:" + colStr+",rule:"+rule
                    +" , already got rule:"+parseTable[rowNum][colNum]);
            System.out.println("error:multiple rules at row:" + rowStr + ",col:" + colStr+",rule:"+rule
                    +" , already got rule:"+parseTable[rowNum][colNum]);
            parseTable[rowNum][colNum]=null;
        }
    }


    public static void main(String[] args) {
        Map<String, Set<String>> firstSet = new TreeMap<String, Set<String>>();
        Map<String, Set<String>> followSet = new TreeMap<String, Set<String>>();

//        String[] rightLinearGrammar = {
//                "Stmt->letter = ArithExpr StmtPrime |Ifstmt StmtPrime |Compoundstmt",
//                "StmtPrime->newline Stmt |#",
//                "Compoundstmt->{ Stmts }",
//                "Stmts->Stmt Stmts |#",
//                "Ifstmt->if LogicExpr : newline Compoundstmt Elifstmt Elsestmt",
//                "Elifstmt->elif LogicExpr : newline Compoundstmt Elifstmt |#",
//                "Elsestmt->else : Compoundstmt |#",
//                "LogicExpr->not LogicExprPrime |OrExpr",
//                "LogicExprPrime->OrExpr |( OrExpr )",
//                "OrExpr->AndExpr OrExprPrime",
//                "OrExprPrime->or AndExpr OrExprPrime |#",
//                "AndExpr->LogicAtom AndExprPrime",
//                "AndExprPrime->and LogicAtom AndExprPrime |#",
//                "LogicAtom->BoolExpr |( LogicExpr )",
//                "BoolExpr->ArithExpr Compop ArithExpr",
//                "Compop->< |> |<= |>= |== |!= |is IsPrime",
//                "IsPrime->not |#",
//                "ArithExpr->MultExpr ArithExprPrime",
//                "ArithExprPrime->+ MultExpr ArithExprPrime |- MultExpr ArithExprPrime |#",
//                "MultExpr->Atom MultExprPrime",
//                "MultExprPrime->* Atom MultExprPrime |/ Atom MultExprPrime |#",
//                "Atom->letter |number |( ArithExpr )"
//        };

        String[] rightLinearGrammar = {
                "Stmt->letter = ArithExpr StmtPrime |Ifstmt StmtPrime |Compoundstmt",
                "StmtPrime->newline Stmt |#",
                "Compoundstmt->{ Stmts }",
                "Stmts->Stmt Stmts |#",
                "Ifstmt->if LogicExpr : newline Compoundstmt Elifstmt Elsestmt",
                "Elifstmt->elif LogicExpr : newline Compoundstmt Elifstmt |#",
                "Elsestmt->else : Compoundstmt |#",
                "LogicExpr->not LogicExprPrime |OrExpr",
                "LogicExprPrime->( OrExpr )",
                "OrExpr->AndExpr OrExprPrime",
                "OrExprPrime->or AndExpr OrExprPrime |#",
                "AndExpr->LogicAtom AndExprPrime",
                "AndExprPrime->and LogicAtom AndExprPrime |#",
                "LogicAtom->BoolExpr |( LogicExpr )",
                "BoolExpr->ArithExpr Compop ArithExpr",
                "Compop->< |> |<= |>= |== |!= |is IsPrime",
                "IsPrime->not |#",
                "ArithExpr->MultExpr ArithExprPrime",
                "ArithExprPrime->+ MultExpr ArithExprPrime |- MultExpr ArithExprPrime |#",
                "MultExpr->Atom MultExprPrime",
                "MultExprPrime->* Atom MultExprPrime |/ Atom MultExprPrime |#",
                "Atom->letter |number |( ArithExpr )"
        };

        Map<String, String[]> ruleMap = new LinkedHashMap<String, String[]>();
        try {
            for (String aRightLinearGrammar : rightLinearGrammar) {
                String[] split1 = aRightLinearGrammar.split("->");
                String[] split2 = split1[1].split("\\|");
                ruleMap.put(split1[0], split2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("右线性文法错误!");
        }
        First first = new First(ruleMap);
        first.firstKernealCode();
        Follow follow = new Follow(ruleMap, first.getFirstSet());
        follow.followKernalCode();
        followSet = follow.getFollowSet(); //follow集合
        firstSet = first.getFirstSet();
        //打印Follow集合
        System.out.println("\nFollow 集合如下:");
        for (Map.Entry<String, Set<String>> entry : followSet.entrySet()) {
            System.out.println(entry.getKey() + "  :  " + entry.getValue());
        }

        ParsingTable parsingTable = new ParsingTable(firstSet, followSet, ruleMap);
        parsingTable.printParseTable();

        ArrayList<String> inputList = new ArrayList<String>();
//        inputList.add("letter");
//        inputList.add("=");
//        inputList.add("(");
//        inputList.add("letter");
//        inputList.add("+");
//        inputList.add("letter");
//        inputList.add(")");
//        inputList.add("*");
//        inputList.add("letter");
//        inputList.add("$");

        inputList.add("if");
        inputList.add("letter");
        inputList.add(">");
        inputList.add("letter");
        inputList.add(":");

        inputList.add("newline");
        inputList.add("{");

        inputList.add("letter");
        inputList.add("=");
        inputList.add("letter");
        inputList.add("}");
        inputList.add("$");


        parsingTable.predictParsing(inputList, "Stmt");
        parsingTable.getParseTreeData("Stmt");

    }

}
