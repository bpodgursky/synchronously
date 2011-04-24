if [ -e cputemp.out ]; then
   rm cputemp.out
fi

while true
do
  sleep 1
  sensors thinkpad-isa-0000 2>&1 | grep temp1: | awk '{print $2}' >> cputemp.out
done
