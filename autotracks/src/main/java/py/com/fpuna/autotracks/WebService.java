package py.com.fpuna.autotracks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import py.com.fpuna.autotracks.model.Resultado;
import py.com.fpuna.autotracks.model.Ruta;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.POST;

public class WebService {

    public static final String ENDPOINT = "http://162.243.25.166:8080/autotracks/resources/";

    public static RutasResource getRutasResource() {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(WebService.ENDPOINT)
                .setConverter(new GsonConverter(gson))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        return adapter.create(RutasResource.class);
    }

    public interface RutasResource {

        @POST("/rutas")
        Resultado guardarRuta(@Body Ruta ruta);

    }

}