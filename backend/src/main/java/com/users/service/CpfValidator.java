package com.users.service;

public final class CpfValidator {
    private CpfValidator() {}

    public static String normalize(String value) {
        if (value == null
                || !value.matches("(?:[0-9]{11}|[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2})"))
            throw new IllegalArgumentException("Invalid CPF");
        String cpf = value.replace(".", "").replace("-", "");
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1)
            throw new IllegalArgumentException("Invalid CPF");
        for (int length = 9; length <= 10; length++) {
            int sum = 0;
            for (int i = 0; i < length; i++) sum += (cpf.charAt(i) - '0') * (length + 1 - i);
            int digit = (sum * 10) % 11;
            if (digit == 10) digit = 0;
            if (digit != cpf.charAt(length) - '0')
                throw new IllegalArgumentException("Invalid CPF");
        }
        return cpf;
    }
}
