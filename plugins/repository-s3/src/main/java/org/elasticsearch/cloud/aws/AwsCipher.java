package org.elasticsearch.cloud.aws;

import org.elasticsearch.cloud.aws.blobstore.S3BlobStore;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;

public class AwsCipher {
    private static int  IV_SIZE = 16;
    private final SecretKeySpec secretKey;

    public AwsCipher() {
        // TODO: get key from env
        secretKey = new SecretKeySpec("TestKeyTestKeyTe".getBytes(), "AES");
    }

    public int getEncryptedBlobBufferSize(S3BlobStore blobStore) {
        return blobStore.bufferSizeInBytes() + IV_SIZE;
    }

    protected IvParameterSpec generateIv() {
        SecureRandom r = new SecureRandom(); // should be the best PRNG
        byte[] iv = new byte[IV_SIZE];
        r.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    protected Cipher getCipherEncrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        IvParameterSpec iv = generateIv();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        return cipher;
    }

    protected Cipher getCipherDecrypt(byte[] ivBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        return cipher;
    }

    public void encrypt(InputStream is, OutputStream os) throws IOException{
        CipherOutputStream eos = null;

        try {
            Cipher cipher = getCipherEncrypt();
            byte[] iv = cipher.getIV();

            // prepend iv vector to output stream
            os.write(iv);
            os.flush();

            // create cipher output stream, delete reference to os since cos wraps it
            eos = new CipherOutputStream(os, cipher);
            os = null;

            // write encrypted input stream to output stream
            byte[] data = new byte[4096];
            int read = is.read(data);
            while (read != -1) {
                eos.write(data, 0, read);
                read = is.read(data);
            }
            eos.flush();

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (eos != null) {
                eos.close();
            }
            if (os != null) {
                os.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }

    public InputStream decrypt(InputStream is)  {
        try {
            byte[] iv = new byte[IV_SIZE];
            is.read(iv);
            Cipher ciper = getCipherDecrypt(iv);

            CipherInputStream dis = new CipherInputStream(is, ciper);
            return dis;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
