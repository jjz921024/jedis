package redis.clients.jedis.executors;

import redis.clients.jedis.ByteBufCmdArgs;
import redis.clients.jedis.CommandObject;

public interface CommandExecutor extends AutoCloseable {

  <T> T executeCommand(CommandObject<T> commandObject);

  default Object executeCommand(ByteBufCmdArgs cmdArgs) {
    throw new RuntimeException("Not impl");
  }
}
