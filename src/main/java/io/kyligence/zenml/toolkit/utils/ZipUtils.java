/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.kyligence.zenml.toolkit.utils;

import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.*;

@Slf4j
@UtilityClass
public class ZipUtils {

    public static void compressZipFile(String sourceDir, String zipFilename) throws IOException {
        try (ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(zipFilename))) {
            compressDirectoryToZipfile(normDir(new File(sourceDir).getParent()), normDir(sourceDir), zipFile);
        }
    }

    public static Map<String, String> uncompressZipFile(File zipFile, String targetDir) throws IOException {
        log.debug("Unzip {} to {}", zipFile.getAbsoluteFile(), targetDir);
        ZipFile zip = new ZipFile(zipFile);
        Enumeration zipEntries = zip.entries();
        Map<String, String> files = Maps.newHashMap();
        while (zipEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipEntries.nextElement();
            String entryName = entry.getName();
            File file = (targetDir != null) ? new File(targetDir, entryName) : new File(entryName);
            if (entry.isDirectory()) {
                if (!file.mkdirs() && !file.isDirectory()) {
                    throw new IOException("Failed to create directory: " + file);
                }
            } else {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    if (!parent.mkdirs() && !file.isDirectory()) {
                        throw new IOException("Failed to create directory: " + parent);
                    }
                }

                InputStream in = zip.getInputStream(entry);
                OutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    IOUtils.copy(in, out);
                    files.put(file.getName(), file.getAbsolutePath());
                } finally {
                    IOUtils.closeQuietly(out);
                    IOUtils.closeQuietly(in);
                }
            }
        }
        IOUtils.closeQuietly(zip);
        return files;
    }

    private static void compressDirectoryToZipfile(String rootDir, String sourceDir, ZipOutputStream out)
            throws IOException {
        File[] sourceFiles = new File(sourceDir).listFiles();
        if (null == sourceFiles) {
            return;
        }

        for (File sourceFile : sourceFiles) {
            if (sourceFile.isDirectory()) {
                compressDirectoryToZipfile(rootDir, sourceDir + normDir(sourceFile.getName()), out);
            } else {
                ZipEntry entry =
                        new ZipEntry(normDir(StringUtils.isEmpty(rootDir) ? sourceDir : sourceDir.replace(rootDir, ""))
                                + sourceFile.getName());
                entry.setTime(sourceFile.lastModified());
                out.putNextEntry(entry);

                FileInputStream in = new FileInputStream(sourceDir + sourceFile.getName());
                IOUtils.copy(in, out);
                IOUtils.closeQuietly(in);
            }
        }
    }

    private static String normDir(String dirName) {
        if (StringUtils.isNotEmpty(dirName) && !dirName.endsWith(File.separator)) {
            dirName = dirName + File.separator;
        }
        return dirName;
    }

    public static byte[] gzip(String origin) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (GZIPOutputStream out = new GZIPOutputStream(bos)) {
                out.write(origin.getBytes(StandardCharsets.UTF_8));
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decompressGzip(byte[] contentBytes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            IOUtils.copy(new GZIPInputStream(new ByteArrayInputStream(contentBytes)), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}