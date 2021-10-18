package redis.clients.jedis;

import redis.clients.jedis.commands.ProtocolCommand;

public class ConnectionCommandObjects {

  protected CommandArguments commandArguments(ProtocolCommand command) {
    return new CommandArguments(command);
  }

  public final CommandObject<Boolean> exists(String key) {
    return new CommandObject<>(commandArguments(Protocol.Command.EXISTS).key(key), BuilderFactory.BOOLEAN);
  }

}
