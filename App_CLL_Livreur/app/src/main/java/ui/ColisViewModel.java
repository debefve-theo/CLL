package ui;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import data.api.amazi.AmaziServices;
import data.model.Colis;
import utils.TokenHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel that manages the list of delivery packages (Colis) and the current
 * delivery ID. It provides LiveData for observing the full and filtered lists,
 * and interacts with AmaziServices to retrieve the delivery ID.
 */
public class ColisViewModel extends ViewModel {

    private final MutableLiveData<List<Colis>> colisLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Colis>> filteredColisLiveData = new MutableLiveData<>();
    //private int LivraisonId;

    /**
     * Returns a LiveData stream of the current list of packages.
     *
     * @return LiveData containing the list of {@link Colis}
     */
    public LiveData<List<Colis>> getColis() {
        return colisLiveData;
    }

    /**
     * Replaces the entire list of packages with the given list.
     *
     * @param nouveauxColis the new list of {@link Colis} to set
     */
    public void setColis(List<Colis> nouveauxColis) {
        colisLiveData.setValue(nouveauxColis);
    }

    /**
     * Adds a package to the current list if it is not already present.
     *
     * @param colis the {@link Colis} to add
     */
    public void addColis(Colis colis) {
        List<Colis> currentList = colisLiveData.getValue();
        if (currentList != null) {
            boolean exists = false;
            for (Colis c : currentList) {
                if (c.getNumber() == colis.getNumber()) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                currentList = new ArrayList<>(currentList);
                currentList.add(colis);
                colisLiveData.setValue(currentList);
            }
        }
    }

    /**
     * Removes the specified package from the current list if present.
     *
     * @param colis the {@link Colis} to remove
     */
    public void removeColis(Colis colis) {
        List<Colis> currentList = colisLiveData.getValue();
        if (currentList != null && currentList.contains(colis)) {
            currentList = new ArrayList<>(currentList);
            currentList.remove(colis);
            colisLiveData.setValue(currentList);
        }
    }

    /**
     * Clears all packages from the current list.
     */
    public void clearColis() {
        colisLiveData.setValue(new ArrayList<>());
    }

    /**
     * Returns a LiveData stream of the currently filtered list of packages.
     *
     * @return LiveData containing the filtered list of {@link Colis}
     */
    public LiveData<List<Colis>> getFilteredColis() {
        return filteredColisLiveData;
    }

    /**
     * Filters the current list of packages by the given status and updates
     * the filtered list LiveData. If status equals "All" (case-insensitive),
     * all packages are included.
     *
     * @param status the status to filter by ("All", "Ongoing", "Delivered", "Absent", etc.)
     */
    public void filterColis(String status) {
        List<Colis> current = colisLiveData.getValue();
        if (current == null) return;

        if (status != null && status.equalsIgnoreCase("All")) {
            filteredColisLiveData.setValue(current);
            return;
        }

        int currentStatus = -1;
        switch (status) {
            case "Ongoing":
                currentStatus = 1;
                break;
            case "Delivered":
                currentStatus = 2;
                break;
            case "Absent":
                currentStatus = 3;
                break;
            default:
                break;
        }

        List<Colis> filtered = new ArrayList<>();
        for (Colis c : current) {
            if (c.getStatus() == currentStatus) {
                filtered.add(c);
            }
        }
        filteredColisLiveData.setValue(filtered);
    }

}