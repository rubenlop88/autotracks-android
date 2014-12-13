package py.com.fpuna.autotracks.model;

import com.google.gson.annotations.Expose;

import java.util.List;

import nl.qbusict.cupboard.annotation.Column;
import nl.qbusict.cupboard.annotation.Ignore;

public class Ruta {

    // Se usan anotaciones @Expose en los campos que queremos enviar al servidor en formato JSON.

    private Long _id;
    @Expose private long fecha;
    @Expose @Ignore private List<Localizacion> localizaciones; // @Ignore para que Cupboard ignore este campo
    @Expose @Column("server_id") private Long serverId; // @Column para que Cubpard cree el campo server_id en lugar de serverId
    private long fin;

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

    public long getFin() {
        return fin;
    }

    public void setFin(long fin) {
        this.fin = fin;
    }

    public List<Localizacion> getLocalizaciones() {
        return localizaciones;
    }

    public void setLocalizaciones(List<Localizacion> localizaciones) {
        this.localizaciones = localizaciones;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }
}
