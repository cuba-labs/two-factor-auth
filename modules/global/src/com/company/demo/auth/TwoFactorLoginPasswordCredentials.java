package com.company.demo.auth;

import com.haulmont.cuba.security.auth.LoginPasswordCredentials;

public class TwoFactorLoginPasswordCredentials extends LoginPasswordCredentials {

    private final Integer verificationCode;

    public TwoFactorLoginPasswordCredentials(LoginPasswordCredentials credentials, Integer verificationCode) {
        super(credentials.getLogin(), credentials.getPassword(), credentials.getLocale(), credentials.getParams());
        this.verificationCode = verificationCode;
    }

    public Integer getVerificationCode() {
        return verificationCode;
    }
}
