package py.com.fpuna.autotracks;

import py.com.fpuna.autotracks.model.Resultado;
import py.com.fpuna.autotracks.model.Ruta;
import retrofit.http.Body;
import retrofit.http.POST;

public interface WebService {

    public static final String ENDPOINT = "http://209.208.108.214:8080/autotracks/resources/";

    @POST("/rutas")
    public Resultado guardarRuta(@Body Ruta ruta);

}
