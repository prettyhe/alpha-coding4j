if (redis.call('HEXISTS', KEYS[1], ARGV[1]) == 0) then
    return nil
end
local counter = redis.call('HINCRBY', KEYS[1], ARGV[1], -1)
if (counter > 0) then
    return 0
else
    redis.call("DEL", KEYS[1])
    return 1
end
return nil
