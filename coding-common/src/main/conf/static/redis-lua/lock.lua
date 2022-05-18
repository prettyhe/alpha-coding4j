if (redis.call("EXISTS", KEYS[1]) == 0)
then
    redis.call('HINCRBY', KEYS[1], ARGV[1], 1)
    redis.call('PEXPIRE', KEYS[1], ARGV[2])
    return nil
end
if (redis.call('HEXISTS', KEYS[1], ARGV[1]) == 1)
then
    redis.call('HINCRBY', KEYS[1], ARGV[1], 1)
    redis.call('PEXPIRE', KEYS[1], ARGV[2])
    return nil
end
return redis.call('PTTL', KEYS[1])
