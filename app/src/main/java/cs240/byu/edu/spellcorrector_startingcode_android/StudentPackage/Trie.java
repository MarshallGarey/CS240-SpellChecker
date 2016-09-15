package cs240.byu.edu.spellcorrector_startingcode_android.StudentPackage;

/**
 * Created by Marshall Garey on 9/15/2016.
 * A Trie is a space-efficient dictionary, where each node represents a letter in a word
 * A node's frequency is the number of times the word that node represents shows up in the Trie
 * The word is constructed by the nodes that lead up to the node with a frequency of 1 or greater
 */
public class Trie implements ITrie {
    private int wordCount;
    private int nodeCount;
    private final int NUM_CHILDREN = 26;
    public Node root;

    // constructor
    public Trie() {
        root = new Node(); // a trie is an array of nodes
        wordCount = 0; // no words yet
        nodeCount = 1; // root node counts as a node
    }

    // interface methods:

    @Override
    public void add(String word) {
        int word_i = 0;
        boolean isNewWord = false;
        isNewWord = recursiveAdd(isNewWord, root, word, word_i);
        if (isNewWord == true) {
            wordCount++;
        }
    }

    private boolean recursiveAdd(boolean isNewWord, Node n, String word, int word_i) {
        // base case
        if (word_i == word.length()) {
            n.frequency++;
            if (n.frequency == 1) { // is this a new word?
                isNewWord = true;
            }
            return isNewWord;
        }
        // take care of current character:
        else {
            // if node doesn't exist, create a new node and increment node count
            if (!nodeExists(word.charAt(word_i), n)) {
                n.nodes[word.charAt(word_i)-'a'] = new Node(word.charAt(word_i), n);
                nodeCount++;
                isNewWord = true;
            }
            word_i++;
            isNewWord = recursiveAdd(isNewWord, n.nodes[word.charAt(word_i-1)-'a'], word, word_i);
        }
        return isNewWord;
    }

    @Override
    public Node find(String word) {
        Node found = null;
        int word_i = 0;
        found = recursiveFind(found, root, word, word_i);

//		String str = "";
//		if (found != null) {
//			str = nodeToString(found);
//		}
//		System.out.println(str);

        return found;
    }

    private Node recursiveFind(Node found, Node n, String word, int word_i) {
        // base cases:
        // reached end of word
        if (word_i == word.length()) {
            // does the node exist?
            if (n == null) {
                return n;
            }
            // is frequency of current node > 0?
            else if (n.frequency > 0) {
                return n; // yes, word exists
            }
            else { // no, the word doesn't exist
                return null;
            }
        }
        // child node doesn't exist - word not found in Trie
        else if (!nodeExists(word.charAt(word_i),n)) {
            return null;
        }
        // go on to next character
        else {
            word_i++;
            found = recursiveFind(found, n.nodes[word.charAt(word_i-1)-'a'], word, word_i);
        }
        return found;
    }

    @Override
    public int getWordCount() {
        return wordCount;
    }

    @Override
    public int getNodeCount() {
        return nodeCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb = recursiveToString(sb, root);
        return sb.toString();

    }

    private StringBuilder recursiveToString(StringBuilder sb, Node n) {
        // base case: if n is null
        if (n == null) {
            return sb;
        }
        // base case: if all 26 children of n are null
        else {
            // if the node frequency is greater than zero, then this is a word
            if (n.frequency > 0 ) {
                sb.append(nodeToString(n) + "\n");
            }
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (n.nodes[i] != null) {
                    recursiveToString(sb, n.nodes[i]); // this child isn't null, recursively search
                }
            }
        }
        return sb;
    }

    @Override
    public int hashCode() {
        // 2 prime numbers * (nodes + words)
        return 827*31*(nodeCount+wordCount);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if ((o instanceof Trie)) {
                Trie other = (Trie) o;
                if (other.hashCode() != this.hashCode()) {
                    return false;
                }
                else if (other.wordCount != this.wordCount) {
                    return false;
                }
                else {
                    return recursiveEquals(other.root, this.root);
                }
            }
            else {
                return false;
            }
        }
        return false;
    }

    private boolean recursiveEquals(Node otherNode, Node thisNode) {
        for (int i = 0; i < NUM_CHILDREN; i++) {
            // both children nodes are null, continue to next letter
            Node a = otherNode.nodes[i];
            Node b = thisNode.nodes[i];
            if (a == null && b == null) {
                continue;
            }
            // one child is null, this other isn't: return false
            else if ((a == null && b != null) || (a != null && b == null)) {
                return false;
            }
            // both children exist: check node frequencies
            else if (a.frequency != b.frequency) {
                return false;
            }
            // nodes are the same: recursively search
            else {
                boolean isEqual = recursiveEquals(a, b);
                if (!isEqual) {
                    return false;
                }
            }
        }
        return true;
    }

    // my own methods:
    private boolean nodeExists(char c, Node n) {
//		System.out.println(c);
        if (n.nodes[c-'a'] != null) { // the node exists
            return true;
        }
        else {
            return false;
        }
    }

    public String nodeToString(Node n) {
        Node temp = n;
        String result = "";
        StringBuilder sb = new StringBuilder("");
        while (temp.parent != null) {
            sb.insert(0, temp.name);
            temp = temp.parent;
        }
        result = sb.toString();
        return result;
    }

    // implement the Node class
    public class Node implements ITrie.INode {
        private Node[] nodes;
        private Node parent;
        private int frequency;
        private char name;

        // constructor
        Node() {
            nodes = new Node[NUM_CHILDREN];
            frequency = 0;
            parent = null;
            name = 0;
        }

        Node(char nodeName, Node parentNode) {
            nodes = new Node[NUM_CHILDREN];
            frequency = 0;
            parent = parentNode;
            name = nodeName;
        }

        @Override
        public int getValue() {
            return frequency;
        }
    }
}
