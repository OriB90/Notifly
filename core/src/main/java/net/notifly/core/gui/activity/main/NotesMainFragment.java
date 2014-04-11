package net.notifly.core.gui.activity.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

import net.notifly.core.Notifly;
import net.notifly.core.R;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.main.swipe.NotesSwipeListViewListener;
import net.notifly.core.gui.activity.note.NewNoteActivity_;
import net.notifly.core.sql.NotesDAO;
import net.notifly.core.util.LocationHandler;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_main)
@OptionsMenu(R.menu.main)
public class NotesMainFragment extends Fragment implements AddressLoader.Callbacks {
    public static final int NEW_NOTE_CODE = 1;
    public static final String EXTRA_NOTE = "net.notifly.core.note";

    @App
    Notifly notifly;
    @ViewById(R.id.notes_list_view)
    SwipeListView swipeListView;
    @Bean
    NotesAdapter adapter;
    @Bean
    LocationHandler locationHandler;

    public static NotesMainFragment newInstance() {
        NotesMainFragment fragment = new NotesMainFragment_();
        fragment.setHasOptionsMenu(true);
        return fragment;
    }

    @AfterViews
    void createNotesListView() {
        adapter.addAll(notifly.getNotes());
        swipeListView.setAdapter(adapter);
        swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onClickFrontView(int position) {
                editNote(adapter.getItem(position));
            }
        });
    }

    void editNote(Note note) {
        swipeListView.closeOpenedItems();
        Intent intent = new Intent(getActivity(), NewNoteActivity_.class);
        intent.putExtra(EXTRA_NOTE, note);
        startActivityForResult(intent, NEW_NOTE_CODE);
    }

    @OptionsItem(R.id.action_add_note)
    void openNewNoteActivity() {
        swipeListView.closeOpenedItems();
        Intent intent = new Intent(getActivity(), NewNoteActivity_.class);
        startActivityForResult(intent, NEW_NOTE_CODE);
    }

    @OnActivityResult(NEW_NOTE_CODE)
    void afterNewNote(int resultCode, Intent intent) {
        if (resultCode == MainActivity.RESULT_OK) {
            Note note = intent.getParcelableExtra(EXTRA_NOTE);
            int position = adapter.getPosition(note);

            if (position >= 0) {
                adapter.remove(note);
            } else {
                position = 0;
            }

            adapter.insert(note, position);
            adapter.notifyDataSetChanged();
            notifly.getNotes().remove(note);
            notifly.addNote(note, this);
        }
    }

    void deleteNote(Note note, int position) {
        NotesDAO notesDAO = new NotesDAO(getActivity());
        notesDAO.deleteNote(note);
        notesDAO.close();
        notifly.getNotes().remove(note);
        swipeListView.closeAnimate(position);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getActionBar().setTitle(getString(R.string.title_section_notes));
    }

    @Override
    public void notifyPostExecute() {
        adapter.notifyDataSetChanged();
    }
}