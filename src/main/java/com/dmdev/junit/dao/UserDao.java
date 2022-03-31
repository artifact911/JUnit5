package com.dmdev.junit.dao;

import lombok.SneakyThrows;

import java.sql.DriverManager;

public class UserDao {

    @SneakyThrows
    public boolean delete (Integer id) {
        try (var connection = DriverManager.getConnection("url", "username", "password")) {
            return true;
        }
    }
}
