local result = {}
local ret = redis.call('SETNX', KEYS[1], ARGV[1])
table.insert(result, ret)
if ret == 1 then
    redis.call('EXPIRE', KEYS[1], ARGV[2])
    table.insert(result, ARGV[1])
else
    local val = redis.call('GET', KEYS[1])
    table.insert(result, val)
end
return result