# Two Factor Authorization for CUBA applications

If a user wants to enable two factor authentication then they go to Help - Settings menu and click on Two factor auth - Enable / Regenerate. Then they scan QR code using [Google Authenticator](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2) (Or another authenticator app). This secret key is stored to DB, see extended User entity - ExtUser with two additional attributes: totpSecret and totpValidationCode.


After that they can log in to the system only if they enter additional Auth key to login form.


This demo uses Vaadin add-on `org.vaadin.addons:qrcode:2.1` and `com.warrenstrange:googleauth:1.1.1` library. See extended login screen, settings screen and `TwoFactorLoginPasswordAuthenticationProvider` class for implementation details.
