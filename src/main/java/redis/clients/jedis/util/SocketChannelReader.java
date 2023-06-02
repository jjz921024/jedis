package redis.clients.jedis.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketChannelReader {

  private final SocketChannel channel;
  private final int size;
  private final int numComponents = 32;

  private ByteBuf buffer;
  private boolean firstRead = true;

  public SocketChannelReader(SocketChannel channel, int size) {
    this.channel = channel;
    if (size <= 0) {
      throw new IllegalArgumentException("Buffer size <= 0");
    }
    this.size = size;
  }

  public SocketChannelReader(SocketChannel channel) {
    this(channel, 512);
  }

  public byte readByte() {
    fillIfFirstRead();
    if (!buffer.isReadable()) {
      fill();
    }
    return buffer.readByte();
  }

  public ByteBuf read(int len) {
    fillIfFirstRead();

    if (!buffer.isReadable()) {
      fill();
    }

    CompositeByteBuf composite = new CompositeByteBuf(UnpooledByteBufAllocator.DEFAULT, true, numComponents);

    int remainCopy = len;
    while (remainCopy > 0) {
      int remaining = buffer.readableBytes();
      int putSize = Math.min(remaining, remainCopy);

      // todo
      composite.addComponent(true, buffer.copy(0, putSize));

      buffer.readerIndex(buffer.readerIndex() + putSize);
      buffer.discardReadBytes();

      remainCopy -= putSize;

      if (!buffer.isReadable()) {
        fill();
      }
    }

    return composite;
  }

  // for error
  public String readLine() {
    byte curr;
    byte next;
    StringBuilder sb = new StringBuilder();

    while (true) {
      curr = readByte();
      if (curr == '\r') {
        next = readByte();

        if (next == '\n') {
          break;
        }

        sb.append((char) curr);
        sb.append((char) next);
      } else {
        sb.append((char) curr);
      }
    }

    String reply = sb.toString();
    if (reply.length() == 0) {
      throw new JedisConnectionException(
          "It seems like server has closed the connection.");
    }
    buffer.discardReadBytes();
    return reply;
  }

  public ByteBuf readLineBytes() {
    byte curr;
    byte next;
    CompositeByteBuf composite = new CompositeByteBuf(UnpooledByteBufAllocator.DEFAULT, true, numComponents);

    while (true) {
      curr = readByte();
      if (curr == '\r') {
        next = readByte();
        if (next == '\n') {
          break;
        }

        composite.addComponent(true, Unpooled.wrappedBuffer(new byte[]{curr, next}));
      } else {
        composite.addComponent(true, Unpooled.wrappedBuffer(new byte[]{curr}));
      }
    }

    buffer.discardReadBytes();
    return composite;
  }


  public int readIntCrLf() {
    byte b = readByte();
    final boolean isNeg = b == '-';

    if (isNeg) {
      b = readByte();
    }

    int value = b - '0';
    while (true) {
      b = readByte();
      if (b == '\r') {
        if (readByte() != '\n') {
          throw new JedisConnectionException("Unexpected character!");
        }

        break;
      } else {
        value = value * 10 + b - '0';
      }
    }
    buffer.discardReadBytes();
    return (isNeg ? -value : value);
  }

  public ByteBuf readLongCrLf() {
    return Unpooled.wrappedBuffer(Integer.toString(readIntCrLf()).getBytes());
  }

  private void fillIfFirstRead() throws JedisConnectionException {
    if (firstRead) {
      fill();
      firstRead = false;
    }
  }

  private void fill() throws JedisConnectionException {
    try {
      ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size);
      if (channel.read(byteBuffer) == -1) {
        throw new JedisConnectionException("Unexpected end of stream.");
      }
      byteBuffer.flip();
      buffer = Unpooled.wrappedBuffer(byteBuffer);

    } catch (IOException e) {
      throw new JedisConnectionException(e);
    }
  }

}