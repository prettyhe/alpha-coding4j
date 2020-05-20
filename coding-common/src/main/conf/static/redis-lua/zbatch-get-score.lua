local result = {}
for i, v in ipairs(ARGV) do
   result[i]=redis.call('ZSCORE', KEYS[1], v)
end
return result

