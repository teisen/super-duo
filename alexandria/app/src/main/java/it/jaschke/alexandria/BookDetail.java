package it.jaschke.alexandria;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String EXTRA_BOOKNAME = "EXTRA_BOOKNAME";
    private final String LOG_TAG = BookDetail.class.getSimpleName();
    public static final String EAN_KEY = "EAN";
    private final int LOADER_ID = 10;
    private View rootView;
    private String ean;
    private String bookTitle;

    public BookDetail(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(LOG_TAG, "onCreateView");
        Bundle arguments = getArguments();
        if (arguments != null) {
            ean = arguments.getString(BookDetail.EAN_KEY);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


        if(savedInstanceState == null) {
            //Log.d(LOG_TAG, "savedInstanceState== " + savedInstanceState + " booktitle=" + bookTitle);
        } else {
            // restore object
            bookTitle = savedInstanceState.getString(EXTRA_BOOKNAME);
            //Log.d(LOG_TAG, "restored String... " + bookTitle);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Log.d(LOG_TAG, "Storing String... " + bookTitle);
        outState.putString(EXTRA_BOOKNAME, bookTitle);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);
        //Log.d(LOG_TAG, "onCreateOptionsMenu");
        MenuItem menuItem = menu.findItem(R.id.action_share);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);

        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        shareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        //Log.d(LOG_TAG, "onLoadFinished");
        bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.fullBookTitle)).setText(bookTitle);

        // refresh the menu bar with the bookTitle
        getActivity().invalidateOptionsMenu();

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        ((TextView) rootView.findViewById(R.id.fullBookDesc)).setText(desc);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        /**
         * Some books searches do not return Authors. To handle this we check if authors is not null
         *
         */
        String authorsText;
        int nrOfAuthors;
        if(authors != null) {
            String[] authorsArr = authors.split(",");
            authorsText = authors.replace(",","\n");
            nrOfAuthors = authorsArr.length;
        } else {
            authorsText = "Unknown";
            nrOfAuthors = 0;
        }
        ((TextView) rootView.findViewById(R.id.authors)).setLines(nrOfAuthors);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authorsText);
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) rootView.findViewById(R.id.fullBookCover)).execute(imgUrl);
            rootView.findViewById(R.id.fullBookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void onPause() {
        super.onDestroyView();
        if(MainActivity.IS_TABLET && rootView.findViewById(R.id.right_container)==null){
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}