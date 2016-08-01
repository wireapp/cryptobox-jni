package com.wire.cryptobox;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class NativeLibraryLoader {

    private static Path tempDir = null;

    public static void loadLibrary(String libname) {
        try {
            System.loadLibrary(libname);
        } catch (UnsatisfiedLinkError ue) {
            try {
                String filename = System.mapLibraryName(libname);

                if (tempDir == null) {
                    tempDir = Files.createTempDirectory("native" + System.nanoTime());
                }
                File tempFile = new File(tempDir.toAbsolutePath().toString(), filename);

                InputStream istream = CryptoBox.class.getResourceAsStream("/" + filename);
                if (istream == null) {
                    throw new FileNotFoundException("Could not find native library " + libname + " in jar.");
                }

                byte[] buffer = new byte[1024];
                int bytes;
                OutputStream ostream = new FileOutputStream(tempFile);
                try {
                    while ((bytes = istream.read(buffer)) != -1) {
                        ostream.write(buffer, 0, bytes);
                    }
                } finally {
                    ostream.close();
                    istream.close();
                }

                System.load(tempFile.getAbsolutePath());
            } catch (IOException ie) {
                throw new RuntimeException(ie);
            }
        }

    }
}
