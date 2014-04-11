package py.com.fpuna.autotracks.model;

import java.util.List;

import nl.qbusict.cupboard.annotation.Ignore;

public class Ruta {

    private Long _id;
    private long fecha;
    @Ignore private List<Localizacion> localizaciones;

    public Ruta() {
    }

    public Ruta(long fecha) {
        this.fecha = fecha;
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        this._id = id;
    } 

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public List<Localizacion> getLocalizaciones() {
        return localizaciones;
    }

    public void setLocalizaciones(List<Localizacion> localizaciones) {
        this.localizaciones = localizaciones;
    }

}
