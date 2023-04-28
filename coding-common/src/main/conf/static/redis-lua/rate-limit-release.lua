if (redis.call('HEXISTS', KEYS[1], ARGV[1]) == 0)
then
    return nil
end
local counter = redis.call('HINCRBY', KEYS[1], ARGV[1], -1)
if (counter > 0)
then
    return 0
end
redis.call('HDEL', KEYS[1], ARGV[1])
if (redis.call('HLEN', KEYS[1]) <= 0)
then
    redis.call("DEL", KEYS[1])
    return nil
end
return nil
