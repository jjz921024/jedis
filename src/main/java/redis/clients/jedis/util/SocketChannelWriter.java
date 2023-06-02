package redis.clients.jedis.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class SocketChannelWriter {

  private static final ByteBuf EOL_SHORT = Unpooled.unreleasableBuffer(Unpooled.copyShort((short) (('\r' << 8) | '\n')));

  private final CompositeByteBuf buffer;
  private final SocketChannel channel;

  public SocketChannelWriter(SocketChannel channel, int size) {
    this.channel = channel;
    if (size <= 0) {
      throw new IllegalArgumentException("Buffer size <= 0");
    }
    buffer = new CompositeByteBuf(PooledByteBufAllocator.DEFAULT, true, size);
  }

  public SocketChannelWriter(SocketChannel channel) {
    this(channel, 64);
  }

  public void write(final byte b) throws IOException {
    buffer.addComponent(true, Unpooled.wrappedBuffer(new byte[]{b}));
  }

  public void write(final byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  public void write(final byte[] b, final int off, final int len) throws IOException {
    buffer.addComponent(true, Unpooled.wrappedBuffer(b, off, len));
  }

  public void write(ByteBuf arg) throws IOException {
    buffer.addComponent(true, arg);
  }

  public void writeCrLf() throws IOException {
    buffer.addComponent(true, EOL_SHORT);
  }

  public void writeIntCrLf(int value) throws IOException {
    buffer.addComponent(true, Unpooled.wrappedBuffer(Integer.toString(value).getBytes()));
    writeCrLf();
  }

  public void flush() throws IOException {
    flushBuffer();
  }

  private void flushBuffer() throws IOException {
    // we have no data to write
    if (!buffer.isReadable()) {
      return;
    }

    // todo: 验证是否发生拷贝
    CompositeByteBuf consolidate = buffer.consolidate();
    channel.write(consolidate.nioBuffer());

    buffer.readerIndex(consolidate.readableBytes());
    buffer.discardReadBytes();
  }

}