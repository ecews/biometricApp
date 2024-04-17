package org.ecews.biometricapp.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This utility compresses a list of files to standard ZIP format file.
 * It is able to compress all sub files and sub directories, recursively.
 *
 * @author www.codejava.net
 */
@Slf4j
public class ZipUtility {
    /**
     * A constants for buffer size used to read/write data
     */
    private static final int BUFFER_SIZE = 4096;

    /**
     * Compresses a list of files to a destination zip file
     *
     * @param listFiles   A collection of files and directories
     * @param destZipFile The path of the destination zip file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zip(List<File> listFiles, String destZipFile, long splitSize) throws FileNotFoundException,
            IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream (destZipFile));
        long totalSize = 0L;
        for (File file : listFiles) {
            if (file.isDirectory()) {
                zipDirectory(file, file.getName(), zos);
            } else {
                if (totalSize > splitSize) {
                    totalSize = 0;
                    zos.flush();
                    zos.close();
                    File out = new File(destZipFile);
                    String name = out.getName();
                    String dir = out.getParent();
                    String newZip = dir + File.separator + name + "_" + RandomStringUtils.randomAlphabetic(5);
                    zos = new ZipOutputStream(new FileOutputStream(newZip));
                }
                totalSize += zipFile(file, zos).getCompressedSize();
            }
        }
        zos.flush();
        zos.close();
    }

    /**
     * Compresses files represented in an array of paths
     *
     * @param files       a String array containing file paths
     * @param destZipFile The path of the destination zip file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zip(String[] files, String destZipFile, long splitSize) throws FileNotFoundException, IOException {
        List<File> listFiles = new ArrayList<> ();
        for (String file : files) {
            listFiles.add(new File(file));
        }
        zip(listFiles, destZipFile, splitSize);
    }

    /**
     * Adds a directory to the current zip output stream
     *
     * @param folder       the directory to be  added
     * @param parentFolder the path of parent directory
     * @param zos          the current zip output stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void zipDirectory(File folder, String parentFolder,
                                     ZipOutputStream zos) throws FileNotFoundException, IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            zos.putNextEntry(new ZipEntry (parentFolder + "/" + file.getName()));
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            long bytesRead = 0;
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = bis.read(bytesIn)) != -1) {
                zos.write(bytesIn, 0, read);
                bytesRead += read;
            }
            zos.closeEntry();
        }
    }

    /**
     * Adds a file to the current zip output stream
     *
     * @param file the file to be added
     * @param zos  the current zip output stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static ZipEntry zipFile(File file, ZipOutputStream zos) throws FileNotFoundException, IOException {
        ZipEntry entry = new ZipEntry(file.getName());
        zos.putNextEntry(entry);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        long bytesRead = 0;
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = bis.read(bytesIn)) != -1) {
            zos.write(bytesIn, 0, read);
            bytesRead += read;
        }
        zos.closeEntry();
        bis.close();
        return entry;
    }
    public static long getFolderSize(File folder) {
        long size = 0;
        
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += getFolderSize(file);
                    }
                }
            }
        } else {
            size = folder.length();
        }
        
        return size;
    }

}
