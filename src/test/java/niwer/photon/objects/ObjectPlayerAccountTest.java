package niwer.photon.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

class ObjectPlayerAccountTest {

    @Test
    void constructorGeneratesDiscordAuthCode() {
        final ObjectPlayerAccount account = new ObjectPlayerAccount();

        assertNotNull(account.discordAuthCode());
        assertTrue(account.discordAuthCode().matches("[0-9a-v]+"));
    }

    @Test
    void discordLinkAndFriendsHelpersBehaveAsExpected() {
        final ObjectPlayerAccount account = new ObjectPlayerAccount();

        assertFalse(account.hasDiscordLinked());

        setField(account, "discordID", "1234567890");
        setField(account, "firends", "[\"alpha\",\"beta\"]");

        assertTrue(account.hasDiscordLinked());
        assertEquals(List.of("alpha", "beta"), account.getFriendsList());
    }

    @Test
    void getFriendsListReturnsEmptyListForBlankOrInvalidJson() {
        final ObjectPlayerAccount blank = new ObjectPlayerAccount();
        setField(blank, "firends", "");

        final ObjectPlayerAccount invalid = new ObjectPlayerAccount();
        setField(invalid, "firends", "not-json");

        assertTrue(blank.getFriendsList().isEmpty());
        assertTrue(invalid.getFriendsList().isEmpty());
    }

    @Test
    void accessorsExposeThePrivateState() {
        final ObjectPlayerAccount account = new ObjectPlayerAccount();

        setField(account, "username", "alice");
        setField(account, "email", "alice@example.com");
        setField(account, "password", "secret");
        setField(account, "twoAuthFactor", true);
        setField(account, "uuid", "uuid-1");
        setField(account, "discordID", "discord-1");
        setField(account, "discordAuthCode", "code-1");
        setField(account, "projectAuthor", true);
        setField(account, "serverCreator", true);
        setField(account, "shopCoins", 42);

        assertEquals("alice", account.username());
        assertEquals("alice@example.com", account.email());
        assertEquals("secret", account.password());
        assertTrue(account.twoAuthFactor());
        assertEquals("uuid-1", account.uuid());
        assertEquals("discord-1", account.discordID());
        assertEquals("code-1", account.discordAuthCode());
        assertTrue(account.projectAuthor());
        assertTrue(account.serverCreator());
        assertEquals(42, account.shopCoins());

        assertTrue(account.toString().contains("username=alice"));
        assertTrue(account.toString().contains("shopCoins=42"));
    }

    @Test
    void generateAuthCodeProducesDifferentValues() {
        final String first = ObjectPlayerAccount.generateAuthCode();
        final String second = ObjectPlayerAccount.generateAuthCode();

        assertNotNull(first);
        assertNotNull(second);
        assertTrue(first.matches("[0-9a-v]+"));
        assertTrue(second.matches("[0-9a-v]+"));
        assertNotEquals(first, second);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            final Field field = ObjectPlayerAccount.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to set field " + fieldName, e);
        }
    }
}
