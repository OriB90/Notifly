package net.notifly.core.gui.activity.main;

import android.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import net.notifly.core.Notifly;
import net.notifly.core.R;
import net.notifly.core.entity.Location;
import net.notifly.core.sql.LocationDAO;
import net.notifly.core.util.LocationHandler;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EFragment(R.layout.fragment_location)
@OptionsMenu(R.menu.fav_locations)
public class LocationFragment extends Fragment implements AddressLoader.Callbacks {
    @ViewById(android.R.id.list)
    AbsListView locationsListView;
    @Bean
    LocationHandler locationHandler;

    List<Location> locations;

    public static LocationFragment newInstance() {
        LocationFragment_ fragment = new LocationFragment_();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @AfterViews
    void loadLocations() {
        if (locations == null) {
            LocationDAO locationDAO = new LocationDAO(getActivity());
            locations = locationDAO.getFavoriteLocations();
            locationDAO.close();
            loadAddresses();
        }

        ArrayAdapter<Location> adapter = new ArrayAdapter<Location>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, locations);
        locationsListView.setAdapter(adapter);
    }

    private void loadAddresses() {
        for (Location location : locations) {
            if (!LocationHandler.isValid(location.address)) {
                new AddressLoader(getActivity(), location).setListener(this).execute();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getActionBar().setTitle(getString(R.string.title_section_fav_locations));
    }

    @Override
    public void notifyPostExecute() {
        ((ArrayAdapter)locationsListView.getAdapter()).notifyDataSetChanged();
    }
}
