// Copyright 2014 Google Inc. All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.u2f.gaedemo.storage;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

import com.google.common.base.Objects;
import com.google.gson.JsonObject;
import com.google.u2f.server.data.SecurityKeyData;

public class TokenStorageData {

  private long enrollmentTime;
  private byte[] keyHandle;
  private byte[] publicKey;
  private byte[] attestationCert;
  private int counter;

  // used by the storage layer
  public TokenStorageData() { }

  public TokenStorageData(SecurityKeyData tokenData) {
    this.enrollmentTime = tokenData.getEnrollmentTime();
    this.keyHandle = tokenData.getKeyHandle();
    this.publicKey = tokenData.getPublicKey();
    try {
      this.attestationCert = tokenData.getAttestationCertificate().getEncoded();
    } catch (CertificateEncodingException e) {
      throw new RuntimeException();
    }
    this.counter = tokenData.getCounter();
  }

  public void updateCounter(int newCounterValue) {
    counter = newCounterValue;
  }
  
  public SecurityKeyData getSecurityKeyData() {
    return new SecurityKeyData(enrollmentTime, keyHandle, 
        publicKey, parseCertificate(attestationCert), counter);
  }
  
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("enrollment_time", enrollmentTime);
    json.addProperty("key_handle", Hex.encodeHexString(keyHandle));
    json.addProperty("public_key", Hex.encodeHexString(publicKey));
    json.addProperty("issuer", 
        getSecurityKeyData().getAttestationCertificate().getIssuerX500Principal().getName()); 
    return json;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }
	
  @Override
  public int hashCode() {
    return Objects.hashCode(
        enrollmentTime,
        keyHandle,
        publicKey,
        attestationCert,
        counter);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TokenStorageData))
      return false;
    TokenStorageData that = (TokenStorageData) obj;
    return (this.enrollmentTime == that.enrollmentTime)
        && (this.counter == that.counter)
        && Arrays.equals(this.keyHandle, that.keyHandle)
        && Arrays.equals(this.publicKey, that.publicKey)
        && Arrays.equals(this.attestationCert, that.attestationCert);
  }
  
  private static X509Certificate parseCertificate(byte[] encodedDerCertificate) {
    try {
      return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(
          new ByteArrayInputStream(encodedDerCertificate));
    } catch (CertificateException e) {
      throw new RuntimeException(e);
    }
  }
}
