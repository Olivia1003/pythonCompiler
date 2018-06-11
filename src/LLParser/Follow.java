/*
 * Create by Qin Yuxin
 */

//package FirstFollow;
package LLParser;

import java.util.*;

public class Follow {
    private Map<String, Set<String>> first = null;
    private Map<String, Set<String>> follow = new TreeMap<String, Set<String>>();
    private Map<String, String[]> mp = null;
    public Follow(Map<String, String[]> mp, Map<String, Set<String>> first) {
        super();
        this.first = first;
        this.mp = mp;
    }

    public Map<String, Set<String>> getFollowSet(){
        return follow;
    }

    //S->AB ( follow(A) = first(B) U ... )
    private void addFirst(String curNode, String nextNode){
        Set<String> st = new TreeSet<String>();
        if(mp.containsKey(nextNode)) {
            st.addAll(first.get(nextNode));
            st.remove("#");
        }
        else if(nextNode!="#")
            st.add(nextNode);
        if(follow.get(curNode) == null){
            follow.put(curNode,st);
            //System.out.println("add " + nextNode +"'s first:" + st.toString() +" to " + curNode);
        }else{
            follow.get(curNode).addAll(st);
            //System.out.println("add "+ nextNode +"'s first:"+st.toString()+" to "+curNode);
        }
    }

    private void combineNode(String curNode,String node2){  //follow(curNode) = follow(node2) U ...
        if(follow.get(node2) == null){ //如果follow(node2)为空 先算follow(node2)
            findFollow(node2);
        }
        if(follow.get(curNode) == null){
            Set<String> st = new TreeSet<String>();
            st.addAll(follow.get(node2));
            follow.put(curNode,st);
            //System.out.println("add " + node2 +"'s follow:" + st.toString() +" to " + curNode);
        }else{
            follow.get(curNode).addAll(follow.get(node2));
            //System.out.println("add " + node2 +"'s follow:" + follow.get(node2) +" to " + curNode);
        }
    }

    private void findFollow(String curNode){
        Set<String> st = null;
        for(String leftNode : mp.keySet()){
            String rightNodes[] = mp.get(leftNode);
            for (String rightNode : rightNodes) {
                //System.out.println("rightNode:" + rightNode);
                String[] words = rightNode.split(" ");
                int index = -1;
                for(int i=0; i<words.length;i++)
                    if(words[i].equals(curNode)) {
                        index = i;
                        break;
                    }
//                System.out.println("curNode: " + curNode);
//                System.out.println("leftNode: " + leftNode);
//                System.out.println("index: " + index);
                while (index >= 0) { //如果在rightNode这个产生式中存在curNode
                    int nextIndex = index + 1;
                    index = index + 1;
                    if (index == words.length) {//curNode在产生式中是末尾的非终结点, A->..B
                        combineNode(curNode, leftNode);
                    } else { //curNode后面还有其他人
                        String nextNode = words[index]; //nextNode: curNode的后一个Node
                        //System.out.println("nextNode: " + nextNode);
                        if (mp.containsKey(nextNode)) {  //如果nextNode是非终结符
                            addFirst(curNode, nextNode);
                            int tmpNextIndex = index;
                            String tmpNext = nextNode;

                            //连在后面的话把把之后的first加进去
                            while (mp.containsKey(tmpNext) && first.get(tmpNext).contains("#") && tmpNextIndex != words.length - 1) {
                                tmpNextIndex++;
                                tmpNext = words[tmpNextIndex];
                                addFirst(curNode, tmpNext);
                            }
                            tmpNextIndex = index;
                            tmpNext = nextNode;

                            //考虑nextNode后面的字符都可能为空，那要算上leftNode的follow集
                            while (mp.containsKey(tmpNext) && first.get(tmpNext).contains("#")) {
                                if (tmpNextIndex == words.length - 1) { //tmpNext在语句的末尾 且tmpNext可能为空
                                    combineNode(curNode, leftNode);
                                    break;
                                } else {
                                    tmpNextIndex++;
                                    tmpNext = words[tmpNextIndex];
                                }
                            }
                        } else
                            addFirst(curNode, nextNode);
                    }
                    index = -1;
                    for(int i=nextIndex; i<words.length;i++)
                        if(words[i].equals(curNode)) {
                            index = i;
                            break;
                        }
                }
            }
        }
    }

    public void followKernalCode(){
        boolean flag = true;
        for(String leftNode : mp.keySet()){
            if(flag){
                Set<String> st = new TreeSet<String>();
                st.add("$");
                follow.put(leftNode, st);
                flag = false;
            }
            findFollow(leftNode);
        }
    }
}
