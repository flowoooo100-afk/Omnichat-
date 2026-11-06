package com.omnichat.app;

import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

@CapacitorPlugin(name = "SecureKey")
public class SecureKeyPlugin extends Plugin {

    private static final String ALIAS = "OmniChatMasterKey";
    private static final String PREFS = "omnichat_secure_keys";

    private SecretKey getMasterKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (keyStore.containsAlias(ALIAS)) {
            return ((SecretKey) keyStore.getKey(ALIAS, null));
        }

        KeyGenerator generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        );

        generator.init(new KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_ENCRYPT |
            KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build());

        return generator.generateKey();
    }

    @PluginMethod
    public void saveKey(PluginCall call) {
        String provider = call.getString("provider");
        String apiKey = call.getString("apiKey");

        if (provider == null || apiKey == null ||
            provider.trim().isEmpty() || apiKey.trim().isEmpty()) {
            call.reject("Provider and API key are required");
            return;
        }

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getMasterKey());

            byte[] encrypted = cipher.doFinal(
                apiKey.getBytes(StandardCharsets.UTF_8)
            );

            SharedPreferences prefs = getContext()
                .getSharedPreferences(PREFS, 0);

            String id = provider.trim().toLowerCase();

            prefs.edit()
                .putString(id + "_data",
                    Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(id + "_iv",
                    Base64.encodeToString(
                        cipher.getIV(), Base64.NO_WRAP))
                .apply();

            JSObject result = new JSObject();
            result.put("success", true);
            call.resolve(result);

        } catch (Exception e) {
            call.reject("Secure storage failed");
        }
    }

    @PluginMethod
    public void getKey(PluginCall call) {
        String provider = call.getString("provider");

        if (provider == null || provider.trim().isEmpty()) {
            call.reject("Provider is required");
            return;
        }

        try {
            String id = provider.trim().toLowerCase();

            SharedPreferences prefs = getContext()
                .getSharedPreferences(PREFS, 0);

            String data = prefs.getString(id + "_data", null);
            String iv = prefs.getString(id + "_iv", null);

            if (data == null || iv == null) {
                call.reject("API key not found");
                return;
            }

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(
                Cipher.DECRYPT_MODE,
                getMasterKey(),
                new GCMParameterSpec(
                    128,
                    Base64.decode(iv, Base64.NO_WRAP)
                )
            );

            byte[] decrypted = cipher.doFinal(
                Base64.decode(data, Base64.NO_WRAP)
            );

            JSObject result = new JSObject();
            result.put("apiKey",
                new String(decrypted, StandardCharsets.UTF_8));

            call.resolve(result);

        } catch (Exception e) {
            call.reject("Unable to decrypt API key");
        }
    }

    @PluginMethod
    public void deleteKey(PluginCall call) {
        String provider = call.getString("provider");

        if (provider == null || provider.trim().isEmpty()) {
            call.reject("Provider is required");
            return;
        }

        String id = provider.trim().toLowerCase();

        getContext()
            .getSharedPreferences(PREFS, 0)
            .edit()
            .remove(id + "_data")
            .remove(id + "_iv")
            .apply();

        JSObject result = new JSObject();
        result.put("success", true);
        call.resolve(result);
    }
}
