package redis.clients.jedis.commands;

public interface XCommands extends KeyCommands, StringCommands, ListCommands, HashCommands,
    SetCommands, SortedSetCommands, GeoCommands, HyperLogLogCommands, StreamCommands,
    ScriptingKeyCommands, SampleKeyedCommands, MiscellaneousCommands {
}
