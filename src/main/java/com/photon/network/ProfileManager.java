package com.photon.network;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.sql.SQLPlayerAccount;
import com.photon.util.NetworkOnly;

@NetworkOnly
@Deprecated
public class ProfileManager {
    private static final SecureRandom random = new SecureRandom();

    public static String getTokenFromEMail(final String name) {
        final long longToken = Math.abs(random.nextLong());
        return name + ":" + Long.toString(longToken, 16);
    }

    public static void deleteProfile(final ObjectPlayerAccount profile) {
        if (profile != null && profile.uuid != null) {
            SQLPlayerAccount.deleteAccount(profile.uuid);
        }
    }

    public static ArrayList<ObjectPlayerAccount> getAllPorifles() {
        List<ObjectPlayerAccount> profiles = SQLPlayerAccount.getAllAccounts();
        return new ArrayList<>(profiles);
    }

    public static ObjectPlayerAccount getProfileFromEMail(final String email) {
        return SQLPlayerAccount.getAccountByEmail(email);
    }

    public static ObjectPlayerAccount getProfileFromDiscordID(final String discordID) {
        return SQLPlayerAccount.getAccountByDiscordID(discordID);
    }

    public static ObjectPlayerAccount getProfileFromUUID(final String givenUUID) {
        return SQLPlayerAccount.getAccountByUUID(givenUUID);
    }

    public static ObjectPlayerAccount getProfileFromUsername(final String username) {
        return SQLPlayerAccount.getAccountByUsername(username);
    }

    public static ObjectPlayerAccount createPlayerProfile(final String username, final String email, final String password) {
        if (username == null || email == null || password == null) return null;
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) return null;
        if (doesProfileExistByEMail(email)) return null;
        if (doesProfileExistByUsername(username)) return null;

        return SQLPlayerAccount.createAccount(username, email, password);
    }

    public static boolean doesProfileExistByEMail(final String email) {
        return SQLPlayerAccount.emailExists(email);
    }

    public static boolean doesProfileExistByUsername(final String username) {
        return SQLPlayerAccount.usernameExists(username);
    }

    public static boolean doesProfileExistByUUID(final String givenUUID) {
        return SQLPlayerAccount.getAccountByUUID(givenUUID) != null;
    }

    /**
     * Checks if the profile exists for this UUID and if the auth code corresponds to the profile UUID
     * @param givenUUID ingame UUID
     * @param givenAuthCode auth code from ingame
     * @return Boolean : true if the profile exists and the auth code corresponds to the profile UUID, false otherwise
     */
    public static boolean isAuthCodeValid(final String givenUUID, final String givenAuthCode) {
        return SQLPlayerAccount.isAuthCodeValid(givenUUID, givenAuthCode);
    }
}