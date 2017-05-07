package com.company.demo.web.settings;

import com.company.demo.entity.ExtUser;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.VBoxLayout;
import com.haulmont.cuba.web.app.ui.core.settings.SettingsWindow;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Layout;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.ICredentialRepository;
import fi.jasoft.qrcode.QRCode;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class ExtSettingsWindow extends SettingsWindow {
    @Inject
    private VBoxLayout totpOptionsBox;

    @Inject
    private DataManager dataManager;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
    }

    private String generateKeyUri(String account, String issuer,
                                  String secret) throws URISyntaxException {
        URI uri = new URI("otpauth", "totp", "/" + issuer + ":" + account,
                "secret=" + secret + "&issuer=" + issuer, null);

        return uri.toASCIIString();
    }

    public void enableTotpAuth() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();

        ExtUser user = (ExtUser) userSession.getUser();
        String userLogin = user.getLogin();

        gAuth.setCredentialRepository(new ICredentialRepository() {
            @Override
            public String getSecretKey(String userName) {
                return user.getTotpSecret();
            }

            @Override
            public void saveUserCredentials(String userName, String secretKey,
                                            int validationCode, List<Integer> scratchCodes) {
                user.setTotpSecret(secretKey);
                user.setTotpValidationCode(validationCode);

                // Update data in DB
                ExtUser userInstance = dataManager.reload(user, View.LOCAL);
                userInstance.setTotpSecret(secretKey);
                userInstance.setTotpValidationCode(validationCode);
                dataManager.commit(userInstance);
            }
        });

        GoogleAuthenticatorKey key = gAuth.createCredentials(userLogin);

        // make the URI
        String keyUri = "";
        try {
            keyUri = generateKeyUri(userLogin, "CUBA Application", key.getKey());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to build URI", e);
        }

        QRCode qrPanel = new QRCode();
        qrPanel.setCaption(String.format("Scan this code: %s using Authenticator", key.getKey()));
        qrPanel.setValue(keyUri);
        qrPanel.setWidth("140px");
        qrPanel.setHeight("140px");

        // Show QR code
        AbstractOrderedLayout layout = totpOptionsBox.unwrap(AbstractOrderedLayout.class);

        if (layout.getComponentCount() > 1)  {
            layout.removeComponent(layout.getComponent(1));
        }
        layout.addComponent(qrPanel);
    }
}