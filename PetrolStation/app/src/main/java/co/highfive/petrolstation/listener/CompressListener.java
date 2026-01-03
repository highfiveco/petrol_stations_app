package co.highfive.petrolstation.listener;

import android.net.Uri;

import java.io.File;

public interface CompressListener {
    void success(File file, Uri uriOrigin);
    void success(File file);
}
