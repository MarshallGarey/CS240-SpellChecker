package cs240.byu.edu.spellcorrector_startingcode_android.StudentPackage;

/**
 * Created by Marshall Garey on 9/15/2016.
 * This is a simple spell corrector which looks for the word in a dictionary with the find method
 * If the word isn't found, it looks for similar words in the dictionary with up two 2 changes
 * These different types of changes are:
 *    Insertion (an extra letter)
 *    Deletion (a missing letter)
 *    Alteration (a changed letter)
 *    Transposition (two swapped letters)
 * The dictionary is implemented using a Trie (see the Trie class)
 */

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class SpellCorrector implements ISpellCorrector {
    // variables
    public Trie trie;

    // constructor
    public SpellCorrector() {
        trie = new Trie();
    }

    /**
     * Tells this <code>ISpellCorrector</code> to use the given file as its dictionary
     * for generating suggestions.
     *
     * @param dictionaryFile File containing the words to be used
     * @throws IOException If the file cannot be read
     */
    @Override
    public void useDictionary(InputStreamReader dictionaryFile) throws IOException {
        // read input file and add all the words into the dictionary
        Scanner s = new Scanner(dictionaryFile);
        while (s.hasNext()) {
            trie.add(s.next().toLowerCase()); // add current word to dictionary
        }
        s.close();
    }

    @Override
    public String suggestSimilarWord(String inputWord) throws NoSimilarWordFoundException {
        String word = inputWord.toLowerCase();
        Trie.Node node = trie.find(word); // returns null if inputWord is not found
        if (node != null) { // node is not null, the word was successfully found
            return trie.nodeToString(node);
        }
        else { // word wasn't found, have to search for similar words
            String result = findSimilarWord(word);
            if (result == null) {
                throw new NoSimilarWordFoundException();
            }
            return result;
        }
    }

    private String findSimilarWord(String inputWord) {
        String result = null;
        Set<String> words1EditDistance = new HashSet<String>();
        Set<String> unfoundWords = new HashSet<String>();
        // get words 1 edit distance away, store them in words1EditDistance,
        // store the rest in unfoundWords.
        get1EditDistanceWords(inputWord, words1EditDistance, unfoundWords);
        // if words1EditDistance is not empty, then pick the best words
        if (words1EditDistance.size() > 0) {
            result = findBestWord(words1EditDistance);
        }
        else { // look for words 2 edit distances away
            Set<String> unusedSet = new HashSet<String>();
            for (String word : unfoundWords) {
                get1EditDistanceWords(word, words1EditDistance, unusedSet);
            }
            if (words1EditDistance.size() > 0) {
                result = findBestWord(words1EditDistance);
            }
        }
        return result;
    }

    private String findBestWord(Set<String> words1EditDistance) {
        // note - I only use this method if words1EditDistance is not empty.
        String result = null;
        // is there only one word?
        if (words1EditDistance.size() == 1) {
            result = words1EditDistance.iterator().next();
        }
        else {
            // find the word(s) with the most frequency:
            // tree set because it sorts alphabetically first
            TreeSet<String> highestFreqWords = new TreeSet<String>();
            highestFreqWords = findWordsWithHighestFrequency(words1EditDistance);
            // if there is more than one, then pick the one alphabetically first.
            result = highestFreqWords.iterator().next(); // picks alphabetically first word.
        }
        return result;
    }

    private TreeSet<String> findWordsWithHighestFrequency(Set<String> words1EditDistance) {
        TreeSet<String> highestFreqWords = new TreeSet<String>();
        int currentHighestFreq = 0;

        for (String word : words1EditDistance) {
            Trie.Node node = trie.find(word);
            int freq = node.getValue(); // frequency of current word
            if (freq > currentHighestFreq) {
                // set new highest frequency:
                currentHighestFreq = freq;
                // clear current set of words and add current word
                highestFreqWords = new TreeSet<String>();
                highestFreqWords.add(word);
            }
            else if (freq == currentHighestFreq) { // same frequency, add word to set
                highestFreqWords.add(word);
            }
        }

        return highestFreqWords;
    }

    private void get1EditDistanceWords(String inputWord, Set<String> words1EditDistance,
                                       Set<String> unfoundWords) {
        getDeletionDistanceWords(inputWord, words1EditDistance, unfoundWords);
//        PrintWriter f = null;
//        try {
//            f = new PrintWriter("editDistancesDebug.txt");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        f.println("\nBegin deletion:");
//        f.print(words1EditDistance.toString());
        getTranspositionWords(inputWord, words1EditDistance, unfoundWords);
//        f.println("\nBegin transposition: ");
//        f.print(words1EditDistance.toString());

        getAlterationWords(inputWord, words1EditDistance, unfoundWords);
//        f.println("\nBegin alteration: ");
//        f.print(words1EditDistance.toString());

        getInsertionWords(inputWord, words1EditDistance, unfoundWords);
//        f.println("\nBegin insertion: ");
//        f.print(words1EditDistance.toString());
//        f.close();
    }

    private void getInsertionWords(String inputWord, Set<String> words1EditDistance,
                                   Set<String> unfoundWords) {
        for (int i = 0; i <= inputWord.length(); i++) {
            for (int j = 0; j < 26; j++) {
                StringBuilder sb = new StringBuilder(inputWord);
                sb.insert(i, (char)('a'+j));
//				System.out.printf("word to find: %s\n", sb.toString());
                if (trie.find(sb.toString()) != null) {
                    words1EditDistance.add(sb.toString());
                }
                else {
                    unfoundWords.add(sb.toString());
                }
            }
        }
    }

    private void getAlterationWords(String inputWord, Set<String> words1EditDistance,
                                    Set<String> unfoundWords) {
        for (int i = 0; i < inputWord.length(); i++) {
            for (int j = 0; j < 26; j++) {
                if ((char)(j+'a') != inputWord.charAt(i)) {
                    StringBuilder sb = new StringBuilder(inputWord);
                    sb.setCharAt(i, (char)(j+'a'));
                    if (trie.find(sb.toString()) != null) {
                        words1EditDistance.add(sb.toString());
                    }
                    else {
                        unfoundWords.add(sb.toString());
                    }
                }
            }
        }

    }

    private void getTranspositionWords(String inputWord, Set<String> words1EditDistance,
                                       Set<String> unfoundWords) {
        String temp = null;
        for (int i = 0; i < inputWord.length() - 1; i++) {
            StringBuilder sb = new StringBuilder(inputWord);
            // swap two characters
            sb.setCharAt(i, inputWord.charAt(i+1));
            sb.setCharAt(i+1, inputWord.charAt(i));
            temp = sb.toString();
//			System.out.println(temp);
            if (trie.find(temp) != null) {
                words1EditDistance.add(temp);
            }
            else {
                unfoundWords.add(sb.toString());
            }
        }
    }

    private void getDeletionDistanceWords(String inputWord, Set<String> words1EditDistance,
                                          Set<String> unfoundWords) {
        String temp = null;
        for (int i = 0; i < inputWord.length(); i++) {
            StringBuilder sb = new StringBuilder(inputWord);
            // delete one character
            sb.deleteCharAt(i);
            temp = sb.toString();
            if (trie.find(temp) != null) {
                words1EditDistance.add(temp);
            }
            else {
                unfoundWords.add(sb.toString());
            }
        }
    }

    public String toString() {
        return trie.toString();
    }

}
