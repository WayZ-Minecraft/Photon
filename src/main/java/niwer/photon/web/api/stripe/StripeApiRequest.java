package niwer.photon.web.api.stripe;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.api.ApiRequest;

public abstract class StripeApiRequest<T> extends ApiRequest<JsonObject> {

    private final Class<T> TYPE_CLASS;

    protected StripeApiRequest(Class<T> responseType) { this.TYPE_CLASS = responseType; }

    @Override
    public void addHeaders(HttpRequest.Builder builder) {
        builder.header("Authorization", "Bearer " + Directories.getConfig().stripe_api_key)
            .header("Accept", "application/json");
    }

    @Override
    public HttpMethod method() { return HttpMethod.GET; }

    // @Override
    // public T request() {
    //     try {
    //         final HttpResponse<String> RESPONSE = this.sendHttpRequest(HttpResponse.BodyHandlers.ofString());
    //         if(RESPONSE.statusCode() == 200) return GsonUtils.GSON.fromJson(RESPONSE.body(), this.TYPE_CLASS);
    //         else Console.log(String.format("Error (%d) : %s", RESPONSE.statusCode(), RESPONSE.body())).type(PhotonLogTypes.WEB_SERVER).container(PhotonEngine.LOGGER).send();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     return null;
    // }
    @Override
    public JsonObject request() {
        try {
            final HttpResponse<String> RESPONSE = this.sendHttpRequest(HttpResponse.BodyHandlers.ofString());
            if(RESPONSE.statusCode() == 200) return JsonParser.parseString(RESPONSE.body()).getAsJsonObject();
            else Console.log(String.format("Error (%d) : %s", RESPONSE.statusCode(), RESPONSE.body())).type(PhotonLogTypes.WEB_SERVER).container(PhotonEngine.LOGGER).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String extractDataObjectId(String payload) {
        final JsonObject root = GsonUtils.parseJsonObject(payload);
        final JsonObject data = GsonUtils.getObject(root, "data");
        final JsonObject object = GsonUtils.getObject(data, "object");
        return GsonUtils.getString(object, "id");
    }
}
