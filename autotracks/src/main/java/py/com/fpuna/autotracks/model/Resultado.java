package py.com.fpuna.autotracks.model;

import com.google.gson.annotations.Expose;

public class Resultado {

    // Se usan anotaciones @Expose en los campos que queremos enviar al servidor en formato JSON.

    @Expose private boolean exitoso;
    @Expose private String mensaje;
    @Expose private Long id;

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
