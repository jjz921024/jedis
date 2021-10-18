package redis.clients.jedis.commands;

public interface XBinaryCommands extends KeyBinaryCommands, StringBinaryCommands,
    ListBinaryCommands, HashBinaryCommands, SetBinaryCommands, SortedSetBinaryCommands,
    GeoBinaryCommands, HyperLogLogBinaryCommands, StreamBinaryCommands, ScriptingKeyBinaryCommands,
    SampleBinaryKeyedCommands, MiscellaneousBinaryCommands {
}
