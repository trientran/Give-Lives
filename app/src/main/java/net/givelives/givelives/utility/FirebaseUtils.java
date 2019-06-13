package net.givelives.givelives.utility;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by MainAcc on 27/11/2017.
 */
//FirebaseUtils.getDatabaseRef()
public class FirebaseUtils {
    private static DatabaseReference mDatabaseRef;
    private static FirebaseDatabase database;
    public static DatabaseReference getDatabaseRef() {
        if (mDatabaseRef == null) {
            getDatabase();
            mDatabaseRef = database.getReference();
            // ...
        }
        return mDatabaseRef;
    }

    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
            // ...
        }
        return database;
    }
}
