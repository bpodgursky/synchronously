while true
do
  sleep 5
  wget -q http://www.weather.gov/xml/current_obs/K$1.xml

  wind=$( { xpath -q -e "//wind_mph/text()" K$1.xml;} )
  temp=$( { xpath -q -e "//temperature_string/text()" K$1.xml;} )
  obstime=$( { xpath -q -e "//observation_time_rfc822/text()" K$1.xml;} )

  echo $obstime	$temp	$wind >> $1.out

  rm K$1.xml
done
