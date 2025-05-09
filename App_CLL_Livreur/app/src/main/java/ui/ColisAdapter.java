package ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_cll_livreur.R;

import java.util.ArrayList;
import java.util.List;

import data.model.Colis;

/**
 * RecyclerView.Adapter implementation for displaying a list of {@link Colis} items.
 */
public class ColisAdapter extends RecyclerView.Adapter<ColisAdapter.ColisViewHolder> {

    /** Internal list of packages to display. */
    private final List<Colis> colisList;

    /**
     * Constructs a new ColisAdapter with the given initial list.
     *
     * @param colisList the initial list of {@link Colis} items; may be null
     */
    public ColisAdapter(List<Colis> colisList) {
        this.colisList = (colisList != null) ? new ArrayList<>(colisList) : new ArrayList<>();
    }

    /**
     * Replaces the current list of packages with a new list and refreshes the view.
     *
     * @param newColisList the new list of {@link Colis} items to display
     */
    public void setColisList(List<Colis> newColisList) {
        colisList.clear();
        colisList.addAll(newColisList);
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new {@link ColisViewHolder} for an item.
     * Inflates the item layout and creates the holder.
     *
     * @param parent   the parent ViewGroup into which the new view will be added
     * @param viewType the view type of the new view (unused)
     * @return a new {@link ColisViewHolder} holding the inflated item view
     */
    @NonNull
    @Override
    public ColisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package, parent, false);
        return new ColisViewHolder(view);
    }

    /**
     * Binds the data from a {@link Colis} item to the given {@link ColisViewHolder}.
     *
     * @param holder   the holder to bind data into
     * @param position the position of the item in the adapter's list
     */
    @Override
    public void onBindViewHolder(@NonNull ColisViewHolder holder, int position) {
        Colis colis = colisList.get(position);
        holder.name.setText(colis.getName());
        holder.address.setText(colis.getAdresse());
        holder.number.setText("N°" + colis.getNumber());
    }

    /**
     * Returns the total number of {@link Colis} items in the adapter.
     *
     * @return the size of the internal colis list
     */
    @Override
    public int getItemCount() {
        return colisList.size();
    }

    /**
     * ViewHolder implementation for a Colis item.
     * <p>
     * Holds references to the TextViews for name, address, and package number.
     * </p>
     */
    static class ColisViewHolder extends RecyclerView.ViewHolder {
        TextView name, address, number;

        public ColisViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);
            address = itemView.findViewById(R.id.tv_address);
            number = itemView.findViewById(R.id.tv_number);
        }
    }

    /**
     * Returns the {@link Colis} at the specified position.
     *
     * @param position the index of the desired item
     * @return the Colis object at the given position
     */
    public Colis getColisAt(int position) {
        return colisList.get(position);
    }
}
