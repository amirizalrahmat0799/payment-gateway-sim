package com.pgs.tokenization.vault;

import com.pgs.tokenization.card.CardBrand;
import com.pgs.tokenization.card.Luhn;
import com.pgs.tokenization.crypto.CardCrypto;
import com.pgs.tokenization.web.dto.TokenizeRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.YearMonth;
import java.util.HexFormat;

@Service
public class TokenizationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final CardTokenRepository tokens;
    private final CardCrypto crypto;
    private final Clock clock;

    public TokenizationService(CardTokenRepository tokens, CardCrypto crypto, Clock clock) {
        this.tokens = tokens;
        this.crypto = crypto;
        this.clock = clock;
    }

    @Transactional
    public CardToken tokenize(TokenizeRequest request) {
        String pan = request.pan().replaceAll("[\\s-]", "");
        if (!Luhn.isValid(pan)) {
            throw new InvalidCardException("Card number failed Luhn check");
        }
        CardBrand brand = CardBrand.detect(pan);
        if (brand == CardBrand.UNKNOWN) {
            throw new InvalidCardException("Unsupported card brand");
        }
        if (YearMonth.of(request.expiryYear(), request.expiryMonth()).isBefore(YearMonth.now(clock))) {
            throw new InvalidCardException("Card is expired");
        }

        CardToken token = new CardToken(
                newToken(),
                crypto.encrypt(pan),
                crypto.fingerprint(pan),
                pan.substring(pan.length() - 4),
                brand,
                request.expiryMonth(),
                request.expiryYear(),
                request.cardholderName());
        return tokens.save(token);
    }

    @Transactional(readOnly = true)
    public CardToken get(String token) {
        return tokens.findById(token).orElseThrow(() -> new TokenNotFoundException(token));
    }

    public boolean isExpired(CardToken token) {
        return token.isExpired(YearMonth.now(clock));
    }

    private static String newToken() {
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        return "tok_" + HexFormat.of().formatHex(bytes);
    }
}
