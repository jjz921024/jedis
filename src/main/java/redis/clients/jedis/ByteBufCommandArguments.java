package redis.clients.jedis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import redis.clients.jedis.commands.ProtocolCommand;

import java.util.ArrayList;
import java.util.Iterator;

public class ByteBufCommandArguments implements Iterable<ByteBuf> {

  private final ArrayList<ByteBuf> args;

  public ByteBufCommandArguments(ProtocolCommand command) {
    args = new ArrayList<>();
    args.add(Unpooled.wrappedBuffer(command.getRaw()));
  }

  public ByteBufCommandArguments addObjects(ByteBuf... args) {
    for (Object arg : args) {
      add(arg);
    }
    return this;
  }

  public ByteBufCommandArguments add(Object arg) {
    if (arg instanceof ByteBuf) {
      args.add((ByteBuf) arg);
    } else if (arg instanceof byte[]) {
      args.add(Unpooled.wrappedBuffer((byte[]) arg));
    } else if (arg instanceof String) {
      args.add(Unpooled.wrappedBuffer(((String) arg).getBytes()));
    } else if (arg instanceof Boolean) {
      String bool = Integer.toString((Boolean) arg ? 1 : 0);
      args.add(Unpooled.wrappedBuffer(bool.getBytes()));
    } else {
      if (arg == null) {
        throw new IllegalArgumentException("null is not a valid argument.");
      }
    }
    return this;
  }

  @Override
  public Iterator<ByteBuf> iterator() {
    return args.iterator();
  }

  public int size() {
    return args.size();
  }

}
