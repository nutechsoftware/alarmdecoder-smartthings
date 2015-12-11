import socket
import struct
import sys

from select import select

from ssdp import Response, Request

class SSDPServer(object):
    MCAST_PORT = 1900
    MCAST_ADDRESS = '239.255.255.250'

    LOCATION_MESSAGE = ('HTTP/1.1 200 OK\r\n' +
                        'CACHE-CONTROL: max-age = 60\r\n' +
                        'EXT:\r\n' +
                        'LOCATION: %(loc)s\r\n' +
                        'SERVER: Blah/1.0 UPnP/1.1 AlarmDecoder/1.0\r\n' +
                        'ST: %(service)s\r\n' +
                        'USN: %(usn)s\r\n' +
                        '\r\n')

    def __init__(self):
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 2)
        sock.bind(('', self.MCAST_PORT))

        mreq = struct.pack('4sl', socket.inet_aton(self.MCAST_ADDRESS), socket.INADDR_ANY)
        sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

        self._socket = sock

    def loop(self):
        while True:
            rl, wl, xl = select([self._socket], [], [])
            for s in rl:
                data, addr = s.recvfrom(4096)
                print '<', addr, data

                request = Request(data)
                if not request.error_code and \
                        request.command == 'M-SEARCH' and \
                        request.path == '*' and \
                        ('AlarmDecoder' in request.headers['ST'] or request.headers['ST'].startswith('ssdp:all')) and \
                        request.headers['MAN'] == '"ssdp:discover"':
                    #service = request.headers['ST'].split(':', 2)[1]
                    #if service == 'api' or service == 'all':
                    response_message = self.LOCATION_MESSAGE % dict(service=request.headers['ST'], loc='http://10.10.0.14:5000', usn="uuid:a91d4fae-7dec-11d0-a765-00a0c91c6bf6")
                    print '>', response_message
                    self._socket.sendto(response_message, addr)

def main():
    socket.setdefaulttimeout(5.0)
    
    server = SSDPServer()
    print server

    server.loop()

if __name__ == '__main__':
    main()

