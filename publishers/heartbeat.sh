if [ -e heartbeat.out ]; then
   rm heartbeat.out
fi

while true
do
  sleep 1
  date >> heartbeat.out
done
