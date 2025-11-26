package campusconnect.interfaces;

import campusconnect.db.DataStore;

public interface Repository {
    DataStore load();
    void save(DataStore store);
}
