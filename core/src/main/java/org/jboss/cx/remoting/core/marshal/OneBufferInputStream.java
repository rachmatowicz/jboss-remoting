/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.cx.remoting.core.marshal;

import java.io.InputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;

/**
 *
 */
public final class OneBufferInputStream extends InputStream {

    private final Object lock = new Object();
    private ByteBuffer buffer;

    private ByteBuffer getBuffer() throws InterruptedIOException {
        synchronized (lock) {
            for (;;) {
                final ByteBuffer buffer = this.buffer;
                if (buffer != null) {
                    if (! buffer.hasRemaining()) {
                        lock.notify();
                        this.buffer = null;
                    } else {
                        return buffer;
                    }
                }
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new InterruptedIOException("getBuffer() operation interrupted!");
                }
            }
        }
    }

    public int read() throws IOException {
        synchronized (lock) {
            return getBuffer().get() & 0xff;
        }
    }

    public int read(final byte[] b, int off, int len) throws IOException {
        synchronized (lock) {
            return 0;
        }
    }
}