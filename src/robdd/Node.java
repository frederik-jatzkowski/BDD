package robdd;

import bool.BooleanExpression;
import bool.Constant;

public class Node {
    
    // Attribute
    // Meta
    String varName;
    BooleanExpression function;

    // Children
    Node lowChild;
    Node highChild;

    // Konstruktor
    public Node(BooleanExpression function, String varName){
        this.function = function;
        this.varName = varName;
    }


    // Public API

    public boolean isLeaf(){
        if (function instanceof Constant)
            return true;
        return false;
    }

    @Override
    public boolean equals(Object o){
        // Guards
        if(o == null) return false;
        if(!(o instanceof Node)) return false;
        // Vergleich der Werte
        Node o2 = (Node) o;
        if(this.function.equals(o2.function))
            return true;
        return false;
    }

    public String toStringWithIndex(int index, int highIndex, int lowIndex){
        String str = "\n\n    #### Knoten f" + index + " ####\n";
        str += "    Markierung: " + this.varName + "\n";
        str += "    Beschriebene Funktion: f" + index + " := " + this.function.toString() + "\n";
        if (highIndex >= 0){
            str += "    HighChild: f" + highIndex + "\n";
        } else {
            str += "    HighChild: keines\n";
        }
        if (lowIndex >= 0){
            str += "    LowChild: f" + lowIndex + "\n";
        } else {
            str += "    LowChild: keines\n";
        }
        str += "    ####";
        return str;
    }

}
