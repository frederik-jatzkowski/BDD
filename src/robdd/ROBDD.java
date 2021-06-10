package robdd;

import bool.*;

import java.util.LinkedList;

public class ROBDD {


    // Attribute
    private String expression;
    private LinkedList<Node> nodes;
    private BooleanExpression function;
    private String[] variables;


    // Konstructor
    public ROBDD(String[] variables, String expression){
        // Aussage abspeichern
        this.expression = expression;
        // Platz für Knoten schaffen
        this.nodes = new LinkedList<>();
        Polynom function = new Polynom(expression);
        // Auf Komplementierende Monome testen
        if(function.hasComplementingMonomes()){
            // Falls solche Vorliegen, ist die Funktion eine konstante 1
            this.function = new Constant(true);
        } else {
            // Ansonsten Funktion vereinfachen
            function.simplyfy();
        }
        this.function = function;
        // Variablenordnung merken
        this.variables = variables;
    }


    // Private Methods

    // Public API
    public void synthetize(){
        
        // Listen zum Merken
        LinkedList<Node> prevLevel;
        LinkedList<Node> currLevel = new LinkedList<>();

        // Wurzel erstellen
        String prevVarName;
        String currVarName = this.variables[0].trim();
        Node newNode = new Node(this.function,currVarName);
        currLevel.add(newNode);
        this.nodes.add(newNode);

        // Weitere Variablen
        Node currentParent;
        Node compareNode;
        boolean check;
        BooleanExpression positiveCofactor;
        BooleanExpression negativeCofactor;

        // Über die Variablen in der vorgegebenen Reihenfolge iterieren
        for (int i = 1; i < variables.length; i++){
            // 1 Level weitergehen
            prevLevel = currLevel;
            currLevel = new LinkedList<>();
            // Aktuellen Variablennamen bestimmen
            prevVarName = currVarName;
            currVarName = this.variables[i].trim();
            // Über alle Nodes aus der vorherigen Ebene iterieren
            for(int j = 0; j < prevLevel.size(); j++){
                // Aktuell zu untersuchende Node
                currentParent = prevLevel.get(j);
                // Kofaktoren mit aktuellem Variablennamen berechnen
                positiveCofactor = currentParent.function.evalWithCofactor(prevVarName, true);
                negativeCofactor = currentParent.function.evalWithCofactor(prevVarName, false);

                // Es wird zunächst das highChild erstellt
                newNode = new Node(positiveCofactor, currVarName);
                // Es wird überprüft, ob die Node bereits existiert
                check = false;
                for (int k = 0; k < this.nodes.size(); k++){
                    compareNode = this.nodes.get(k);
                    if (compareNode.equals(newNode)){
                        // Falls sie bereits existiert, auf die aktuelle Ebene ziehen und als highChild einfügen
                        compareNode.varName = currVarName;
                        currentParent.highChild = compareNode;
                        check = true;
                        if (!currLevel.contains(compareNode))
                            // Falls sie noch nicht auf der aktuellen Ebene existiert, einfügen. Ansonsten Duplikat
                            currLevel.add(compareNode);
                    }
                }
                if (!check) {
                    // Falls kein entsprechender Knoten gefunden wurde, einfügen
                    currentParent.highChild = newNode;
                    currLevel.add(newNode);
                    this.nodes.add(newNode);
                }
                    

                // Dann wird das lowChild erstellt
                newNode = new Node(negativeCofactor, currVarName);
                // Es wird überprüft, ob die Node bereits existiert
                check = false;
                for (int k = 0; k < this.nodes.size(); k++){
                    compareNode = this.nodes.get(k);
                    if (compareNode.equals(newNode)){
                        // Falls sie bereits existiert, auf die aktuelle Ebene ziehen und als lowChild einfügen
                        compareNode.varName = currVarName;
                        currentParent.lowChild = compareNode;
                        check = true;
                        if (!currLevel.contains(compareNode))
                            // Falls sie noch nicht auf der aktuellen Ebene existiert, einfügen. Ansonsten Duplikat
                            currLevel.add(compareNode);
                    }
                }
                if (!check) {
                    // Falls kein entsprechender Knoten gefunden wurde, einfügen
                    currentParent.lowChild = newNode;
                    currLevel.add(newNode);
                    this.nodes.add(newNode);
                }
            }
        }

    }

    @Override
    public String toString(){
        String str = "";
        String var;
        Node node;
        // Überschrift
        str += "\n########## Reduced Ordered Binary Decision Diagram ##########\n\n";
        str += "Eingabeausdruck: " + this.expression + "\n";
        str += "Vorgegebene Variablenordnung: " + (String.join(" < ",this.variables));
        // Über alle Variablen iterieren
        for(int i = 0; i < this.variables.length; i++){
            var = this.variables[i].trim();
            // Überschrift anhängen
            str += "\n\n\n######## Neue Ebene: \"" + var + "\" ########";
            // Durch die Knoten Iterieren
            for(int j = 0; j < this.nodes.size(); j++){
                node = this.nodes.get(j);
                // Wenn der Knoten auf der aktuellen Ebene liegt
                if(node.varName.equals(var)){
                    str += node.toStringWithIndex(
                        j,
                        this.nodes.indexOf(node.highChild),
                        this.nodes.indexOf(node.lowChild)
                    );
                }
            }
        }

        // Ausgabe
        return str;
    }
}