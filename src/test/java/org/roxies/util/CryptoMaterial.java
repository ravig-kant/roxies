/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.cert.Certificate;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 *
 * @author ravigu
 */
public class CryptoMaterial {

    public static String getKeyStore() {
        
        try {
            Path keystoreLoc = File.createTempFile("test-keystore", ".jks").toPath();
            OutputStream fos = Files.newOutputStream(keystoreLoc);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null,null);
            KeyCertPair rootCA = new KeyCertPair("cn=DemoCA,o=roxies", (long) 365 * 24 * 60 * 60);
            KeyCertPair serverCert = new KeyCertPair("cn=Demo,o=roxies", (long) 365 * 24 * 60 * 60);
            Certificate signedCACert = rootCA.createSignedCertificate(rootCA);
            Certificate signedCert = serverCert.createSignedCertificate(rootCA);
            System.out.println(signedCert.toString());
            System.out.println(signedCACert.toString());
            Certificate[] certificates = new Certificate[2];
            certificates[0] = signedCACert;
            certificates[1] = signedCert;

            CertPath path = CertificateFactory.getInstance("X.509").generateCertPath(Arrays.asList(signedCert,signedCACert));

            System.out.println("Keystore location: " + keystoreLoc.toString());
            ks.setKeyEntry("pvtkey", serverCert.key, "welcome".toCharArray(), certificates);
            ks.store(fos, "welcome".toCharArray());
            return keystoreLoc.toString();
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException ex) {
            Logger.getLogger(CryptoMaterial.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static class KeyCertPair {

        public PrivateKey key;
        public X509Certificate certificate;

        public KeyCertPair(String dn, long validity) {
            try {
                CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
                keyGen.generate(1024);
                key = keyGen.getPrivateKey();
                certificate = keyGen.getSelfCertificate(new X500Name(dn), validity);
            } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | IOException | CertificateException | SignatureException ex) {
                Logger.getLogger(CryptoMaterial.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public X509Certificate createSignedCertificate(KeyCertPair rootCA) {
            try {
                Principal issuer = rootCA.certificate.getSubjectDN();
                String issuerSigAlg = rootCA.certificate.getSigAlgName();

                byte[] inCertBytes = certificate.getTBSCertificate();
                X509CertInfo info = new X509CertInfo(inCertBytes);

                info.set(X509CertInfo.ISSUER, (X500Name) issuer);
                X509CertImpl outCert = new X509CertImpl(info);
                outCert.sign(rootCA.key, issuerSigAlg);

                return outCert;
            } catch (CertificateParsingException ex) {
                Logger.getLogger(CryptoMaterial.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CertificateEncodingException | IOException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException ex) {
                Logger.getLogger(CryptoMaterial.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CertificateException ex) {
                Logger.getLogger(CryptoMaterial.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

    }

    public static void main(String[] args) {
        CryptoMaterial.getKeyStore();
    }
}
