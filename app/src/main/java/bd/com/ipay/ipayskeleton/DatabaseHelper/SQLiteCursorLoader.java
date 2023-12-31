package bd.com.ipay.ipayskeleton.DatabaseHelper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * A loader that queries the {@link ContentResolver} and returns a {@link Cursor}.
 * This class implements the {@link android.support.v4.content.Loader} protocol in a standard way for
 * querying cursors, building on {@link AsyncTaskLoader} to perform the cursor
 * query on a background thread so that it does not block the application's UI.
 * <p>
 * <p>A CursorLoader must be built with the full information for the query to
 * perform, either through the
 * {@link #SQLiteCursorLoader(Context, Uri, String[], String, String[], String)} (Context, Uri, String[], String, String[], String)} or
 * creating an empty instance with {@link #SQLiteCursorLoader(Context)} (Context)} and filling
 * in the desired paramters with {@link #setUri(Uri)}, {@link #setSelection(String)},
 * {@link #setSelectionArgs(String[])}, {@link #setSortOrder(String)},
 * and {@link #setProjection(String[])}.
 */
public abstract class SQLiteCursorLoader extends AsyncTaskLoader<Cursor> {
    private final ForceLoadContentObserver mObserver;

    private Uri mUri;
    private String[] mProjection;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mSortOrder;

    private Cursor mCursor;

    /* Runs on a worker thread */
    @Override
    public abstract Cursor loadInBackground();
//    {
//        Cursor cursor = getContext().getContentResolver().query(mUri, mProjection, mSelection,
//                mSelectionArgs, mSortOrder);
//        if (cursor != null) {
//            // Ensure the cursor window is filled
//            cursor.getCount();
//            registerContentObserver(cursor, mObserver);
//        }
//        return cursor;
//    }

    /**
     * Registers an observer to get notifications from the content provider
     * when the cursor needs to be refreshed.
     */
    public void registerContentObserver(Cursor cursor, Uri observerUri) {
        cursor.registerContentObserver(mObserver);
        cursor.setNotificationUri(getContext().getContentResolver(), observerUri);
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        try {
            if (isReset()) {
                // An async query came in while the loader is stopped
                if (cursor != null) {
                    cursor.close();
                }
                return;
            }
            Cursor oldCursor = mCursor;
            mCursor = cursor;

            if (isStarted()) {
                super.deliverResult(cursor);
            }

            if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
                oldCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an empty unspecified CursorLoader.  You must follow this with
     * calls to {@link #setUri(Uri)}, {@link #setSelection(String)}, etc
     * to specify the query to perform.
     */
    public SQLiteCursorLoader(Context context) {
        super(context);
        mObserver = new ForceLoadContentObserver();
    }

    /**
     * Creates a fully-specified CursorLoader.  See
     * {@link ContentResolver#query(Uri, String[], String, String[], String)
     * ContentResolver.query()} for documentation on the meaning of the
     * parameters.  These will be passed as-is to that call.
     */
    public SQLiteCursorLoader(Context context, Uri uri, String[] projection, String selection,
                              String[] selectionArgs, String sortOrder) {
        super(context);
        mObserver = new ForceLoadContentObserver();
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * <p>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public String[] getProjection() {
        return mProjection;
    }

    public void setProjection(String[] projection) {
        mProjection = projection;
    }

    public String getSelection() {
        return mSelection;
    }

    public void setSelection(String selection) {
        mSelection = selection;
    }

    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    public void setSelectionArgs(String[] selectionArgs) {
        mSelectionArgs = selectionArgs;
    }

    public String getSortOrder() {
        return mSortOrder;
    }

    public void setSortOrder(String sortOrder) {
        mSortOrder = sortOrder;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix);
        writer.print("mUri=");
        writer.println(mUri);
        writer.print(prefix);
        writer.print("mProjection=");
        writer.println(Arrays.toString(mProjection));
        writer.print(prefix);
        writer.print("mSelection=");
        writer.println(mSelection);
        writer.print(prefix);
        writer.print("mSelectionArgs=");
        writer.println(Arrays.toString(mSelectionArgs));
        writer.print(prefix);
        writer.print("mSortOrder=");
        writer.println(mSortOrder);
        writer.print(prefix);
        writer.print("mCursor=");
        writer.println(mCursor);
        //writer.print(prefix); writer.print("mContentChanged="); writer.println(mContentChanged);
    }

    private class CursorLoaderContentObserver extends ContentObserver {
        public CursorLoaderContentObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }


    }

}