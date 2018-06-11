package LLParser;

import java.util.ArrayList;

/**
 * Created by Phoebe on 2018-6-10.
 */
public class TreeNode {
    private String name;
    private ArrayList<TreeNode> children = new ArrayList<TreeNode>();
    private int value;

    public TreeNode(String name) {
        this.name = name;
        this.value = 1;
    }

    //在头部添加
    public void addChild(TreeNode childNode) {
        children.add(0, childNode);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<TreeNode> children) {
        this.children = children;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}