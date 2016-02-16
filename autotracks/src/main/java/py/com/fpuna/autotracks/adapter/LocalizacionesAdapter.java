package py.com.fpuna.autotracks.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import py.com.fpuna.autotracks.R;
import py.com.fpuna.autotracks.model.Localizacion;

/**
 * Created by Alfredo on 13/12/2014.
 */
public class LocalizacionesAdapter extends RecyclerView.Adapter<LocalizacionesAdapter.ViewHolder> {
    private Localizacion[] mDataset;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView posicionTextView;
        public TextView fechaHoraTextView;
        public ImageView imgViewIcon;

        public ViewHolder(View localizacionesLayoutView) {
            super(localizacionesLayoutView);
            posicionTextView = (TextView) localizacionesLayoutView.findViewById(R.id.localizaciones_posicion);
            fechaHoraTextView = (TextView) localizacionesLayoutView.findViewById(R.id.localizaciones_fecha);
            imgViewIcon = (ImageView) localizacionesLayoutView.findViewById(R.id.localizaciones_icon);
        }
    }

    public LocalizacionesAdapter(Localizacion[] localizaciones) {
        this.mDataset = localizaciones;
    }

    @Override
    public LocalizacionesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View localizacionesLayout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.localizaciones_layout, null);

        ViewHolder viewHolder = new ViewHolder((RelativeLayout) localizacionesLayout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Double latitud = mDataset[position].getLatitud();
        Double longitud = mDataset[position].getLongitud();
        Long fecha = mDataset[position].getFecha();
        viewHolder.posicionTextView.setText(latitud + ", " + longitud);
        viewHolder.fechaHoraTextView.setText(sdf.format(new Date(fecha)));
    }

    @Override
    public int getItemCount() {
        return this.mDataset.length;
    }
}
