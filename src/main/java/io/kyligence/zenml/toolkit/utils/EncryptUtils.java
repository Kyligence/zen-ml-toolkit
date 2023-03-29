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

import io.kyligence.zenml.toolkit.config.ToolkitConfig;
import io.kyligence.zenml.toolkit.exception.ErrorCode;
import io.kyligence.zenml.toolkit.exception.ToolkitException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class EncryptUtils {

    private static final ToolkitConfig config = ToolkitConfig.getInstance();

    private static final String KEY_ALGORITHM = "AES";

    public static final String DEC_FLAG = "DEC";

    public static final String ENC_PREFIX = "ENC('";
    public static final String ENC_SUBFIX = "')";

    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    private static Key key;

    static {
        try {
            var secretKeyHexStr = config.getSecretKey();
            key = toKey(Hex.decodeHex(secretKeyHexStr));
        } catch (DecoderException e) {
            log.error("Load AES key error", e);
            System.exit(1);
        }
    }

    public static boolean isEncrypted(String value) {
        return StringUtils.isNotEmpty(value) && value.startsWith(ENC_PREFIX) && value.endsWith(ENC_SUBFIX);
    }

    public static byte[] initSecretKey() {
        KeyGenerator kg;
        try {
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            kg.init(128);
            SecretKey secretKey = kg.generateKey();
            return secretKey.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            log.error("no such encryption algorithm", e);
            return new byte[0];
        }
    }

    public static String getKey() {
        return Hex.encodeHexString(key.getEncoded());
    }

    private static Key toKey(byte[] key) {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    public static String encrypt(String plainText) {
        byte[] bytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] encryptBytes = encrypt(bytes, key, DEFAULT_CIPHER_ALGORITHM);
        return Hex.encodeHexString(encryptBytes);
    }

    private static byte[] encrypt(byte[] data, Key key, String cipherAlgorithm) {
        try {
            var cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception ex) {
            log.warn("Password Encryption error", ex);
            return data;
        }
    }

    public static String decrypt(String cipherHexText) {
        byte[] cipherBytes;
        try {
            cipherBytes = Hex.decodeHex(cipherHexText);
        } catch (Exception e) {
            throw new ToolkitException(ErrorCode.PWD_DECODE_ERROR);
        }

        byte[] clearBytes = decrypt(cipherBytes, key, DEFAULT_CIPHER_ALGORITHM);
        return new String(clearBytes, StandardCharsets.UTF_8);
    }

    public static byte[] decrypt(byte[] data, Key key, String cipherAlgorithm) {
        try {
            var cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception ex) {
            throw new ToolkitException(ErrorCode.PWD_DECRYPTION_ERROR);
        }
    }

    public static String decryptPassInNotebook(String value) {
        return decrypt(value.substring(ENC_PREFIX.length(), value.length() - ENC_SUBFIX.length()));
    }
}
