package co.highfive.petrolstation.listener;

import java.io.File;

public interface DeleteListener {
    void pos(int pos , File file );
    void pos(int pos , int imageId);
}
