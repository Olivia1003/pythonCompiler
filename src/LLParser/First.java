/*
 * Create by Qin Yuxin
 */

//package FirstFollow;
package LLParser;

import java.util.*;

public class First {
    private Map<String, Set<String>> first = new TreeMap<String, Set<String>>(); //first集
    private Map<String, String[]> mp;

    First(Map<String, String[]> mp) {
        super();
        this.mp = mp;
    }

    public Map<String, Set<String>> getFirstSet(){
        return first;
    }

    private Set<String> findFirst(String curNode, String[] rightNodes){
        if(first.containsKey(curNode))
            return first.get(curNode);
        Set<String> st = new TreeSet<String>();
        for(String rightNode : rightNodes){
            String[] words = rightNode.split(" ");
            for (String nextNode : words){
                if(!mp.containsKey(nextNode)){      //nextNode是终结符
                    st.add(nextNode);
                    break;
                }
                else{       //nextNode是非终结符
                    Set<String> tmpSt = findFirst(nextNode, mp.get(nextNode));
                    st.addAll(tmpSt);
                    if (!tmpSt.contains("#"))
                        break;
                }
            }
        }
        first.put(curNode, st);
        return st;
    }

    //
    public String firstKernealCode(){
        String content = "";
        for(String leftNode : mp.keySet()){     //mp.keySet:[S,AA,B](所有非终结符)
            String[] rightNodes = mp.get(leftNode); //rightNodes = [AA B c] , [a,#] ,[]
            findFirst(leftNode, rightNodes);  //放入first集合
        }

        //打印first集合
        System.out.println("First集合如下:");
        for(Map.Entry<String, Set<String>> entry : first.entrySet()){
            content += entry.getKey() + "  :  " + entry.getValue() + "\n";
            System.out.println(entry.getKey() + "  :  " + entry.getValue());
        }
        return content;
    }
}
