package redis.clients.jedis.executors;

import redis.clients.jedis.ByteBufCommandArguments;
import redis.clients.jedis.CommandObject;

public interface CommandExecutor extends AutoCloseable {

  <T> T executeCommand(CommandObject<T> commandObject);

  default <T> T executeCommand(ByteBufCommandArguments commandArguments) {
    throw new UnsupportedOperationException();
  }

}
