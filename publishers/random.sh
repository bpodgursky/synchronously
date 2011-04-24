
if [ -e random.out ]; then
   rm random.out
fi

while true
do
  sleep 1
  echo $RANDOM >> random.out
done
