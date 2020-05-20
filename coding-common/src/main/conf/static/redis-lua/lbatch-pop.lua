local result = {}
local batch = %d
local count = redis.call('LLEN', KEYS[1])
	if count > batch then
		for i = 0, (batch-1) do
			local val = redis.call('LPOP', KEYS[1])
			table.insert(result, val)
		end
	else 
		for i = 0, (count-1) do
			local val = redis.call('LPOP', KEYS[1])
			table.insert(result, val)
		end
	end
return result