package py.com.fpuna.autotracks.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;

import py.com.fpuna.autotracks.R;
import py.com.fpuna.autotracks.model.Ruta;

/**
 * Created by Alfredo on 13/12/2014.
 */
public class RutasAdapter extends RecyclerView.Adapter<RutasAdapter.ViewHolder> {
    private Ruta[] mDataset;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView inicioTextView;
        public ImageView imgViewIcon;
        public  TextView finTextView;

        public ViewHolder(View rutasLayoutView) {
            super(rutasLayoutView);
            inicioTextView = (TextView) rutasLayoutView.findViewById(R.id.rutas_inicio);
            finTextView = (TextView) rutasLayoutView.findViewById(R.id.rutas_fin);
            imgViewIcon = (ImageView) rutasLayoutView.findViewById(R.id.rutas_icon);
        }
    }

    public RutasAdapter(Ruta[] rutas) {
        this.mDataset = rutas;
    }

    @Override
    public RutasAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rutasLayout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rutas_layout, null);

        ViewHolder viewHolder = new ViewHolder((RelativeLayout) rutasLayout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Date inicio = new Date(mDataset[position].getFecha());
        Date fin = new Date(mDataset[position].getFin());
        viewHolder.inicioTextView.setText(sdf.format(inicio));
        viewHolder.finTextView.setText(sdf.format(fin));
    }

    @Override
    public int getItemCount() {
        return this.mDataset.length;
    }
}
