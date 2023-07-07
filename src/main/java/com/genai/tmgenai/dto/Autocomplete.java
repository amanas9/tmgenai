package com.genai.tmgenai.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Autocomplete {

    public int getSize() {
        return root.getSize();
    }

    TrieNode root;

    public Autocomplete() {
        root = new TrieNode();
    }

    public void insert(String word, AutoCompleteDetails autoCompleteDetails) {
        TrieNode current = root;
        for (char ch : word.toLowerCase().toCharArray()) {
            current.getChildren().putIfAbsent(ch, new TrieNode(autoCompleteDetails));
            current = current.getChildren().get(ch);
        }
        current.setEndOfWord(true);
    }

    public List<AutoCompleteDetails> giveSuggestions(String prefix, AutoCompleteDetails.VERTICAL vertical) {
        TrieNode prefixNode = findNode(prefix.toLowerCase());
        List<AutoCompleteDetails> suggestions = new ArrayList<>();
        collectWords(prefixNode, new StringBuilder(prefix), suggestions, vertical);
        return suggestions;
    }

    private TrieNode findNode(String prefix) {
        TrieNode current = root;
        for (char ch : prefix.toCharArray()) {
            if (!current.getChildren().containsKey(Character.toLowerCase(ch))) {
                return null;
            }
            current = current.getChildren().get(Character.toLowerCase(ch));
        }
        return current;
    }

    private void collectWords(TrieNode node, StringBuilder prefix, List<AutoCompleteDetails> suggestions, AutoCompleteDetails.VERTICAL vertical) {
        if (node == null) {
            return;
        }

        if (node.isEndOfWord() && node.getAutoCompleteDetails().vertical == vertical) {
            suggestions.add(node.getAutoCompleteDetails());
        }

        for (char ch : node.getChildren().keySet()) {
            prefix.append(ch);
            collectWords(node.getChildren().get(ch), prefix, suggestions, vertical);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }
}

class TrieNode {
    private Map<Character, TrieNode> children;
    private boolean isEndOfWord;
    private AutoCompleteDetails autoCompleteDetails;

    public AutoCompleteDetails getAutoCompleteDetails() {
        return autoCompleteDetails;
    }

    public TrieNode()
    {
        this.children = new HashMap<>();
        this.isEndOfWord = false;
    }
    public TrieNode(AutoCompleteDetails autoCompleteDetails) {
        this.children = new HashMap<>();
        this.isEndOfWord = false;
        this.autoCompleteDetails = autoCompleteDetails;
    }

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isEndOfWord() {
        return isEndOfWord;
    }

    public void setEndOfWord(boolean endOfWord) {
        isEndOfWord = endOfWord;
    }

    public int getSize() {
        int size = 0;
        for (TrieNode child : children.values()) {
            size += child.getSize();
        }
        return isEndOfWord ? size + 1 : size;
    }

}

