local result = {}
for i = 1, %d do
    redis.call('zincrby', KEYS[1], %d, ARGV[i])
end
return result