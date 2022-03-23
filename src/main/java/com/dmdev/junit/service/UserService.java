package com.dmdev.junit.service;

import com.dmdev.junit.dto.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class UserService {

    private final List<User> users = new ArrayList<>();

    public List<User> getAll() {
        return users;
    }

    public void add(User... users) {
         this.users.addAll(Arrays.asList(users));
    }

    public Optional<User> login(String name, String password) {
        return users.stream()
                    .filter(user -> user.getName().equals(name))
                    .filter(user -> user.getPassword().equals(password))
                    .findFirst();
    }

    public Map<Integer, User> getAllConvertedById() {
        return users.stream()
                .collect(toMap(User::getId, identity()));
    }
}
