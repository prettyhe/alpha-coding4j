local a1 = tonumber(ARGV[1])
local a2 = tonumber(ARGV[2])
local v = redis.call('DECRBY', KEYS[1], ARGV[1])
if (v >= a2) then
    return v
else
    redis.call('SET', KEYS[1], ARGV[2])
    return a2
end