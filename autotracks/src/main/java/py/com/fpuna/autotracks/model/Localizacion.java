package py.com.fpuna.autotracks.model;

import android.location.Location;

import com.google.gson.annotations.Expose;

public class Localizacion {

    public class Enviado {
        public static final String TRUE = "t";
        public static final String FALSE = "f";
    }

    // Se usan anotaciones @Expose en los campos que queremos enviar al servidor en formato JSON.

    private Long _id;
    private long ruta;
    @Expose private String imei;
    @Expose private double latitud;
    @Expose private double longitud;
    @Expose private double altitud;
    @Expose private float exactitud;
    @Expose private float direccion;
    @Expose private float velocidad;
    @Expose private long fecha;
    private String enviado;

    public Localizacion() {
    }

    public Localizacion(Location location, long ruta) {
        this.ruta = ruta;
        this.latitud = location.getLatitude();
        this.longitud = location.getLongitude();
        this.altitud = location.getAltitude();
        this.exactitud = location.getAccuracy();
        this.direccion = location.getBearing();
        this.velocidad = location.getSpeed();
        this.fecha = location.getTime();
        this.enviado = Enviado.FALSE;
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

    public float getExactitud() {
        return exactitud;
    }

    public void setExactitud(float exactitud) {
        this.exactitud = exactitud;
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

    public String getEnviado() {
        return enviado;
    }

    public void setEnviado(String enviado) {
        this.enviado = enviado;
    }

}
