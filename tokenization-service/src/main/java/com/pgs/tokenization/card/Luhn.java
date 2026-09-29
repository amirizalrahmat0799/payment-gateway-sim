package com.pgs.tokenization.card;

/** Luhn (mod 10) checksum used by all major card schemes. */
public final class Luhn {

    private Luhn() {
    }

    public static boolean isValid(String digits) {
        if (digits == null || digits.length() < 12 || digits.length() > 19 || !digits.chars().allMatch(Character::isDigit)) {
            return false;
        }
        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }
}
