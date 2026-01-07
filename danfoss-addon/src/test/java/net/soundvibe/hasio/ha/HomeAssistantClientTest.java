package net.soundvibe.hasio.ha;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class HomeAssistantClientTest {

    @Test
    void should_use_shared_http_client_across_instances() throws Exception {
        var client1 = new HomeAssistantClient("token1");
        var client2 = new HomeAssistantClient("token2");

        OkHttpClient httpClient1 = getHttpClient(client1);
        OkHttpClient httpClient2 = getHttpClient(client2);

        assertSame(httpClient1, httpClient2, "OkHttpClient should be shared across instances");
    }

    @Test
    void should_have_static_http_client() throws Exception {
        Field httpClientField = HomeAssistantClient.class.getDeclaredField("HTTP_CLIENT");

        assertTrue(java.lang.reflect.Modifier.isStatic(httpClientField.getModifiers()),
                "HTTP_CLIENT should be static");
        assertTrue(java.lang.reflect.Modifier.isFinal(httpClientField.getModifiers()),
                "HTTP_CLIENT should be final");
    }

    private OkHttpClient getHttpClient(HomeAssistantClient client) throws Exception {
        Field field = HomeAssistantClient.class.getDeclaredField("HTTP_CLIENT");
        field.setAccessible(true);
        return (OkHttpClient) field.get(client);
    }
}
