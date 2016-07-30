package com.wire.cryptobox;

import java.io.*;

public class NativeLibraryLoader {

    public static void loadLibrary(String libname) {
        try {
            System.loadLibrary(libname);
        } catch (UnsatisfiedLinkError ue) {
            try {
                String filename = System.mapLibraryName(libname);
                String[] split = filename.split("\\.(?=[^\\.]+$)");
                if (split.length < 2) throw new UnsatisfiedLinkError("Could not extract filename from " + filename);

                File temp = File.createTempFile(split[0], "." + split[split.length - 1]);

                InputStream istream = CryptoBox.class.getResourceAsStream("/" + filename);
                if (istream == null) {
                    throw new FileNotFoundException("Could not find native library " + libname + " in jar.");
                }

                byte[] buffer = new byte[1024];
                int bytes;
                OutputStream ostream = new FileOutputStream(temp);
                try {
                    while ((bytes = istream.read(buffer)) != -1) {
                        ostream.write(buffer, 0, bytes);
                    }
                } finally {
                    ostream.close();
                    istream.close();
                }

                System.load(temp.getAbsolutePath());
            } catch (IOException ie) {
                throw new RuntimeException(ie);
            }
        }

    }
}
