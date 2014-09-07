package org.usvao.sso.ip.pw;

import java.util.List;
import java.util.Vector; 
import java.util.regex.Pattern;

/**
 * a {@link PasswordChecker PasswordChecker} that makes sure that the 
 * password isn't too close to a set of words.  This checker is intended for
 * making sure that a password isn't to close to the username.  It checks not 
 * only that the password isn't a case sensitive match but also if it contains
 * the word that it the unmatched part is at least a certain minimum
 * number of characters long.  This class also looks for combinations of the 
 * given words.  
 */
public class LikeWordsPasswordChecker extends PasswordCheckerBase {

    int minleftover = 6;
    Vector<DangerWord> words = new Vector<DangerWord>();

    /**
     * create the checker.  A password that contains a word of concern 
     * must have at least 6 additional characters.  
     * For this checker to work properly, words
     * much be added with addWord() after calling this constructor.
     */
    public LikeWordsPasswordChecker() { }

    /**
     * create the checker.  For this checker to work properly, words
     * much be added with addWord() after calling this constructor.
     * @param extralen   the minimum extra length:  If the password 
     *                      begins or ends with this user string, the 
     *                      remainder of the password must be this length
     */
    public LikeWordsPasswordChecker(int extralen) {
        minleftover = extralen;
    }

    /**
     * create a checker that checks a single word
     * @param word       a word of concern to check for
     * @param label      a label that identifies the word represents (e.g.
     *                      "the username".  If null, word will be used.
     * @param extralen   the minimum extra length:  If the password 
     *                      begins or ends with this user string, the 
     *                      remainder of the password must be this length
     */
    public LikeWordsPasswordChecker(String word, String label, int extralen) {
        this(extralen);
        addWord(word, label);
    }

    /**
     * return the minimum number of extra characters required to be in a 
     * password if it begins or ends with a word of concern.
     */
    public int getMinLeftoverLength() { return minleftover; }

    /**
     * return the list of words of concern that this checker is checking 
     * for.
     */
    public String[] getWords() {
        synchronized (words) {
            String[] out = new String[words.size()];
            int i = 0;
            for (DangerWord danger : words) 
                out[i++] = danger.word;
            return out;
        }
    }

    /**
     * return the list of labels that identify each of the words of concern.
     */
    public String[] getWordLabels() {
        synchronized (words) {
            String[] out = new String[words.size()];
            int i = 0;
            for (DangerWord danger : words) 
                out[i++] = danger.what;
            return out;
        }
    }

    /**
     * add a word to the list of words of concern that this checker checks 
     * for.
     */
    public void addWord(String word) { 
        synchronized (words) {
            words.add(new DangerWord(word)); 
        }
    }

    /**
     * add a word to the list of words of concern that this checker checks 
     * for.
     * @param word   the actual word to look for in the password
     * @param label  a label that identifies what the word represents (e.g.
     *                 "the username").  If null, word is used.  
     */
    public void addWord(String word, String label) { 
        synchronized (words) {
            if (label == null) label = word;
            words.add(new DangerWord(word, label)); 
        }
    }

    static Pattern meta = Pattern.compile("([\\\\\\[\\]\\.\\?\\*\\+\\{\\}\\(\\)])");

    class DangerWord {
        public String word = null;
        public String what = null;
        public Pattern contains = null;
        public DangerWord(String word, String what) {
            this.word = word;
            if (what == null) what = word;
            this.what = what;
            String wordpat = meta.matcher(word).replaceAll("\\\\$1");
            contains = Pattern.compile(wordpat, Pattern.CASE_INSENSITIVE);
        }
        public DangerWord(String word) { this(word, word); } 
    }

    /**
     * add a list of reasons as to why the given password does not 
     * comply with password policies to a given List.  If the password
     * is fully compliant with policy, nothing is added to the list and 
     * zero is returned.
     * <p>
     * This implementation checks to see if begins or ends (case-insensitively)
     * with each of the internal words of concern.  If it does, the unmatched
     * portion must contain a minimum number of characters.  This checks 
     * against all of the words, even after a failure is found.  
     * @return int   the number of failure explanations added to the list
     */
    @Override
    public int explainNoncompliance(String password, List<String> reasons) {
        int nprobs = 0;
        int comblength = 0;
        int nmatches = 0;
        StringBuilder combwhat = new StringBuilder();
        for (DangerWord danger : words) {
            if (danger.contains.matcher(password).find()) {
                nmatches++;
                if (danger.word.length() == password.length()) {
                    reasons.add("Password cannot match " + danger.what);
                    nprobs++;
                }
                else if (password.length()-danger.word.length() < minleftover) {
                    reasons.add("Password is too close to " + danger.what +
                                " (add more characters)");
                    nprobs++;
                }

                if (combwhat.length() > 0) combwhat.append(", ");
                combwhat.append(danger.what);
                comblength += danger.word.length();
            }
        }

        if (nmatches > 1 && password.length()-comblength < minleftover) {
            reasons.add("Password is too close to a combination of " + 
                        combwhat.toString());
            nprobs++;
        }

        return nprobs;
    }
}