package robdd;

import bool.*;

import java.util.LinkedList;

public class ROBDD {


    // Attribute
    private String expression;
    private LinkedList<Node> nodes;
    private BooleanExpression function;
    private String[] variables;
    // Für Synthese
    LinkedList<Node> prevLevel;
    LinkedList<Node> currLevel;
    
    String prevVarName;
    String currVarName;

    Node currentParent;



    // Konstructor
    public ROBDD(String[] variables, String expression){
        // Aussage abspeichern
        this.expression = expression;
        this.variables = variables;
        // Platz für Knoten schaffen
        this.nodes = new LinkedList<>();
        Polynom function = new Polynom(expression, this.variables);
        // Auf Komplementierende Monome testen
        if(function.hasComplementingMonomes()){
            // Falls solche Vorliegen, ist die Funktion eine konstante 1
            this.function = new Constant(true);
        } else {
            // Ansonsten Funktion vereinfachen
            function.normalizeWithQuineMcCluskey();
        }
        this.function = function;
        // Variablenordnung merken
        this.variables = variables;
    }


    // Private Methods
    private void evalCofactorsForCurrentLevel(){
        // Über alle Nodes aus der vorherigen Ebene iterieren
        for(int j = 0; j < this.prevLevel.size(); j++){
            // Aktuell zu untersuchende Node
            this.currentParent = this.prevLevel.get(j);
            // Kofaktoren mit aktuellem Variablennamen berechnen
            this.evalCofactor(true);
            this.evalCofactor(false);
        }
    }

    private void evalCofactor(boolean high){
        BooleanExpression function = this.currentParent.function.evalWithCofactor(this.prevVarName, high);
        this.createOrConnectChild(function, high);
    }

    private void createOrConnectChild(BooleanExpression function, boolean high){
        // Variablen
        Node compareNode;
        boolean check;
        // Es wird zunächst das child erstellt
        Node newNode = new Node(function, this.currVarName);
        // Es wird überprüft, ob die Node bereits existiert
        check = false;
        for (int k = 0; k < this.nodes.size(); k++){
            compareNode = this.nodes.get(k);
            if (compareNode.equals(newNode)){
                // Falls sie bereits existiert, auf die aktuelle Ebene ziehen und als child einfügen
                compareNode.varName = this.currVarName;
                this.insertChild(compareNode, high);
                check = true;
                if (!this.currLevel.contains(compareNode))
                    // Falls sie noch nicht auf der aktuellen Ebene existiert, einfügen. Ansonsten Duplikat
                    this.currLevel.add(compareNode);
            }
        }
        if (!check) {
            // Falls kein entsprechender Knoten gefunden wurde, bei einfügen
            this.insertChild(newNode, high);
            currLevel.add(newNode);
            this.nodes.add(newNode);
        }
    }

    private void insertChild(Node child, boolean high){
        if(high) {
            this.currentParent.highChild = child;
            return;
        }
        this.currentParent.lowChild = child;
    }

    // Public API
    public void synthetize(){
        
        // Listen zum Merken
        this.currLevel = new LinkedList<>();

        // Wurzel erstellen
        this.currVarName = this.variables[0];
        Node newNode = new Node(this.function,currVarName);
        this.currLevel.add(newNode);
        this.nodes.add(newNode);

        // Über die Variablen in der vorgegebenen Reihenfolge iterieren
        for (int i = 1; i < this.variables.length; i++){
            // 1 Level weitergehen
            this.prevLevel = this.currLevel;
            this.currLevel = new LinkedList<>();
            // Aktuellen Variablennamen bestimmen
            this.prevVarName = this.currVarName;
            this.currVarName = this.variables[i].trim();
            // Kofaktoren auswerten
            this.evalCofactorsForCurrentLevel();
        }
        // 1 Level weitergehen
        this.prevLevel = this.currLevel;
        this.currLevel = new LinkedList<>();
        // Aktuellen Variablennamen bestimmen
        this.prevVarName = this.currVarName;
        this.currVarName = "blatt";
        // Kofaktoren auswerten
        this.evalCofactorsForCurrentLevel();

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
        var = "blatt";
        // Überschrift anhängen
        str += "\n\n\n######## Neue Ebene: \"" + var + "\" ########";
        // Durch die Knoten Iterieren
        for(int j = 0; j < this.nodes.size(); j++){
            node = this.nodes.get(j);
            // Wenn der Knoten auf der aktuellen Ebene liegt
            if(node.varName.equals(var)){
                str += node.toStringWithIndexWithoutChildren(j);
            }
        }

        // Ausgabe
        return str;
    }
}