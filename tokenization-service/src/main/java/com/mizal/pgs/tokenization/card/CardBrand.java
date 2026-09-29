package com.mizal.pgs.tokenization.card;

/** Card scheme detection from the IIN (first digits of the PAN). */
public enum CardBrand {
    VISA,
    MASTERCARD,
    AMEX,
    UNKNOWN;

    public static CardBrand detect(String pan) {
        if (pan == null || pan.length() < 6) {
            return UNKNOWN;
        }
        if (pan.startsWith("4")) {
            return VISA;
        }
        if (pan.startsWith("34") || pan.startsWith("37")) {
            return AMEX;
        }
        int first2 = Integer.parseInt(pan.substring(0, 2));
        int first4 = Integer.parseInt(pan.substring(0, 4));
        if ((first2 >= 51 && first2 <= 55) || (first4 >= 2221 && first4 <= 2720)) {
            return MASTERCARD;
        }
        return UNKNOWN;
    }
}
