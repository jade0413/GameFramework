package com.yp.gameframwrok.server.manager;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReconnectTokenManager {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, Entry> tokenToSession = new ConcurrentHashMap<>();
    private volatile long ttlMillis = 30_000;

    public String issue(int userId) {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokenToSession.put(token, new Entry(userId, System.currentTimeMillis() + ttlMillis));
        return token;
    }

    public Integer consume(String token) {
        Entry e = tokenToSession.remove(token);
        if (e == null) return null;
        if (e.expireAt < System.currentTimeMillis()) return null;
        return e.sessionId;
    }

    public void clearExpiredTokens(){
        tokenToSession.values().removeIf(e -> e.expireAt < System.currentTimeMillis());
    }

    private static final class Entry {
        final int sessionId;
        final long expireAt;
        private Entry(int sessionId, long expireAt) {
            this.sessionId = sessionId;
            this.expireAt = expireAt;
        }
    }
}


