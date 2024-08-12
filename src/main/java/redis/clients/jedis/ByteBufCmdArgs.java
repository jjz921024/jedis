package redis.clients.jedis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.exceptions.JedisClusterOperationException;
import redis.clients.jedis.util.JedisClusterCRC16;

import java.util.ArrayList;
import java.util.Iterator;

public class ByteBufCmdArgs implements Iterable<ByteBuf> {

  private int commandHashSlot = -1;

  private final ArrayList<ByteBuf> args;

  public ByteBufCmdArgs(ProtocolCommand command) {
    args = new ArrayList<>();
    args.add(Unpooled.wrappedBuffer(command.getRaw()));
  }

  public ByteBufCmdArgs addObjects(ByteBuf... args) {
    for (Object arg : args) {
      add(arg);
    }
    return this;
  }

  public ByteBufCmdArgs add(Object arg) {
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

  protected ByteBufCmdArgs processKey(byte[] key) {
    final int hashSlot = JedisClusterCRC16.getSlot(key);
    if (commandHashSlot < 0) {
      commandHashSlot = hashSlot;
    } else if (commandHashSlot != hashSlot) {
      throw new JedisClusterOperationException("Keys must belong to same hashslot.");
    }
    return this;
  }

  public int getCommandHashSlot() {
    return commandHashSlot;
  }

  @Override
  public Iterator<ByteBuf> iterator() {
    return args.iterator();
  }

  public int size() {
    return args.size();
  }

}
