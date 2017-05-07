package com.company.demo.web.login;

import com.company.demo.entity.ExtUser;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.web.app.loginwindow.AppLoginWindow;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorException;
import com.warrenstrange.googleauth.ICredentialRepository;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;

public class ExtAppLoginWindow extends AppLoginWindow {
    @Inject
    private TextField authKeyBox;

    @Inject
    private DataManager dataManager;

    @Override
    protected void doLogin(String login, String password, Locale locale) throws LoginException {
        // check auth key first

        ExtUser user = dataManager.load(LoadContext.create(ExtUser.class)
                .setQuery(
                        new LoadContext.Query("select u from sec$User u where u.loginLowerCase = :login")
                                .setParameter("login", login.toLowerCase())
                )
                .setView(View.LOCAL)
        );
        if (user != null && user.getTotpSecret() != null) {
            if (StringUtils.isEmpty(authKeyBox.getRawValue())) {
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
                boolean valid = gAuth.authorizeUser(user.getLoginLowerCase(), Integer.parseInt(authKeyBox.getValue()));
                if (!valid) {
                    throw new LoginException("Incorrect two factor auth key");
                }
            } catch (GoogleAuthenticatorException | NumberFormatException e) {
                throw new LoginException("Incorrect two factor auth key");
            }
        }

        super.doLogin(login, password, locale);
    }
}