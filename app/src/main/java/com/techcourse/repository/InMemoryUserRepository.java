package com.techcourse.repository;

import com.techcourse.domain.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository {

    private static final Map<String, User> database = new ConcurrentHashMap<>();

    static {
        final User user = new User(1, "1", "1", "hkkang@woowahan.com");
        database.put(user.getAccount(), user);
    }

    public static void save(final User user) {
        database.put(user.getAccount(), user);
    }

    public static Optional<User> findByAccount(final String account) {
        return Optional.ofNullable(database.get(account));
    }

    private InMemoryUserRepository() {}
}
