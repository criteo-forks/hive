/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.thrift.transport.sasl;

import org.apache.thrift.transport.TNonblockingTransport;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class SaslWriter {

  protected final TNonblockingTransport transport;
  protected ByteBuffer byteBuffer;

  protected SaslWriter(TNonblockingTransport transport) {
    this.transport = transport;
  }

  /**
   * Provide (maybe empty) header and payload to the writer. This can be called only when isComplete
   * returns true.
   *
   * @param header Some extra header bytes (without the 4 bytes for payload length), which will be
   *               the start of the frame. It can be empty, depending on the message format.
   * @param payload
   * @return this writer
   * @throws IllegalStateException if it is called when isComplete returns false.
   */
  public SaslWriter withHeaderAndPayload(byte[]header, byte[] payload) throws IllegalStateException {
    if (!isComplete()) {
      throw new IllegalStateException("Previsous write is not yet complete, with " + byteBuffer.remaining() + " bytes left.");
    }
    if (payload == null) {
      payload = new byte[0];
    }
    byteBuffer = buildBuffer(header, payload);
    byteBuffer.rewind();
    return this;
  }

  /**
   * Provide all the bytes of the frame (header and payload) to the writer.
   *
   * @param frameBytes the frame as a byte array.
   * @return this writer.
   * @throws IllegalStateException if it is called when isComplete returns false.
   */
  public SaslWriter withFrameBytes(byte[] frameBytes) {
    if (!isComplete()) {
      throw new IllegalStateException("Previsous write is not yet complete, with " + byteBuffer.remaining() + " bytes left.");
    }
    if (frameBytes == null) {
      frameBytes = new byte[0];
    }
    byteBuffer = ByteBuffer.wrap(frameBytes);
    return this;
  }

  protected abstract ByteBuffer buildBuffer(byte[] header, byte[] payload);

  /**
   * Nonblocking write to the underlying transport.
   * @throws IOException
   */
  public void write() throws IOException {
    if (byteBuffer.hasRemaining()) {
      transport.write(byteBuffer);
    }
  }

  /**
   *
   * @return true when no more data needs to be written out.
   */
  public boolean isComplete() {
    return byteBuffer == null || !byteBuffer.hasRemaining();
  }
}
