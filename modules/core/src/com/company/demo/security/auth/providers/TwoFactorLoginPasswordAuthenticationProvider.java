package com.company.demo.security.auth.providers;

import com.company.demo.auth.TwoFactorLoginPasswordCredentials;
import com.company.demo.entity.ExtUser;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.security.auth.AuthenticationDetails;
import com.haulmont.cuba.security.auth.Credentials;
import com.haulmont.cuba.security.auth.providers.AbstractAuthenticationProvider;
import com.haulmont.cuba.security.auth.providers.LoginPasswordAuthenticationProvider;
import com.haulmont.cuba.security.global.BadCredentialsException;
import com.haulmont.cuba.security.global.LoginException;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorException;
import com.warrenstrange.googleauth.ICredentialRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;


@Component("cuba_TwoFactorLoginPasswordAuthenticationProvider")
public class TwoFactorLoginPasswordAuthenticationProvider extends AbstractAuthenticationProvider implements Ordered {

    private final LoginPasswordAuthenticationProvider loginPasswordAuthenticationProvider;
    @Inject
    private Logger log;
    @Inject
    private DataManager dataManager;

    public TwoFactorLoginPasswordAuthenticationProvider(Persistence persistence, Messages messages, LoginPasswordAuthenticationProvider loginPasswordAuthenticationProvider) {
        super(persistence, messages);
        this.loginPasswordAuthenticationProvider = loginPasswordAuthenticationProvider;
    }

    @Override
    public AuthenticationDetails authenticate(Credentials credentials) throws LoginException {
        TwoFactorLoginPasswordCredentials creds = (TwoFactorLoginPasswordCredentials)credentials;
        if (check2fa(creds.getLogin(), creds.getVerificationCode(), creds.getLocale())) {
            return loginPasswordAuthenticationProvider.authenticate(credentials);
        }
        throw new BadCredentialsException(getInvalidCredentialsMessage(creds.getLogin(), creds.getLocale()));
    }

    @Override
    public boolean supports(Class<?> credentialsClass) {
        return TwoFactorLoginPasswordCredentials.class.isAssignableFrom(credentialsClass);
    }

    @Override
    public int getOrder() {
        return loginPasswordAuthenticationProvider.getOrder()-10;
    }

    private boolean check2fa(String login, Integer authKey, Locale credentialsLocale) {
        ExtUser user = dataManager.load(LoadContext.create(ExtUser.class)
                .setQuery(
                        new LoadContext.Query("select u from sec$User u where u.loginLowerCase = :login")
                                .setParameter("login", login.toLowerCase())
                )
                .setView(View.LOCAL)
        );
        if (user != null && user.getTotpSecret() != null) {
            if (authKey == null) {
                throw new LoginException("You must provide two factor auth key to proceed");
            }

            GoogleAuthenticator gAuth = new GoogleAuthenticator();

            gAuth.setCredentialRepository(new ICredentialRepository() {
                @Override
                public String getSecretKey(String userName) {
                    return user.getTotpSecret();
                }

                @Override
                public void saveUserCredentials(String userName, String secretKey,
                                                int validationCode, List<Integer> scratchCodes) {
                    throw new RuntimeException("TOTP key cannot be updated before login");
                }
            });

            try {
                return gAuth.authorizeUser(user.getLoginLowerCase(), authKey);
            } catch (GoogleAuthenticatorException | NumberFormatException e) {
                log.error("Incorrect two factor auth key", e);
                throw new BadCredentialsException(getInvalidCredentialsMessage(login, credentialsLocale));
            }
        }
        throw new BadCredentialsException(getInvalidCredentialsMessage(login, credentialsLocale));
    }

}
