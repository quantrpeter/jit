package com.jitcompiler.samples;

/**
 * Sample class demonstrating string processing
 */
public class StringProcessor {
    
    /**
     * Concatenate two strings
     */
    public String concat(String a, String b) {
        return a + b;
    }
    
    /**
     * Reverse a string
     */
    public String reverse(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        StringBuilder sb = new StringBuilder(input);
        return sb.reverse().toString();
    }
    
    /**
     * Count vowels in a string
     */
    public int countVowels(String input) {
        if (input == null) {
            return 0;
        }
        
        int count = 0;
        String vowels = "aeiouAEIOU";
        
        for (int i = 0; i < input.length(); i++) {
            if (vowels.indexOf(input.charAt(i)) != -1) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Check if string is palindrome
     */
    public boolean isPalindrome(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        
        int left = 0;
        int right = input.length() - 1;
        
        while (left < right) {
            if (input.charAt(left) != input.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        
        return true;
    }
}

