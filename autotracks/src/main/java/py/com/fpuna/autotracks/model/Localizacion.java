package py.com.fpuna.autotracks.model;

import android.location.Location;

public class Localizacion {

    private Long _id;
    private long ruta;
    private String imei;
    private double latitud;
    private double longitud;
    private double altitud;
    private float direccion;
    private float velocidad;
    private long fecha;

    public Localizacion() {
    }

    public Localizacion(Location location, long ruta) {
        this.ruta = ruta;
        this.latitud = location.getLatitude();
        this.longitud = location.getLongitude();
        this.altitud = location.getAltitude();
        this.direccion = location.getBearing();
        this.velocidad = location.getSpeed();
        this.fecha = location.getTime();
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        this._id = id;
    }

    public long getRuta() {
        return ruta;
    }

    public void setRuta(long ruta) {
        this.ruta = ruta;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getAltitud() {
        return altitud;
    }

    public void setAltitud(double altitud) {
        this.altitud = altitud;
    }

    public float getDireccion() {
        return direccion;
    }

    public void setDireccion(float direccion) {
        this.direccion = direccion;
    }

    public float getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(float velocidad) {
        this.velocidad = velocidad;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

}
