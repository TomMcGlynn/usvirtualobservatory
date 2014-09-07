package org.usvao.sso.ip.pw;

import java.util.List;
import java.util.Vector; 
import java.util.Set; 
import java.util.HashSet; 
import java.util.regex.Pattern; 

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;

/**
 * a {@link PasswordChecker PasswordChecker} that makes sure that the 
 * password does not match any of a list of banned words.  A default list of
 * banned words is provided by an associated class resource; however, other
 * may be added.  The string comparison is case-insensitive.
 */
public class BannedWordsPasswordChecker extends PasswordCheckerBase {

    Set<String> banned = new HashSet<String>();
    static Pattern sp = Pattern.compile("\\s+");

    /**
     * create a checker with the default set of banned words.  
     */
    public BannedWordsPasswordChecker() { loadDefaultWords(); }

    /**
     * create a checker with an initial set of banned words. 
     * @param words         an initial set of banned words
     * @param loadDefaults  if true, also load the default set of banned words.
     */
    public BannedWordsPasswordChecker(String[] words, boolean loadDefaults) {
        if (loadDefaults) loadDefaultWords();
        addWords(words);
    }

    /**
     * create a checker that will look for one banned word.  The default 
     * list will not be loaded.
     */
    public BannedWordsPasswordChecker(String word) {
        addWord(word);
    }

    /**
     * create a checker that will look words from a stream.  The default 
     * list will not be loaded.
     */
    public BannedWordsPasswordChecker(Reader strm, boolean ignorePunctuation) 
        throws IOException
    {
        addWords(strm, ignorePunctuation);
    }

    void loadDefaultWords() {
        InputStream res = getClass().getResourceAsStream("bannedwords.txt");
        if (res == null) return;

        try {
            addWords(new InputStreamReader(res), false);
        }
        catch (IOException ex) {
            throw new InternalError("Failed read resource, bannedwords.txt: " +
                                    ex.getMessage());
        }
    }

    /**
     * add all words found read in from the given stream.
     */
    public void addWords(Reader strm, boolean ignorePunctuation) 
        throws IOException
    {
        BufferedReader rdr = new BufferedReader(strm);
        String line = null;
        StringBuilder filtered = null;
        char c = 0;
        while ((line = rdr.readLine()) != null) {
            if (ignorePunctuation) {
                filtered = new StringBuilder();
                for(int i=0; i < line.length(); ++i) {
                    c = line.charAt(i);
                    if (! Character.isLetter(c) && ! Character.isDigit(c) &&
                        c != '\'') // treat apostrophes like letters
                        c = ' ';
                    filtered.append(c);
                }
                line = filtered.toString();
            }

            addWords(sp.split(line));
        }
    }

    /**
     * add a list of words.  Each word is added exactly as provided with 
     * no filtering or trimming.  
     */
    public void addWords(String[] words) {
        for(String word : words)
            addWord(word);
    }

    /**
     * add a word to the banned list.  The word is added exactly as provided;
     * no filtering or trimming is done.  
     */
    public void addWord(String word) {
        banned.add(word.toLowerCase());
    }

    /**
     * return the number of banned words that will be looked for
     */
    public int getWordCount() { return banned.size(); }

    /**
     * add a list of reasons as to why the given password does not 
     * comply with password policies to a given List.  If the password
     * is fully compliant with policy, nothing is added to the list and 
     * zero is returned.
     * <p>
     * This implementation tests the password against the internal banned list.
     */
    @Override
    public int explainNoncompliance(String password, List<String> reasons) {
        if (banned.contains(password.toLowerCase())) {
            reasons.add("Password matches banned word");
            return 1;
        }
        return 0;
    }
    
}