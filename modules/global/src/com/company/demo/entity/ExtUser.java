package com.company.demo.entity;

import javax.persistence.Entity;
import com.haulmont.cuba.core.entity.annotation.Extends;
import javax.persistence.Column;
import com.haulmont.cuba.security.entity.User;

@Extends(User.class)
@Entity(name = "demo$ExtUser")
public class ExtUser extends User {
    private static final long serialVersionUID = 8816758699583136030L;

    @Column(name = "TOTP_SECRET")
    protected String totpSecret;

    @Column(name = "TOTP_VALIDATION_CODE")
    protected Integer totpValidationCode;

    public Integer getTotpValidationCode() {
        return totpValidationCode;
    }

    public void setTotpValidationCode(Integer totpValidationCode) {
        this.totpValidationCode = totpValidationCode;
    }


    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public String getTotpSecret() {
        return totpSecret;
    }


}