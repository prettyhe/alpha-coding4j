redis.call('INCRBY', KEYS[1], ARGV[1])
return redis.call('GET', KEYS[1])