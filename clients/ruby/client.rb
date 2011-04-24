#!/usr/bin/ruby

require 'socket'

sock = TCPSocket.open(ARGV[0], ARGV[1])

while(true)
  puts "from server: "+sock.gets("\n")
end
