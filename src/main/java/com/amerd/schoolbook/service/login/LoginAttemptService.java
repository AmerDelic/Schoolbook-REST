package com.amerd.schoolbook.service.login;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;
    public static final int ATTEMPT_INCREMENT = 1;

    // key: user - value: attempts:
    private final LoadingCache<String, Integer> loginAttemptCache;

    public LoginAttemptService() {
        super();
        this.loginAttemptCache = CacheBuilder.newBuilder().expireAfterWrite(15, MINUTES)
                .maximumSize(100).build(new CacheLoader<>() {
                    @Override
                    public @NotNull Integer load(@NotNull String key) {
                        return 0;
                    }
                });
    }

    public void evictUserFromLoginAttemptCache(String username) {
        loginAttemptCache.invalidate(username);
    }

    public void addUserToLoginAttemptCache(String username) {
        int attempts = 0;
        try {
            attempts = ATTEMPT_INCREMENT + loginAttemptCache.get(username);
            loginAttemptCache.put(username, attempts);
        } catch (ExecutionException e) {
            log.error("", e);
        }
    }

    public boolean exceededMaxAttempts(String username) {
        try {
            return loginAttemptCache.get(username) >= MAX_ATTEMPTS;
        } catch (ExecutionException e) {
            log.error("", e);
        }
        return false;
    }
}
