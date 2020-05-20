local a1 = tonumber(ARGV[1])
local a2 = tonumber(ARGV[2])
if (a1 > a2) then
    return a2 - a1
end
local v = redis.call('GET', KEYS[1])
if ((v == false) or (v + a1 <= a2)) then
    return redis.call('INCRBY', KEYS[1], ARGV[1])
else
    return a2 - v - a1
end